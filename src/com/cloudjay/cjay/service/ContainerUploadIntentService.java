package com.cloudjay.cjay.service;

import java.sql.SQLException;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Trace;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.LogUserActivityEvent;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.MismatchDataException;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.ServerInternalErrorException;
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.UploadType;

import de.greenrobot.event.EventBus;

@EService
public class ContainerUploadIntentService extends IntentService implements CountingInputStreamEntity.UploadListener {

	static final int NOTIFICATION_ID = 1000;
	private CJayImageDaoImpl cJayImageDaoImpl;
	private ContainerSessionDaoImpl containerSessionDaoImpl;

	public ContainerUploadIntentService() {
		super("UploadIntentService");
	}

	/**
	 * Upload ContainerSession to server
	 * 
	 * @param containerSession
	 */
	@Trace(level = Log.INFO)
	synchronized void doUploadContainer(ContainerSession containerSession) {

		Logger.w("Uploading container: " + containerSession.getContainerId());
		DataCenter.getDatabaseHelper(getApplicationContext())
					.addUsageLog("Begin to #upload container: " + containerSession.getContainerId());

		UploadType uploadType = UploadType.values()[containerSession.getUploadType()];
		String response = "";
		containerSession.setUploadState(UploadState.IN_PROGRESS);

		try {
			containerSessionDaoImpl.update(containerSession);
		} catch (SQLException e) {

			Logger.e("Cannot change State to `IN PROGRESS`. Process will be stopped.");
			e.printStackTrace();
			return;
		}

		// Convert ContainerSession to TmpContainerSession for uploading
		TmpContainerSession uploadItem = null;
		try {

			// Temporary upload, use different type of mapping
			if (uploadType == UploadType.NONE) {
				uploadItem = Mapper.getInstance().toTmpContainerSession(getApplicationContext(), containerSession,
																		false);
			} else {
				uploadItem = Mapper.getInstance().toTmpContainerSession(getApplicationContext(), containerSession);
			}

		} catch (Exception e) {

			containerSession.setUploadState(UploadState.ERROR);
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	"#upload #failed container " + containerSession.getContainerId() + " | "
												+ Integer.toString(containerSession.getUploadType())
												+ " | #error on #conversion to Upload Format");
			e.printStackTrace();
		}

