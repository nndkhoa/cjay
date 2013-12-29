package com.cloudjay.cjay.network;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

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

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.Mapper;

public class UploadIntentService extends IntentService implements
		CountingInputStreamEntity.UploadListener {

	int increment = 10;
	int targetProgressBar = 0;

	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager()
					.getHelper(getApplicationContext()).getCJayImageDaoImpl();
			CJayImage uploadItem = cJayImageDaoImpl.getNextWaiting();

			if (uploadItem != null) {
				doFileUpload(uploadItem);
			}

			containerSessionDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(getApplicationContext())
					.getContainerSessionDaoImpl();

			ContainerSession containerSession = containerSessionDaoImpl
					.getNextWaiting();

			if (null != containerSession)
				doUploadContainer(containerSession);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Upload ContainerSession to server
	 * 
	 * @param containerSession
	 */
	private void doUploadContainer(ContainerSession containerSession) {
		containerSession
				.setUploadState(ContainerSession.STATE_UPLOAD_IN_PROGRESS);

		// Convert ContainerSession to TmpContainerSession for uploading
		TmpContainerSession uploadItem = Mapper.toTmpContainerSession(
				containerSession, getApplicationContext());

		// Post to Server and notify event to UploadFragment
		CJayClient.getInstance()
				.postContainerSession(getApplicationContext(), uploadItem);

	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private CJayImageDaoImpl cJayImageDaoImpl;
	private ContainerSessionDaoImpl containerSessionDaoImpl;

	@Override
	public void onChange(int progress) {
	}

	private void doFileUpload(CJayImage uploadItem) {
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
				Log.i("FOO", "About to call httpClient.execute");
				resp = httpClient.execute(post);
				Log.i("FOO", resp.getStatusLine().getReasonPhrase());
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {

			// Set Status to Uploading
			try {
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
				cJayImageDaoImpl.update(uploadItem);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
