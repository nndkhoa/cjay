package com.cloudjay.cjay.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.Trace;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
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
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.UploadType;

import de.greenrobot.event.EventBus;

@EIntentService
public class UploadIntentService extends IntentService implements CountingInputStreamEntity.UploadListener {

	static final int NOTIFICATION_ID = 1000;
	private CJayImageDaoImpl cJayImageDaoImpl;
	private ContainerSessionDaoImpl containerSessionDaoImpl;

	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Trace(level = Log.INFO)
	void doFileUpload(CJayImage uploadItem) {

		Logger.Log("doFileUpload: " + uploadItem.getImageName());

		try {
			// Try New Upload Method
			cJayImageDaoImpl.refresh(uploadItem);
			uploadItem.setUploadState(CJayImage.STATE_UPLOAD_IN_PROGRESS);
			// Set Status to Uploading
			cJayImageDaoImpl.update(uploadItem);

			String uploadUrl = String.format(CJayConstant.CJAY_TMP_STORAGE, uploadItem.getImageName());

			final HttpResponse resp;

			// SSL Enable
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			DefaultHttpClient client = new DefaultHttpClient();

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
			DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			HttpPost post = new HttpPost(uploadUrl);
			post.addHeader("Content-Type", "image/jpeg");

			ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(	Uri.parse(uploadItem.getUri()),
																							"r");
			InputStream in = getContentResolver().openInputStream(Uri.parse(uploadItem.getUri()));
			CountingInputStreamEntity entity = new CountingInputStreamEntity(in, fileDescriptor.getStatSize());
			entity.setUploadListener(UploadIntentService.this);
			entity.setContentType("image/jpeg");

			post.setEntity(entity);

			try {
				Logger.i("About to call httpClient.execute");
				resp = httpClient.execute(post);

				Logger.i(resp.getStatusLine().getReasonPhrase());
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
						|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {

					// Set Status Success
					cJayImageDaoImpl.refresh(uploadItem);
					uploadItem.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);
					cJayImageDaoImpl.update(uploadItem);
				} else {
					Log.i("FOO", "Screw up with http - " + resp.getStatusLine().getStatusCode());
				}
				resp.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {

			// Set Status to Uploading
			try {
				// THIS IS SQL ERROR --> NO REPEAT
				cJayImageDaoImpl.refresh(uploadItem);
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
				cJayImageDaoImpl.update(uploadItem);

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			e.printStackTrace();

		} catch (IOException e) {

			// Set Status to Uploading
			try {
				cJayImageDaoImpl.refresh(uploadItem);
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
				cJayImageDaoImpl.update(uploadItem);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
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
			uploadItem = Mapper.getInstance().toTmpContainerSession(containerSession, getApplicationContext());

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

		// convert back then save containerSession
		Mapper.getInstance().update(getApplicationContext(), response, containerSession);

		containerSession.setUploadState(UploadState.COMPLETED);

		UploadType uploadType = UploadType.values()[containerSession.getUploadType()];

		Logger.Log("Upload successfully container " + containerSession.getContainerId() + " | " + uploadType.name());
		DataCenter.getDatabaseHelper(getApplicationContext()).addUsageLog(	"#upload #successfully container "
																					+ containerSession.getContainerId()
																					+ " | " + uploadType.name());

		// Restore container upload state to NORMAL if upload_type = NONE (temporary upload at GateImport)
		synchronized (containerSession) {

			if (uploadType == UploadType.NONE) {

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

			} else {
				Logger.e("Cannot restore container upload state");
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

		Logger.Log("onEvent UploadStateChangedEvent");
		ContainerSession containerSession = event.getContainerSession();
		UploadState uploadState = UploadState.values()[containerSession.getUploadState()];

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
		// Logger.Log("onHandleIntent");

		if (NetworkHelper.isConnected(getApplicationContext())) {
			try {
				CJayImage uploadItem = cJayImageDaoImpl.getNextWaiting();

				if (uploadItem != null) {
					doFileUpload(uploadItem);
				}

				// It will return container which `upload confirmation = true`
				ContainerSession containerSession = containerSessionDaoImpl.getNextWaiting(DataCenter.getDatabaseHelper(this)
																										.getWritableDatabase());

				if (null != containerSession) {
					doUploadContainer(containerSession);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
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