		try {
			response = CJayClient.getInstance().postContainerSession(getApplicationContext(), uploadItem);
			Logger.Log("Response from server: " + response);

		} catch (NoConnectionException e) {

			Logger.Log("No Internet Connection");
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog("No connection | #rollback | Container: " + containerSession.getContainerId());
			rollbackContainerState(containerSession);
			return;

		} catch (NullSessionException e) {

			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog("Null Session | #rollback | Container: " + containerSession.getContainerId());
			rollbackContainerState(containerSession);

			// Log user out
			CJayApplication.logOutInstantly(getApplicationContext());
			onDestroy();

		} catch (MismatchDataException e) {

			e.printStackTrace();
			// Set state to Error
			containerSession.setUploadState(UploadState.ERROR);
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	"#upload #failed container " + containerSession.getContainerId() + " | "
												+ Integer.toString(containerSession.getUploadType()));
			return;

		} catch (ServerInternalErrorException e) {

			Logger.e("Server Internal Error cmnr");
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	"Server Internal Error | #rollback | Container: "
												+ containerSession.getContainerId());
			rollbackContainerState(containerSession);
			return;

		} catch (Exception e) {
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog("Unknown Exception | #rollback | Container: " + containerSession.getContainerId());
			rollbackContainerState(containerSession);
			return;
		}

		boolean isInterruptedByOfficialUpload = false;

		// this is temporary upload
		if (uploadType == UploadType.NONE) {

			try {
				Logger.Log("Refresh container session " + containerSession.getContainerId());
				containerSessionDaoImpl.refresh(containerSession);

			} catch (SQLException e) {
				e.printStackTrace();
			}

			UploadType newUploadType = UploadType.values()[containerSession.getUploadType()];

			// but while the upload is in progress, user want to upload official version
			// so we don't need to restored session state.
			if (containerSession.isOnLocal() == false && newUploadType == UploadType.IN) {
				isInterruptedByOfficialUpload = true;
				Logger.w("is interrupted by official upload");
			}
		}

		// convert back then save containerSession
		try {

			Mapper.getInstance().update(getApplicationContext(), response, containerSession);

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {
			EventBus.getDefault()
					.post(	new LogUserActivityEvent((containerSession.getContainerId()
									+ " | #error when update response data | Stack trace: " + e.getMessage())));
			e.printStackTrace();
		}

		containerSession.setUploadState(UploadState.COMPLETED);
		Logger.Log("Upload successfully container " + containerSession.getContainerId() + " | " + uploadType.name());
		DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog(	"#upload #successfully container "
																					+ containerSession.getContainerId()
																					+ " | " + uploadType.name());

		// Restore container upload state to NORMAL if upload_type = NONE (temporary upload at GateImport)
		synchronized (containerSession) {

			switch (uploadType) {
				case NONE:
					if (isInterruptedByOfficialUpload) {

						Logger.Log("User trigger upload on GateImportFragment. It will be uploaded again.");
						containerSession.setUploadState(UploadState.WAITING);

					} else {

						containerSession.setUploadConfirmation(false);
						containerSession.setUploadState(UploadState.NONE);

						try {
							containerSessionDaoImpl.update(containerSession);
						} catch (SQLException e) {
							e.printStackTrace();
						}

						// - Clear upload from upload fragment
						// - Refresh container in CameraActivity
						// - Refresh list item in GateImport
						Logger.w("Notify UploadState RESTORED Event");
						EventBus.getDefault().post(new UploadStateRestoredEvent(containerSession));
					}
					break;

				default:
					break;
			}
		}
	}

	@Override
	public void onChange(int progress) {
	}

	@Override
	public void onCreate() {

		try {
			if (null == containerSessionDaoImpl) {
				containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager()
													.getHelper(getApplicationContext()).getContainerSessionDaoImpl();
			}

			if (null == cJayImageDaoImpl) {
				cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(getApplicationContext())
												.getCJayImageDaoImpl();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		EventBus.getDefault().register(this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {

		try {
			stopForeground(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	// Use to clear item
	public void onEvent(UploadStateChangedEvent event) {

		ContainerSession containerSession = event.getTarget();
		UploadState uploadState = UploadState.values()[containerSession.getUploadState()];

		Logger.Log("onEvent UploadStateChangedEvent | " + uploadState.name());
		switch (uploadState) {
			case COMPLETED:
			case ERROR:
			case WAITING:

				try {
					containerSessionDaoImpl.update(containerSession);
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.e("Cannot update state for container " + containerSession.getContainerId());
					Logger.e("Current state " + Integer.toString(containerSession.getUploadState()));
				}

				break;

			case IN_PROGRESS:
			default:
				break;
		}

		EventBus.getDefault().post(new ContainerSessionUpdatedEvent(containerSession));
	}

	/**
	 * Main method, it called from QueueIntentService
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (NetworkHelper.isConnected(getApplicationContext())) {
			try {
				ContainerSession containerSession = containerSessionDaoImpl.getNextWaiting(	this,
																							DataCenter.getDatabaseHelper(	this)
																										.getWritableDatabase());

				if (null != containerSession) {
					doUploadContainer(containerSession);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		stopSelf();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void rollbackContainerState(ContainerSession containerSession) {

		UploadType uploadType = UploadType.values()[containerSession.getUploadType()];
		DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog(	"#rollback "
																					+ containerSession.getContainerId()
																					+ " | " + uploadType.name());

		Logger.w("Rolling back type: " + uploadType.name());
		switch (uploadType) {
			case IN:
				containerSession.setOnLocal(false);
				break;

			case OUT:
				containerSession.setCheckOutTime("");
				break;

			case AUDIT:
			case REPAIR:
			default:
				break;
		}

		containerSession.setUploadConfirmation(false);
		containerSession.setUploadState(UploadState.NONE);

		try {
			containerSessionDaoImpl.update(containerSession);
		} catch (SQLException e) {
			Logger.Log("Error when rolling back container " + containerSession.getContainerId());
			e.printStackTrace();
		}
	}
}
