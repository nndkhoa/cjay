package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.Log;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class App extends Application {

    private static App instance;
    private static JobManager jobManager;
    private static DB snappyDB;
    Context mContext;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public static DB getDB(Context context) throws SnappydbException {
        snappyDB = DBFactory.open(context, CJayConstant.DB_NAME);
        return snappyDB;
    }

    public static void closeDB() throws SnappydbException {
        if (snappyDB != null & snappyDB.isOpen()) {
            Logger.Log("Closing database ... ");
            snappyDB.close();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configureDirectories();
        configureImageLoader();
        configureJobManager();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects().penaltyLog()
                .penaltyDeath().build());

        // Configure Alarm Manager
        mContext = getApplicationContext();
        if (!Utils.isAlarmUp(mContext)) {

            Logger.w("Alarm Manager is not running.");
            Utils.startAlarm(mContext);

        } else {

            Logger.Log("Alarm is already running "
                    + StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));

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
    }

    /**
     * Cấu hình Job Manager của JobQueue
     */
    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
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
                })
                .minConsumerCount(1)
                .maxConsumerCount(3)
                .loadFactor(3)
                .consumerKeepAlive(120)
                .build();

        jobManager = new JobManager(this, configuration);
    }

    public static JobManager getJobManager() {
        return jobManager;
    }
}