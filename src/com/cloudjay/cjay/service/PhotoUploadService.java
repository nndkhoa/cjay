package com.cloudjay.cjay.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.androidannotations.annotations.EService;
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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.events.CJayImageUploadProgressChangedEvent;
import com.cloudjay.cjay.events.CJayImageUploadStateChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.tasks.PhotupThreadRunnable;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import de.greenrobot.event.EventBus;

@EService
public class PhotoUploadService extends Service {

	private class UpdateBigPictureStyleRunnable extends PhotupThreadRunnable {

		private final CJayImage mSelection;

		public UpdateBigPictureStyleRunnable(CJayImage selection) {
			mSelection = selection;
		}

		public void runImpl() {
			mSelection.setBigPictureNotificationBmp(PhotoUploadService.this,
													mSelection.processBitmap(	mSelection.getThumbnailImage(PhotoUploadService.this),
																				false, true));
			updateNotification(mSelection);
		}

	}

	public void onEvent(CJayImageUploadProgressChangedEvent event) {
		updateNotification(event.getTarget());
	}

	public synchronized void onEvent(CJayImageUploadStateChangedEvent event) {

		CJayImage upload = event.getTarget();

		try {

			Logger.Log("CJayImageUploadStateChangedEvent "
					+ (upload.getIssue() != null ? upload.getIssue().getUuid() : ""));

			if (!TextUtils.isEmpty(upload.getUuid())) {
				cJayImageDaoImpl.updateRaw("UPDATE cjay_image SET state = " + upload.getUploadState()
						+ " WHERE uuid LIKE " + Utils.sqlString(upload.getUuid()));
				cJayImageDaoImpl.refresh(upload);
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		switch (upload.getUploadState()) {
			case CJayImage.STATE_UPLOAD_IN_PROGRESS:
				updateNotification(upload);
				break;

			case CJayImage.STATE_UPLOAD_COMPLETED:
				mNumberUploaded++;
				// Fall through...

			case CJayImage.STATE_UPLOAD_ERROR:
				startNextUploadOrFinish();
				// Fall through...
				break;

			case CJayImage.STATE_UPLOAD_WAITING:
				// // NOTE: if call stopSelf() here, ImageQueue will loop forever
				break;
		}
	}

	private static class UploadPhotoRunnable extends PhotupThreadRunnable {

		private final WeakReference<Context> mContextRef;
		private final CJayImage mUpload;

		public UploadPhotoRunnable(Context context, CJayImage upload) {
			mContextRef = new WeakReference<Context>(context);
			mUpload = upload;
		}

		synchronized public void doFileUpload(Context ctx, final CJayImage uploadItem) {

			try {
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_IN_PROGRESS);

				if (uploadItem.getUri().startsWith("http")) {
					uploadItem.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);
					return;
				}

				// Try New Upload Method
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

				ParcelFileDescriptor fileDescriptor = ctx.getContentResolver()
															.openFileDescriptor(Uri.parse(uploadItem.getUri()), "r");
				InputStream in = ctx.getContentResolver().openInputStream(Uri.parse(uploadItem.getUri()));
				CountingInputStreamEntity entity = new CountingInputStreamEntity(in, fileDescriptor.getStatSize());
				entity.setUploadListener(new CountingInputStreamEntity.UploadListener() {

					@Override
					public void onChange(int percent) {
						uploadItem.setUploadProgress(percent);
					}

				});
				entity.setContentType("image/jpeg");
				post.setEntity(entity);

				if (isInterrupted()) {
					Logger.e("isInterrupted");
					return;
				}

				try {
					// Logger.Log("About to call httpClient.execute");
					Logger.Log("doFileUpload: " + uploadItem.getImageName());
					resp = httpClient.execute(post);

					Logger.Log(resp.getStatusLine().getReasonPhrase());
					if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
							|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {

						// Set Status Success
						uploadItem.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);

					} else {
						Logger.w("Screw up with http - " + resp.getStatusLine().getStatusCode());
						uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
					}

					resp.getEntity().consumeContent();

				} catch (ClientProtocolException e) {
					Logger.e("ClientProtocolException: " + e.getMessage());
					uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
				}
			} catch (IOException e) { // Rớt mạng

				Logger.e("IOException: " + e.getMessage());
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
				return;
			}
		}

		public void runImpl() {
			final Context context = mContextRef.get();
			if (null == context) { return; }
			doFileUpload(context, mUpload);
		}

