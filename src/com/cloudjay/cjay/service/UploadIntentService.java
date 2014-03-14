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
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionUploadedEvent;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.Flags;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.NoConnectionException;

import de.greenrobot.event.EventBus;

@EIntentService
public class UploadIntentService extends IntentService implements
		CountingInputStreamEntity.UploadListener {

	int increment = 10;
	int targetProgressBar = 0;
	static final int NOTIFICATION_ID = 1000;

	private String mNotificationSubtitle;
	private int mNumberUploaded = 0;

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

	public void onEventMainThread(UploadStateChangedEvent event) {
		ContainerSession upload = event.getContainerSession();

		switch (upload.getUploadState()) {
		case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			break;

		case ContainerSession.STATE_UPLOAD_COMPLETED:
			mNumberUploaded++;
			// Fall through...

			// case ContainerSession.STATE_UPLOAD_ERROR:
			// startNextUploadOrFinish();
			// // Fall through...

		case ContainerSession.STATE_UPLOAD_WAITING:
			if (Flags.ENABLE_DB_PERSISTENCE) {
				try {
					Logger.Log("onEventMainThread(UploadStateChangedEvent event)");
					containerSessionDaoImpl.update(upload);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	/**
	 * Upload ContainerSession to server
	 * 
	 * @param containerSession
	 */
	@Trace(level = Log.INFO)
	synchronized void doUploadContainer(ContainerSession containerSession) {

		Logger.Log("doUploadContainer: " + containerSession.getContainerId());

		try {
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_IN_PROGRESS);
			containerSessionDaoImpl.update(containerSession);

			// Convert ContainerSession to TmpContainerSession for uploading
			TmpContainerSession uploadItem = Mapper.getInstance()
					.toTmpContainerSession(containerSession,
							getApplicationContext());

			// Post to Server and notify event to UploadFragment
			Logger.Log("Ready to post Container Session");
			String returnJson = CJayClient.getInstance().postContainerSession(
					getApplicationContext(), uploadItem);

			Logger.Log(returnJson);

			// convert back then save containerSession
			Mapper.getInstance().update(getApplicationContext(), returnJson,
					containerSession);

			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_COMPLETED);
			containerSessionDaoImpl.update(containerSession);

			EventBus.getDefault().post(
					new ContainerSessionUploadedEvent(containerSession));

			return;
		} catch (SQLException e) {
			e.printStackTrace();
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
		} catch (NoConnectionException e) {
			// Turn off alarm manager
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
		} catch (Exception e) {
			e.printStackTrace();
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
		}

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

		super.onCreate();
	}

	@Override
	public void onDestroy() {

		try {
			stopForeground(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
