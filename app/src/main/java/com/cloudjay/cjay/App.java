package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.path.android.jobqueue.JobManager;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.androidannotations.api.view.OnViewChangedNotifier;


public class App extends Application {

	private static App instance;
	private static JobManager jobManager;
    private static DB snappydb;

	public App() {
		instance = this;
	}

	public static App getInstance() {
		return instance;
	}

	public static JobManager getJobManager(Context context) {
		if (jobManager == null) {
			OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(null);
			jobManager = new JobManager(context);
			OnViewChangedNotifier.replaceNotifier(previousNotifier);
		}
		return jobManager;
	}
    public static DB getSnappyDB(Context context) throws SnappydbException {
        if (snappydb == null) {
            OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(null);
            snappydb = DBFactory.open(context,"Thai");
            OnViewChangedNotifier.replaceNotifier(previousNotifier);
        }
        return snappydb;
    }

	@Override
	public void onCreate() {
		super.onCreate();

		// init image loader default options
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.showImageForEmptyUri(R.drawable.ic_app_360)
				.build();

		// init image loader config
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.discCacheSize(500 * 1024 * 1024)
				.memoryCache(new WeakMemoryCache())
				.threadPoolSize(3)
				.threadPriority(Thread.MAX_PRIORITY)
				.build();

		// init image loader with config defined
		ImageLoader.getInstance().init(config);

		//		Crashlytics.start(this);
	}
}
