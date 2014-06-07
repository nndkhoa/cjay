package com.cloudjay.cjay.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Trace;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionUpdatedEvent;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.events.UploadStateRestoredEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.MismatchDataException;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.ServerInternalErrorException;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;

@EService
public class ContainerUploadIntentService extends IntentService implements CountingInputStreamEntity.UploadListener {

	static final int NOTIFICATION_ID = 2000;
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

		UploadType uploadType = UploadType.values()[containerSession.getUploadType()];
		String response = "";

		Logger.w("Uploading container: " + containerSession.getContainerId() + " | " + uploadType.name());
		DataCenter.getDatabaseHelper(getApplicationContext())
					.addUsageLog("Begin to #upload container: " + containerSession.getContainerId());

		// Convert ContainerSession to TmpContainerSession for uploading
		TmpContainerSession uploadItem = null;
		try {

			// Temporary upload, use different type of mapping
			if (uploadType == UploadType.NONE) {

				Logger.w("Upload temp container " + containerSession.getContainerId());
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
			return;
		}

		try {

			containerSession.setUploadState(UploadState.IN_PROGRESS);

			response = CJayClient.getInstance().postContainerSession(getApplicationContext(), uploadItem);
			Logger.Log("Response from server: " + response);

			containerSession.setUploadState(UploadState.COMPLETED);
			Logger.Log("Upload successfully container " + containerSession.getContainerId() + " | " + uploadType.name());
			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	"#upload #successfully container " + containerSession.getContainerId() + " | "
												+ uploadType.name());

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

		try {

			// convert back then save containerSession
			Mapper.getInstance().update(getApplicationContext(), response, containerSession);

		} catch (SQLException e) {

			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	containerSession.getContainerId()
												+ " | #SQLerror when update #response data | Stack trace: "
												+ e.getMessage());
			e.printStackTrace();

		} catch (Exception e) {

			DataCenter.getDatabaseHelper(getApplicationContext())
						.addUsageLog(	containerSession.getContainerId()
												+ " | #error when update #response data | Stack trace: "
												+ e.getMessage());

			e.printStackTrace();
		}

		// containerSession.setUploadState(UploadState.COMPLETED);
		// Logger.Log("Upload successfully container " + containerSession.getContainerId() + " | " + uploadType.name());
		// DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog( "#upload #successfully container "
		// + containerSession.getContainerId()
		// + " | " + uploadType.name());

