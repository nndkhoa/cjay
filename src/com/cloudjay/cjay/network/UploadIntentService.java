package com.cloudjay.cjay.network;


import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Random;

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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.UploadAlertDialogActivity;
import com.cloudjay.cjay.dao.UploadItem;
import com.cloudjay.cjay.dao.UploadItemDaoImpl;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.BitmapHelper;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.google.gson.Gson;
//import java.util.Calendar;
//import android.util.Log;
//import android.widget.Toast;

public class UploadIntentService extends IntentService implements CountingInputStreamEntity.UploadListener  {

	// Notification Upload Status
	private NotificationManager nm;
	NotificationCompat.Builder mBuilder;
	public static final int NOTIFICATION_ID = 0;
	PendingIntent alertIntent;
	//private final Calendar time = Calendar.getInstance();
	private String uuid;
	
	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String tmpImgUri = intent.getExtras().getString("tmpImgUri");
		String token = intent.getExtras().getString("token");
		uuid = intent.getExtras().getString("uuid");

		Intent newAlertIntent = new Intent(this,
				UploadAlertDialogActivity.class);
		newAlertIntent.putExtra("uuid", uuid);
		newAlertIntent.putExtra("tmpImgUri", tmpImgUri);

		Random randomGenerator = new Random();

		int requestCode = randomGenerator.nextInt();
		// Generate Request Code until not duplicate
		while (PendingIntent.getActivity(this, requestCode, newAlertIntent,
				PendingIntent.FLAG_NO_CREATE) != null) {
			requestCode = randomGenerator.nextInt();
		}
		alertIntent = PendingIntent.getActivity(this, requestCode,
				newAlertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		doFileUpload(tmpImgUri, token);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("Picture Uploading")
				.setContentText("Upload in Progress")
				.setOngoing(true);

//		Toast.makeText(this, "Service created at " + time.getTime(),
//				Toast.LENGTH_LONG).show();
		// showNotification();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Cancel the persistent notification.
		// nm.cancel(R.string.service_started);
//		Toast.makeText(this, " Service destroyed at " + time.getTime() + ";",
//				Toast.LENGTH_LONG).show();
	}

	
	int increment = 10;
	int targetProgressBar = 0;
	
	private UploadItemDaoImpl uploadList;
	@Override
	public void onChange(int progress) {
		
		if (targetProgressBar <= progress) {					
			//Displays the progress bar for the first time.
			UploadItem uploadItem;
			try {
				uploadItem = uploadList.findByUUID(uuid);
				if (uploadItem.isNoteStatus()) {
					mBuilder.setProgress(100, progress, false);
					nm.notify(uuid, NOTIFICATION_ID, mBuilder.build());
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			targetProgressBar += increment;					
		}
	}
	
	public void doFileUpload(String tmpImgUri, String token) {

		try {			
			// Try New Upload Method
			
			uploadList = CJayClient.getInstance()
					.getDatabaseManager().getHelper(getApplicationContext())
					.getUploadItemImpl();
			
			UploadItem uploadItem = uploadList.findByUUID(uuid);
			// Set Status to Uploading
			uploadItem.setUploadStatus(0);

			uploadList.update(uploadItem);

			// From URI load bytes
			InputStream iStream = getContentResolver().openInputStream(
					Uri.parse(tmpImgUri));
			byte[] bytes = BitmapHelper.getBytes(iStream);

			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
					bytes.length);
			mBuilder.setLargeIcon(bitmap);
			// Get Google Cloud Token then Upload to Google Cloud Storage
			String googleToken = CJayClient.getInstance()
					.getGoogleCloudToken(token);
			String uploadUrl = String.format("https://www.googleapis.com/upload/storage/v1beta2/b/cjaytmp/o?uploadType=media&name=%s", uploadItem.getImageURL());
			
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
			post.addHeader("Authorization", String.format("Bearer %s", googleToken));
			post.addHeader("Content-Type", "image/jpeg");
			
			
			ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(Uri.parse(tmpImgUri), "r");
			InputStream in = getContentResolver().openInputStream(Uri.parse(tmpImgUri));
			CountingInputStreamEntity entity = new CountingInputStreamEntity(in, fileDescriptor.getStatSize());
			entity.setUploadListener(UploadIntentService.this);
			entity.setContentType("image/jpeg");
			post.setEntity(entity);
			
			try {
				Log.i("FOO", "About to call httpClient.execute");
				resp = httpClient.execute(post);
				Log.i("FOO", resp.getStatusLine().getReasonPhrase());				
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK || resp.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {					
					// Set Status Success
					uploadItem = uploadList.findByUUID(uuid);
					uploadItem.setUploadStatus(1);
					uploadList.update(uploadItem);

					// If Note has done first, upload the data
					if (uploadItem.isNoteStatus()) {
						try {			
							mBuilder.setContentText(getResources().getString(R.string.upload_completing_msg))
							.setContentTitle(getResources().getString(R.string.upload_completing_title))
							// Removes the progress bar
							.setProgress(0, 0, false).setOngoing(true);

							nm.notify(uuid, 0, mBuilder.build());
														
							Gson gson = new Gson();
							TmpContainerSession jaypixItem = (TmpContainerSession) gson
									.fromJson(uploadItem.getJsonPostStr(),
											TmpContainerSession.class);
							CJayClient.getInstance().uploadItem(
									getApplicationContext(), jaypixItem);
							
							mBuilder.setContentTitle(getResources().getString(R.string.upload_complete_title))
									.setContentText(getResources().getString(R.string.upload_complete_msg))
									// Removes the progress bar
									.setProgress(0, 0, false).setOngoing(false);
		
							nm.notify(uuid, 0, mBuilder.build());
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					Log.i("FOO", "Screw up with http - " + resp.getStatusLine().getStatusCode());
				}
				resp.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {				
			// Set Status Failed
			
			UploadItem uploadItem;
			try {
				uploadItem = uploadList.findByUUID(uuid);
				uploadItem.setUploadStatus(-1);
				uploadList.update(uploadItem);
				
				mBuilder.setContentIntent(alertIntent);
				
				mBuilder.setContentText(getResources().getString(R.string.upload_failed_msg))
						.setContentTitle(getResources().getString(R.string.upload_failed_title))
						// Removes the progress bar
						.setProgress(0, 0, false).setOngoing(true);

				nm.notify(uuid, NOTIFICATION_ID, mBuilder.build());
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
