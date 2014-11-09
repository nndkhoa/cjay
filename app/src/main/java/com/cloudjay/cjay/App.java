package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.Log;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.path.android.jobqueue.network.NetworkUtil;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class App extends Application {

	private static App instance;
	private static DB snappyDB = null;

	public App() {
		instance = this;
	}

	public static App getInstance() {
		return instance;
	}

	public static DB getDB(Context context) throws SnappydbException {

//		StackTraceElement[] trace = new Throwable().getStackTrace();
//		Logger.Log("Open DB " + trace[1].getFileName() + "#" + trace[1].getMethodName() + "() | Line: " + trace[1].getLineNumber());

		if (snappyDB == null || snappyDB.isOpen() == false) {
			snappyDB = DBFactory.open(context, CJayConstant.DB_NAME);
		}

		return snappyDB;
	}

	public static void closeDB() throws SnappydbException {

		if (snappyDB != null && snappyDB.isOpen()) {
//			StackTraceElement[] trace = new Throwable().getStackTrace();
//			Logger.Log("Close DB " + trace[1].getFileName() + "#" + trace[1].getMethodName() + "() | Line: " + trace[1].getLineNumber());
			snappyDB.close();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Crashlytics.start(this);

		try {
			closeDB();
		} catch (SnappydbException e) {
			e.printStackTrace();
		}

		configureDirectories();
		configureImageLoader();
		configureJobManager();
		configureAlarmManager();

//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
//				.detectLeakedClosableObjects().penaltyLog()
//				.penaltyDeath().build());

	}

	/**
	 * Configure Alarm Manager
	 */
	private void configureAlarmManager() {

		if (!Utils.isAlarmUp(getApplicationContext())) {
			
			Logger.w("Alarm Manager is not running. Starting alarm ...");
			Utils.startAlarm(getApplicationContext());

		} else {
			Logger.Log("Alarm is already running "
					+ StringUtils.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));

		}
	}

	/**
	 * Khởi tạo các folder mặc định
	 */
	private void configureDirectories() {

		// Init Default dirs
		if (!CJayConstant.BACK_UP_DIRECTORY_FILE.exists()) {
			CJayConstant.BACK_UP_DIRECTORY_FILE.mkdir();
		}

		if (!CJayConstant.LOG_DIRECTORY_FILE.exists()) {
			CJayConstant.LOG_DIRECTORY_FILE.mkdirs();
		}

		if (!CJayConstant.APP_DIRECTORY_FILE.exists()) {
			CJayConstant.APP_DIRECTORY_FILE.mkdir();
		}
	}

	/**
	 * Cấu hình Universal Image Loader
	 */
	private void configureImageLoader() {

		// init image loader default options
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.showImageForEmptyUri(R.drawable.ic_app_360)
				.showImageOnLoading(R.drawable.ic_app_360)
				.build();

		// init image loader config
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.discCacheSize(500 * 1024 * 1024)
				.memoryCache(new WeakMemoryCache())
				.threadPoolSize(3)
				.threadPriority(Thread.MAX_PRIORITY)
				.build();

		// .memoryCacheSize(40 * 1024 * 1024)

		// init image loader with config defined
		ImageLoader.getInstance().init(config);
	}

	/**
	 * Cấu hình Job Manager của JobQueue
	 */
	private void configureJobManager() {

		Configuration configuration = new Configuration.Builder(this)

				.networkUtil(new NetworkUtil() {
					@Override
					public boolean isConnected(Context context) {
						return Utils.canReachInternet();
					}
				})
				.customLogger(new CustomLogger() {
					private static final String TAG = "JOBS";

					@Override
					public boolean isDebugEnabled() {
						return true;
					}

					@Override
					public void d(String text, Object... args) {
						Log.d(TAG, String.format(text, args));
					}

					@Override
					public void e(Throwable t, String text, Object... args) {
						Log.e(TAG, String.format(text, args), t);
					}

					@Override
					public void e(String text, Object... args) {
						Log.e(TAG, String.format(text, args));
					}

					@Override
					public void i(String text, Object... args) {
						Log.i(TAG, String.format(text, args));
					}
				})
				.minConsumerCount(1)
				.maxConsumerCount(1)
				.build();

		jobManager = new JobManager(this, configuration);
	}

	private static JobManager jobManager;

	public static JobManager getJobManager() {
		return jobManager;
	}

}