		// Restore container upload state to NORMAL if upload_type = NONE (temporary upload at GateImport)
		synchronized (containerSession) {

			try {
				containerSessionDaoImpl.refresh(containerSession);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			switch (uploadType) {
				case NONE:
					if (isInterruptedByOfficialUpload) {

						Logger.Log("User trigger upload on GateImportFragment. It will be uploaded again.");
						containerSession.setUploadState(UploadState.WAITING);

					} else {

						// restore state
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

				case IN:
					containerSession.setOnLocal(false);
					try {
						containerSessionDaoImpl.update(containerSession);
					} catch (SQLException e) {
						e.printStackTrace();
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

		mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		EventBus.getDefault().register(this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {

		try {
			stopForeground(true);
			finishedNotification();
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

		if (uploadState == UploadState.NONE) {
			try {
				containerSessionDaoImpl.update(containerSession);
			} catch (SQLException e) {
				Logger.Log("Error when rolling back container " + containerSession.getContainerId());
				e.printStackTrace();
			}
			return;
		}

		try {

			if (!TextUtils.isEmpty(containerSession.getUuid())) {
				containerSessionDaoImpl.updateRaw("UPDATE container_session SET state = "
						+ containerSession.getUploadState() + " WHERE _id LIKE "
						+ Utils.sqlString(containerSession.getUuid()));

				containerSessionDaoImpl.refresh(containerSession);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.e("Cannot update state for container " + containerSession.getContainerId());
			Logger.e("Current state " + Integer.toString(containerSession.getUploadState()));
		}

		switch (uploadState) {

			case IN_PROGRESS:
				updateNotification(containerSession);
				break;

			case COMPLETED:
				mNumberUploaded++;
				uploadedContainer.add(containerSession.getContainerId());
				// Fall through

			case ERROR:
				// get Next Item if needed
				break;

			case WAITING:
				break;

			default:
				break;
		}

		// use to update container session data in CameraActivity
		EventBus.getDefault().post(new ContainerSessionUpdatedEvent(containerSession));
	}

	/**
	 * Main method, it called from QueueIntentService
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		if (NetworkHelper.isConnected(this)) {

			try {

				// rollback stuck containers
				DataCenter.getInstance().rollbackStuckContainers(this);
				ContainerSession containerSession = containerSessionDaoImpl.getNextWaiting(	this,
																							DataCenter.getDatabaseHelper(	this)
																										.getWritableDatabase());

				if (null != containerSession) {
					startForeground();
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

	private NotificationManager mNotificationMgr;
	private NotificationCompat.Builder mNotificationBuilder;
	private int mNumberUploaded = 0;

	private void startForeground() {

		if (null == mNotificationBuilder) {
			mNotificationBuilder = new NotificationCompat.Builder(this);
			mNotificationBuilder.setSmallIcon(R.drawable.ic_app_small);
			mNotificationBuilder.setContentTitle(getString(R.string.app_name));
			mNotificationBuilder.setOngoing(true);
			mNotificationBuilder.setWhen(System.currentTimeMillis());

			Intent intent = null;
			try {
				intent = new Intent(this, Utils.getHomeActivity(this));
			} catch (NullSessionException e) {
				e.printStackTrace();
			}

			if (intent != null) {
				intent.setAction(CJayConstant.INTENT_OPEN_TAB_UPLOAD);
				PendingIntent pendingintent = PendingIntent.getActivity(this, 2, intent,
																		PendingIntent.FLAG_UPDATE_CURRENT);
				mNotificationBuilder.setContentIntent(pendingintent);
			}

		}

		startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
	}

	void finishedNotification() {
		if (null != mNotificationBuilder) {
			String text = getResources().getQuantityString(R.plurals.notification_uploaded_container, mNumberUploaded,
															mNumberUploaded);

			mNotificationBuilder.setOngoing(false);
			mNotificationBuilder.setProgress(0, 0, false);
			mNotificationBuilder.setWhen(System.currentTimeMillis());
			mNotificationBuilder.setContentTitle(text);
			mNotificationBuilder.setTicker(text);
			mNotificationBuilder.setContentText(StringHelper.concatStringsWSep(uploadedContainer, ","));

			mNotificationMgr.notify(NOTIFICATION_ID, mNotificationBuilder.build());
		}
	}

	List<String> uploadedContainer = new ArrayList<String>();

	void updateNotification(final ContainerSession upload) {

		String text;

		switch (upload.getUploadState()) {

			case CJayImage.STATE_UPLOAD_WAITING:
				text = getString(R.string.notification_uploading_container, upload.getContainerId());

				mNotificationBuilder.setContentTitle(text);
				mNotificationBuilder.setTicker(text);
				mNotificationBuilder.setProgress(0, 0, true);
				mNotificationBuilder.setWhen(System.currentTimeMillis());
				break;

			case CJayImage.STATE_UPLOAD_IN_PROGRESS:

				if (upload.getUploadProgress() >= 0) {

					text = getString(R.string.notification_uploading_container_progress, upload.getContainerId());
					mNotificationBuilder.setContentTitle(text);
					mNotificationBuilder.setProgress(0, 0, true);

				}
				break;
		}

		mNotificationMgr.notify(NOTIFICATION_ID, mNotificationBuilder.build());
	}

	public void rollbackContainerState(ContainerSession containerSession) {

		UploadType uploadType = UploadType.values()[containerSession.getUploadType()];
		DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog(	"#rollback "
																					+ containerSession.getContainerId()
																					+ " | " + uploadType.name());

		Logger.w("Rolling back type: " + uploadType.name());
		switch (uploadType) {

			case IN:
				containerSession.setOnLocal(true);
				break;

			case OUT:
				DataCenter.getDatabaseHelper(getApplicationContext())
							.addUsageLog(	"#rollback " + containerSession.getContainerId()
													+ " | Set check_out_time to empty");
				containerSession.setCheckOutTime("");
				break;

			case AUDIT:
			case REPAIR:
			default:
				break;
		}

		containerSession.setUploadConfirmation(false);
		containerSession.setUploadState(UploadState.NONE);
	}
}
