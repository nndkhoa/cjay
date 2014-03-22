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
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.MismatchDataException;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;

import de.greenrobot.event.EventBus;

@EIntentService
public class UploadIntentService extends IntentService implements
		CountingInputStreamEntity.UploadListener {

	static final int NOTIFICATION_ID = 1000;
	private CJayImageDaoImpl cJayImageDaoImpl;
	private ContainerSessionDaoImpl containerSessionDaoImpl;

	public UploadIntentService() {
		super("UploadIntentService");
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
				ContainerSession containerSession = containerSessionDaoImpl
						.getNextWaiting();

				if (null != containerSession) {
					doUploadContainer(containerSession);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// Use to clear item
	public void onEvent(UploadStateChangedEvent event) {

		Logger.e("onEvent UploadStateChangedEvent");
		ContainerSession containerSession = event.getContainerSession();

		switch (containerSession.getUploadState()) {
		case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			break;

		case ContainerSession.STATE_UPLOAD_COMPLETED:
		case ContainerSession.STATE_UPLOAD_ERROR:
		case ContainerSession.STATE_UPLOAD_WAITING:

			try {
				containerSessionDaoImpl.update(containerSession);
			} catch (SQLException e) {

				e.printStackTrace();
				Logger.e("Cannot update state for container "
						+ containerSession.getContainerId());
				Logger.e("Current state "
						+ Integer.toString(containerSession.getUploadState()));

			}
			break;
		}

		EventBus.getDefault().post(
				new ContainerSessionUpdatedEvent(containerSession));

	}

	public void rollbackContainerState(ContainerSession containerSession) {

		int type = containerSession.getUploadType();
		Logger.w("Rolling back type: " + Integer.toString(type));

		switch (type) {
		case ContainerSession.TYPE_IN:
			containerSession.setOnLocal(false);
			break;

		case ContainerSession.TYPE_OUT:
			containerSession.setCheckOutTime("");
			break;

		case ContainerSession.TYPE_AUDIT:
		case ContainerSession.TYPE_REPAIR:
		default:
			break;
		}

		containerSession.setUploadConfirmation(false);
		containerSession.setUploadState(ContainerSession.STATE_NONE);
	}

	/**
	 * Upload ContainerSession to server
	 * 
	 * @param containerSession
	 */
	@Trace(level = Log.INFO)
	synchronized void doUploadContainer(ContainerSession containerSession) {

		Logger.w("Uploading container: " + containerSession.getContainerId());
		String response = "";

		containerSession
				.setUploadState(ContainerSession.STATE_UPLOAD_IN_PROGRESS);

		try {
			containerSessionDaoImpl.update(containerSession);
		} catch (SQLException e) {
			Logger.e("Cannot change State to `IN PROGRESS`. Process will be stopped.");
			e.printStackTrace();
			return;
		}

		// Convert ContainerSession to TmpContainerSession for uploading
		TmpContainerSession uploadItem = Mapper.getInstance()
				.toTmpContainerSession(containerSession,
						getApplicationContext());

		try {
			response = CJayClient.getInstance().postContainerSession(
					getApplicationContext(), uploadItem);

		} catch (NoConnectionException e) {

			Logger.Log("No Internet Connection");
			rollbackContainerState(containerSession);

		} catch (NullSessionException e) {

			e.printStackTrace();
			rollbackContainerState(containerSession);

			// Log user out
			CJayApplication.logOutInstantly(getApplicationContext());
			onDestroy();

		} catch (MismatchDataException e) {

			e.printStackTrace();
			// Set state to Error
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_ERROR);

		} catch (Exception e) {
			rollbackContainerState(containerSession);
		}

		Logger.Log(response);

		// convert back then save containerSession
		Mapper.getInstance().update(getApplicationContext(), response,
				containerSession);

		containerSession
				.setUploadState(ContainerSession.STATE_UPLOAD_COMPLETED);

		// try {
		// containerSessionDaoImpl.update(containerSession);
		// } catch (SQLException e) {
		// Logger.e("Cannot change State to `COMPLETE`. Process will be stopped.");
		// e.printStackTrace();
		// return;
		// }
	}

	@Override
	public void onCreate() {

		try {
			if (null == containerSessionDaoImpl)
				containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getContainerSessionDaoImpl();

			if (null == cJayImageDaoImpl)
				cJayImageDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getCJayImageDaoImpl();

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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onChange(int progress) {
	}

	@Trace(level = Log.INFO)
	void doFileUpload(CJayImage uploadItem) {

		Logger.Log("doFileUpload: " + uploadItem.getImageName());

		try {
			// Try New Upload Method
			uploadItem.setUploadState(CJayImage.STATE_UPLOAD_IN_PROGRESS);
			// Set Status to Uploading
			cJayImageDaoImpl.update(uploadItem);

			String uploadUrl = String.format(CJayConstant.CJAY_TMP_STORAGE,
					uploadItem.getImageName());

			final HttpResponse resp;

			// SSL Enable
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			DefaultHttpClient client = new DefaultHttpClient();

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory
					.getSocketFactory();
			socketFactory
					.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(
					client.getParams(), registry);
			DefaultHttpClient httpClient = new DefaultHttpClient(mgr,
					client.getParams());

			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			HttpPost post = new HttpPost(uploadUrl);
			post.addHeader("Content-Type", "image/jpeg");

			ParcelFileDescriptor fileDescriptor = getContentResolver()
					.openFileDescriptor(Uri.parse(uploadItem.getUri()), "r");
			InputStream in = getContentResolver().openInputStream(
					Uri.parse(uploadItem.getUri()));
			CountingInputStreamEntity entity = new CountingInputStreamEntity(
					in, fileDescriptor.getStatSize());
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
					uploadItem.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);
					cJayImageDaoImpl.update(uploadItem);
				} else {
					Log.i("FOO", "Screw up with http - "
							+ resp.getStatusLine().getStatusCode());
				}
				resp.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {

			// Set Status to Uploading
			try {
				// THIS IS SQL ERROR --> NO REPEAT
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
				cJayImageDaoImpl.update(uploadItem);

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			e.printStackTrace();

		} catch (IOException e) {

			// Set Status to Uploading
			try {
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
				cJayImageDaoImpl.update(uploadItem);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
