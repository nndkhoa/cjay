package com.cloudjay.cjay;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import uk.co.senab.bitmapcache.BitmapLruCache;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cloudjay.cjay.model.DatabaseManager;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.network.HttpRequestWrapper;
import com.cloudjay.cjay.network.IHttpRequestWrapper;
import com.cloudjay.cjay.receivers.InstantUploadReceiver;
import com.cloudjay.cjay.service.*;
import com.cloudjay.cjay.task.PhotupThreadFactory;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Flags;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

@ReportsCrashes(formKey = "", formUri = "https://cloudjay-web.appspot.com/acra/", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast)
public class CJayApplication extends Application {

	private final String LOG_TAG = "CJayApplication";
	static final float EXECUTOR_POOL_SIZE_PER_CORE = 1.5f;
	public static final String THREAD_FILTERS = "filters_thread";

	private BitmapLruCache mImageCache;

	IDatabaseManager databaseManager = null;
	IHttpRequestWrapper httpRequestWrapper = null;

	private ExecutorService mDatabaseThreadExecutor, mMultiThreadExecutor;

	public ExecutorService getMultiThreadExecutorService() {
		if (null == mMultiThreadExecutor || mMultiThreadExecutor.isShutdown()) {
			final int numThreads = Math.round(Runtime.getRuntime()
					.availableProcessors() * EXECUTOR_POOL_SIZE_PER_CORE);
			mMultiThreadExecutor = Executors.newFixedThreadPool(numThreads,
					new PhotupThreadFactory());

			if (Flags.DEBUG) {
				Log.d(LOG_TAG, "MultiThreadExecutor created with " + numThreads
						+ " threads");
			}
		}
		return mMultiThreadExecutor;
	}

	public ExecutorService getDatabaseThreadExecutorService() {

		if (null == mDatabaseThreadExecutor
				|| mDatabaseThreadExecutor.isShutdown()) {
			mDatabaseThreadExecutor = Executors
					.newSingleThreadExecutor(new PhotupThreadFactory());
		}

		return mDatabaseThreadExecutor;
	}

	public static CJayApplication getApplication(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}

	public static void startCJayHomeActivity(Context context) {
		int userRole = ((CJayActivity) context).getCurrentUser().getRole();

		Intent intent = null;
		switch (userRole) {
		case 1: // Giám định
			intent = new Intent(context, AuditorHomeActivity_.class);
			break;

		case 4: // Sửa chữa
			intent = new Intent(context, RepairHomeActivity_.class);
			break;

		case 6: // Cổng
		default:
			intent = new Intent(context, GateHomeActivity_.class);
			break;
		}
		context.startActivity(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		databaseManager = new DatabaseManager();
		httpRequestWrapper = new HttpRequestWrapper();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				// .displayer(new FadeInBitmapDisplayer(300))
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.memoryCacheSize(41943040).discCacheSize(104857600)
				.memoryCache(new WeakMemoryCache()).threadPoolSize(10)
				.threadPriority(Thread.MAX_PRIORITY).build();

		ACRA.init(CJayApplication.this);
		ACRA.getErrorReporter().checkReportsOnApplicationStart();

		ImageLoader.getInstance().init(config);
		CJayClient.getInstance().init(httpRequestWrapper, databaseManager);
		DataCenter.getInstance().initialize(databaseManager);
		databaseManager.getHelper(getApplicationContext());

		if (!CJayConstant.APP_DIRECTORY_FILE.exists())
			CJayConstant.APP_DIRECTORY_FILE.mkdir();

		if (!CJayConstant.HIDDEN_APP_DIRECTORY_FILE.exists())
			CJayConstant.HIDDEN_APP_DIRECTORY_FILE.mkdir();

		// Configure Logger
		Logger.PRODUCTION_MODE = false;

		// Making Alarm for Queue Worker
		Intent intent = new Intent(this, QueueIntentService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar current = Calendar.getInstance();
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		// Start every 10 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, current.getTimeInMillis(),
				10 * 1000, pintent);

		// // TEST
		// String resp =
		// "[{\"id\": 54, \"container_id\": \"UDJEU\", \"image_id_path\": \"http://storage-cjay.cloudjay.com/phuoc_long_icd_test-2014-49-05-gate-in-b5b89f5b-0dcf-4010-9b80-99e7bf02fdd6.jpg\", \"operator_code\": \"CAI\", \"operator_name\": \"CAI (CAI)\", \"check_in_time\": \"2014-01-05T03:50:15\", \"check_out_time\": null, \"depot_code\": \"phuoc_long_icd_test\", \"gate_report_images\": [{\"id\": 190, \"type\": 0, \"time_posted\": \"2014-01-05T03:50:15\", \"image_name\": \"http://storage-cjay.cloudjay.com/phuoc_long_icd_test-2014-49-05-gate-in-b5b89f5b-0dcf-4010-9b80-99e7bf02fdd6.jpg\"}, {\"id\": 191, \"type\": 0, \"time_posted\": \"2014-01-05T03:50:15\", \"image_name\": \"http://storage-cjay.cloudjay.com/phuoc_long_icd_test-2014-49-05-gate-in-bd38639d-35ae-4dbe-ae8a-4a106035f820.jpg\"}, {\"id\": 192, \"type\": 0, \"time_posted\": \"2014-01-05T03:50:15\", \"image_name\": \"http://storage-cjay.cloudjay.com/phuoc_long_icd_test-2014-49-05-gate-in-695960ee-3582-44a1-b6e4-b5925b34f457.jpg\"}], \"status\": 0}]";
		// Gson gson = new GsonBuilder().setDateFormat(
		// CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();
		//
		// Type listType = new TypeToken<List<TmpContainerSession>>() {
		// }.getType();
		//
		// List<TmpContainerSession> tmpContainerSessions = null;
		// try {
		// tmpContainerSessions = gson.fromJson(resp, listType);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// if (null != tmpContainerSessions) {
		// TmpContainerSession tmp = tmpContainerSessions.get(0);
		//
		// }

	}

	public void checkInstantUploadReceiverState() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final boolean enabled = prefs.getBoolean(
				CJayConstant.PREF_INSTANT_UPLOAD_ENABLED, false);

		final ComponentName component = new ComponentName(this,
				InstantUploadReceiver.class);
		final PackageManager pkgMgr = getPackageManager();

		switch (pkgMgr.getComponentEnabledSetting(component)) {
		case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
			if (enabled) {
				pkgMgr.setComponentEnabledSetting(component,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP);
				if (Flags.DEBUG) {
					Log.d(LOG_TAG, "Enabled Instant Upload Receiver");
				}
			}
			break;

		case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
		case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
			if (!enabled) {
				pkgMgr.setComponentEnabledSetting(component,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
				if (Flags.DEBUG) {
					Log.d(LOG_TAG, "Disabled Instant Upload Receiver");
				}
			}
			break;
		}
	}

	@SuppressWarnings("deprecation")
	public int getSmallestScreenDimension() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		return Math.min(display.getHeight(), display.getWidth());
	}

	public BitmapLruCache getImageCache() {
		if (null == mImageCache) {
			mImageCache = new BitmapLruCache(this,
					CJayConstant.IMAGE_CACHE_HEAP_PERCENTAGE);
		}
		return mImageCache;
	}
}