		protected boolean isInterrupted() {

			if (super.isInterrupted()) {

				Logger.Log("7. CJayImage.STATE_UPLOAD_WAITING");
				mUpload.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
				return true;
			}

			return false;
		}
	}

	static final int MAX_NUMBER_RETRIES = 3;
	static final int NOTIFICATION_ID = 1000;

	private static boolean mCurrentlyUploading;
	private ExecutorService mExecutor;
	private int mNumberUploaded = 0;

	private NotificationManager mNotificationMgr;
	private NotificationCompat.Builder mNotificationBuilder;
	private NotificationCompat.BigPictureStyle mBigPicStyle;
	private Future<?> mCurrentUploadRunnable;
	private String mNotificationSubtitle;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static CJayImageDaoImpl cJayImageDaoImpl;

	@Override
	public void onCreate() {
		super.onCreate();

		EventBus.getDefault().register(this);
		try {
			cJayImageDaoImpl = DataCenter.getDatabaseHelper(this).getCJayImageDaoImpl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		CJayApplication app = CJayApplication.getApplication(this);
		mExecutor = app.getSingleThreadExecutorService();
		mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		setCurrentlyUploading(false);
	}

	@Override
	public void onDestroy() {
		setCurrentlyUploading(false);
		EventBus.getDefault().unregister(this);

		try {
			stopForeground(true);
			finishedNotification();
		} catch (Exception e) {
			e.printStackTrace();
			// Can sometimes call NPE
		}

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uploadAll();
		return super.onStartCommand(intent, flags, startId);
	}

	void finishedNotification() {
		if (null != mNotificationBuilder) {
			String text = getResources().getQuantityString(R.plurals.notification_uploaded_photo, mNumberUploaded,
															mNumberUploaded);

			mNotificationBuilder.setOngoing(false);
			mNotificationBuilder.setProgress(0, 0, false);
			mNotificationBuilder.setWhen(System.currentTimeMillis());
			mNotificationBuilder.setContentTitle(text);
			mNotificationBuilder.setTicker(text);

			mNotificationMgr.notify(NOTIFICATION_ID, mNotificationBuilder.build());
		}
	}

	void startNextUploadOrFinish() {
		CJayImage nextUpload = null;

		try {
			nextUpload = cJayImageDaoImpl.getNextWaiting(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (null != nextUpload && canUpload()) {
			Logger.Log("Start uploading next image: " + nextUpload.getImageName());
			startUpload(nextUpload);

		} else {

			Logger.Log("ImageQueue is empty. Stopped PhotoUploadService.");
			setCurrentlyUploading(false);
			stopSelf();
		}
	}

	void stopUploading() {

		if (null != mCurrentUploadRunnable) {
			mCurrentUploadRunnable.cancel(true);
		}

		setCurrentlyUploading(false);
		stopSelf();
	}

	void updateNotification(final CJayImage upload) {

		String text;

		if (VERSION.SDK_INT >= VERSION_CODES.BASE) {

			final Bitmap uploadBigPic = upload.getBigPictureNotificationBmp();
			if (null == uploadBigPic) {
				mExecutor.submit(new UpdateBigPictureStyleRunnable(upload));
			}
			mBigPicStyle.bigPicture(uploadBigPic);
		}

		switch (upload.getUploadState()) {
			case CJayImage.STATE_UPLOAD_WAITING:
				text = getString(	R.string.notification_uploading_photo, mNumberUploaded + 1,
									CJayImageDaoImpl.totalNumber + mNumberUploaded);
				mNotificationBuilder.setContentTitle(text);
				mNotificationBuilder.setTicker(text);
				mNotificationBuilder.setProgress(0, 0, true);
				mNotificationBuilder.setWhen(System.currentTimeMillis());
				break;

			case CJayImage.STATE_UPLOAD_IN_PROGRESS:

				if (upload.getUploadProgress() >= 0) {
					text = getString(	R.string.notification_uploading_photo_progress, mNumberUploaded + 1,
										upload.getUploadProgress(), CJayImageDaoImpl.totalNumber + mNumberUploaded);
					mNotificationBuilder.setContentTitle(text);
					mNotificationBuilder.setProgress(100, upload.getUploadProgress(), false);
				}
				break;
		}

		mBigPicStyle.setSummaryText(mNotificationSubtitle);

		mNotificationMgr.notify(NOTIFICATION_ID, mBigPicStyle.build());
	}

	private boolean canUpload() {
		return NetworkHelper.isConnected(this);
	}

	private void startForeground() {
		if (null == mNotificationBuilder) {
			mNotificationBuilder = new NotificationCompat.Builder(this);
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_upload);
			mNotificationBuilder.setContentTitle(getString(R.string.app_name));
			mNotificationBuilder.setOngoing(true);
			mNotificationBuilder.setWhen(System.currentTimeMillis());

			// PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, PhotoSelectionActivity.class),
			// 0);
			// mNotificationBuilder.setContentIntent(intent);
		}

		if (null == mBigPicStyle) {
			mBigPicStyle = new NotificationCompat.BigPictureStyle(mNotificationBuilder);
		}

		startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
	}

	private void startUpload(CJayImage upload) {

		trimCache();
		updateNotification(upload);
		mCurrentUploadRunnable = mExecutor.submit(new UploadPhotoRunnable(this, upload));
		setCurrentlyUploading(true);

	}

	private void trimCache() {
		CJayApplication.getApplication(this).getImageCache().trimMemory();
	}

	private boolean uploadAll() {

		// If we're currently uploading, ignore call
		if (isCurrentlyUploading()) { return true; }

		if (canUpload()) {

			// rollback stuck images
			DataCenter.getInstance().rollbackStuckImages(this);

			CJayImage uploadItem = null;
			try {
				uploadItem = cJayImageDaoImpl.getNextWaiting(this);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (uploadItem != null) {

				Logger.Log("Begin to upload cjay images.");
				startForeground();
				startUpload(uploadItem);
				return true;
			}
		}

		// If we reach here, there's no need to keep us running
		setCurrentlyUploading(false);
		stopSelf();

		return false;
	}

	public static boolean isCurrentlyUploading() {
		return mCurrentlyUploading;
	}

	public void setCurrentlyUploading(boolean mCurrentlyUploading) {
		PhotoUploadService.mCurrentlyUploading = mCurrentlyUploading;
	}

}
