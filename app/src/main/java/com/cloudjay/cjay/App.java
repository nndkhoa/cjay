package com.cloudjay.cjay;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cloudjay.cjay.api.ApiEndpoint;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.NotificationItem;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.service.PubnubService_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.crashlytics.android.Crashlytics;
import com.esotericsoftware.kryo.Kryo;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.path.android.jobqueue.network.NetworkUtil;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class App extends Application {

    private static App instance;
    private static DB snappyDB = null;

    private static boolean defaultBetaApiVal = false;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }


//		StackTraceElement[] trace = new Throwable().getStackTrace();
//		Logger.Log("Open DB " + trace[1].getFileName() + "#" + trace[1].getMethodName() + "() | Line: " + trace[1].getLineNumber());

    public static DB getDB(Context context) throws SnappydbException {

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

        ApiEndpoint.initBetaApi(defaultBetaApiVal);

        try {
            closeDB();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        try {
            Kryo kryo = getDB(getApplicationContext()).getKryoInstance();
            kryo.register(Session.class);
            kryo.register(AuditItem.class);
            kryo.register(AuditImage.class);
            kryo.register(GateImage.class);
            kryo.register(UploadObject.class);
            kryo.register(Operator.class);
            kryo.register(IsoCode.class);
            kryo.register(NotificationItem.class);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        configureDirectories();
        configureImageLoader();
        configureJobManager();
        configureAlarmManager();
        configureSettings();
//        generateSessionLogTextFile();

        // Init DB Flow
        FlowManager.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Destroy DB Flow
        FlowManager.destroy();
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

            if (!Utils.isRunning(getApplicationContext(), PubnubService_.class.getName())) {

                Logger.w("PubnubService is not Running");
                Intent pubnubIntent = new Intent(getApplicationContext(), PubnubService_.class);
                startService(pubnubIntent);
            }
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
                .loadFactor(0)
                .build();

        jobManager = new JobManager(this, configuration);
    }

    /**
     * Cấu hình cho Setting
     */
    private void configureSettings() {
        boolean defaultDebug = true;
        boolean defaultAutoUpdate = true;
        boolean defaultRainyMode = false;

        // Configure Logger
        boolean debuggable = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_logger_checkbox),
                        defaultDebug);
        Logger.getInstance().setDebuggable(debuggable);

        // Configure AutoUpdate
        boolean autoUpdate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_logger_checkbox),
                        defaultAutoUpdate);

        // Configure rainy mode
        boolean rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
                        defaultRainyMode);

        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean(getString(R.string.pref_key_enable_logger_checkbox), debuggable);
        editor.putBoolean(getString(R.string.pref_key_auto_check_update_checkbox), autoUpdate);
        editor.putBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox), rainyMode);
        editor.commit();


    }

    private static JobManager jobManager;

    public static JobManager getJobManager() {
        return jobManager;
    }

//    public void generateSessionLogTextFile() {
//
//        // create today String
//        String today = StringUtils.getCurrentTimestamp(CJayConstant.DAY_FORMAT);
//        String fileName ="cjay-log-" + today + ".txt";
//        File logFile =
//                new File(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_DOWNLOADS), fileName);
//        if (!logFile.exists()) {
//            try {
//                logFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
