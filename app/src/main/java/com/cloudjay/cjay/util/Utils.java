package com.cloudjay.cjay.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.HomeActivity_;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.service.LogService;
import com.cloudjay.cjay.task.service.LogService_;
import com.cloudjay.cjay.task.service.PubnubService_;
import com.cloudjay.cjay.task.service.QueryService_;
import com.cloudjay.cjay.task.service.SyncIntentService_;
import com.cloudjay.cjay.task.service.UploadIntentService_;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.pubnub.api.Pubnub;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Utils {

    /**
     * Create notification
     *
     * @param context
     */
    public static void keepNotificationAlive(Context context) {

        String contentText;
        String tickerText;
        String contentTitle;

        Intent intent = new Intent(context, HomeActivity_.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Utils.canReachInternet()) {
            tickerText = "Connected to Internet";
            contentText = "Connected";
            contentTitle = "CJay Network";

        } else {
            tickerText = "Disconnected from Internet";
            contentText = "No connectivity";
            contentTitle = "CJay Network";
        }

        // Change notification message to connected
        Notification.Builder builder = new Notification.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_app_small))
                .setSmallIcon(R.drawable.ic_app_small).setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(contentTitle)
                .setOngoing(true)
                .setContentText(contentText)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CJayConstant.PERMANENT_NOTIFICATION_ID, builder.build());

    }

    /**
     * Log user out
     * 1. Unsubscribe all pubnub channel
     * 2. Clear all preference data
     * 3. Delete JobQueue DB
     * 4. Delete snappy DB
     *
     * @param context
     */
    public static void logOut(Context context) {

        // Unsubscribe channels pubnub
        Logger.Log("Unsubscribe all channels");
        Pubnub pubnub = new Pubnub(CJayConstant.PUBLISH_KEY, CJayConstant.SUBSCRIBE_KEY);
        pubnub.unsubscribeAllChannels();

        context.stopService(new Intent(context, PubnubService_.class));
        context.stopService(new Intent(context, QueryService_.class));
        context.stopService(new Intent(context, SyncIntentService_.class));
        context.stopService(new Intent(context, UploadIntentService_.class));

        // Clear preference and Database
        Logger.Log("Clear all preferences");
        PreferencesUtil.clearPrefs(context);
        context.deleteDatabase("db_default_job_manager.db");

        // Delete database
        Logger.Log("Delete snappy database");
        try {
            DB db = App.getDB(context);
            db.destroy();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check a intent service is running or not
     *
     * @param ctx
     * @param serviceName
     * @return
     */
    public static boolean isRunning(Context ctx, String serviceName) {

        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().equals(serviceName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initial alarm manager to trigger app services
     *
     * @param context
     */
    public static void startAlarm(Context context) {

        Logger.w(" -> start Alarm Manager");

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int i = calendar.get(Calendar.HOUR); // gets hour in 12h format

        // start 30 seconds after boot completed
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, (12 - i) * 3600);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Making Alarm for Sync Worker
        Intent intent = new Intent(context, SyncIntentService_.class);
        PendingIntent pSyncIntent = PendingIntent.getService(context, CJayConstant.ALARM_SYNC_SERVICE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Making Alarm for Log (02/01/2015)
        Intent logIntent = new Intent(context, LogService_.class);
        PendingIntent pLogIntent = PendingIntent.getService(context, CJayConstant.ALARM_SYNC_SERVICE_ID, logIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Start every 24 hours
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                CJayConstant.ALARM_INTERVAL * 1000, pSyncIntent);
        // Start get log from device (02/01/2015)
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                CJayConstant.ALARM_INTERVAL * 1000, pLogIntent);

        // --------
        // Configure Pubnub Service
        Logger.w(" --> set repeating for pubnub service");
        Intent pubnubIntent = new Intent(context, PubnubService_.class);
        PendingIntent pPubnubIntent = PendingIntent.getService(context, CJayConstant.ALARM_PUBNUB_SERVICE_ID, pubnubIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // wake up every 30 minutes to ensure service stays alive
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                (30 * 60 * 1000), pPubnubIntent);
        context.startService(new Intent(context, PubnubService_.class));


        // TODO: #tieubao
        // --------
        // Configure Upload Service
        Logger.w(" --> set repeating for upload service");
        Intent uploadIntent = new Intent(context, UploadIntentService_.class);
        PendingIntent pUploadIntent = PendingIntent.getService(context, CJayConstant.ALARM_UPLOAD_SERVICE_ID, uploadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // wake up every 5 minutes to ensure service stays alive
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                (30 * 60 * 1000), pUploadIntent);
    }

    /**
     * Use to check if alarm is up or not
     *
     * @param context
     * @return
     */
    public static boolean isAlarmUp(Context context) {

        Intent syncIntent = new Intent(context, SyncIntentService_.class);
        boolean syncUp = PendingIntent.getService(context, CJayConstant.ALARM_SYNC_SERVICE_ID, syncIntent, PendingIntent.FLAG_NO_CREATE) != null;

        Intent pubnubIntent = new Intent(context, PubnubService_.class);
        boolean pubnubUp = PendingIntent.getService(context, CJayConstant.ALARM_PUBNUB_SERVICE_ID, pubnubIntent, PendingIntent.FLAG_NO_CREATE) != null;
        boolean isSubscribed = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_SUBSCRIBE_PUBNUB, false);

        Intent uploadIntent = new Intent(context, UploadIntentService_.class);
        boolean uploadUp = PendingIntent.getService(context, CJayConstant.ALARM_UPLOAD_SERVICE_ID, uploadIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (!uploadUp)
            Logger.w("Upload Service is not running");

        if (!syncUp)
            Logger.w("Queue Service is not running");

        if (!pubnubUp)
            Logger.w("Pubnub Service is not running");

        if (syncUp && pubnubUp && isSubscribed && uploadUp)
            return true;
        else
            return false;
    }

    /**
     * Unregister Sync Intent Service from Alarm Manager
     *
     * @param context
     */
    public static void cancelAlarm(Context context) {
        Logger.Log("stop Alarm Manager");

        Intent intent = new Intent(context, SyncIntentService_.class);
        PendingIntent sender = PendingIntent.getService(context, CJayConstant.ALARM_SYNC_SERVICE_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    /**
     * Check if list intent service is running or not
     *
     * @param context
     * @param intent
     * @return
     */
    public static boolean isAlarmUp(Context context, Intent... intent) {

        if (intent != null) {
            for (Intent i : intent) {
                if (PendingIntent.getService(context, CJayConstant.ALARM_PUBNUB_SERVICE_ID, i, PendingIntent.FLAG_NO_CREATE) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Cancel alarm for given pending intent
     *
     * @param context
     * @param intent
     */
    public static void cancelAlarm(Context context, PendingIntent intent) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
        intent.cancel();

    }

    /**
     * Get app version name
     *
     * @param ctx
     * @return
     */
    public static String getAppVersionName(Context ctx) {

        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }

    /**
     * Display a pretty alert
     *
     * @param context
     * @param textResId
     */
    public static void showCrouton(Activity context, int textResId) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, textResId, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    /**
     * Display a pretty alert
     *
     * @param context
     * @param message
     */
    public static void showCrouton(Activity context, String message, Style style) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, message, style);
        crouton.setConfiguration(new Configuration.Builder()
                .setDuration(Configuration.DURATION_SHORT).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    /**
     * Display a pretty alert
     *
     * @param context
     * @param message
     */
    public static void showCrouton(Activity context, String message) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, message, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    /**
     * Return role of current user
     *
     * @param context
     * @return
     */
    public static int getRole(Context context) {
        return Integer.valueOf(PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_USER_ROLE));
    }

    /**
     * Check containerId is valid or not
     *
     * @param containerId
     * @return
     */
    public static boolean isContainerIdValid(String containerId) {

        //if (!Logger.isDebuggable()) {

        int crc = ContCheckDigit.getCRC(containerId);
        if (crc == 10) {
            crc = 0;
        }

        char lastChar = containerId.charAt(containerId.length() - 1);
        if (Character.getNumericValue(lastChar) == crc) {
            return true;
        } else {
            return false;
        }
        //}

        //return true;
    }

    /**
     * Check if container id is valid based on ISO 6346
     *
     * @param containerID
     * @return
     */
    public static boolean simpleValid(String containerID) {
        Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
        Matcher matcher = pattern.matcher(containerID);
        if (!matcher.matches()) return false;
        return true;
    }

    /**
     * Replace a null string by space
     *
     * @param in
     * @return
     */
    public static String replaceNullBySpace(String in) {
        return in == null || in.equals("") ? " " : in;
    }

    /**
     * Replace a null string by an empty string
     *
     * @param in
     * @return
     */
    public static String stripNull(String in) {
        return in == null ? "" : in;
    }

    /**
     * Count total image off session
     *
     * @param session
     * @return
     */
    public static int countTotalImage(Session session) {
        int totalImage = 0;
        List<AuditItem> auditItems = session.getAuditItems();
        if (auditItems != null) {
            for (AuditItem auditItem : auditItems) {
                totalImage = totalImage + auditItem.getAuditImages().size();
            }
            totalImage = totalImage + session.getGateImages().size();
            return totalImage;
        } else {
            totalImage = session.getGateImages().size();
            return totalImage;
        }
    }

    public static int countUploadedImage(Session session) {
        int uploadedImage = 0;
        List<AuditItem> auditItems = session.getAuditItems();
        if (auditItems != null) {
            for (AuditItem auditItem : auditItems) {
                List<AuditImage> auditImages = auditItem.getAuditImages();
                for (AuditImage auditImage : auditImages) {
                    if (auditImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
                        uploadedImage = uploadedImage + 1;
                    }
                }

            }
        }

        List<GateImage> gateImages = session.getGateImages();
        for (GateImage gateImage : gateImages) {
            if (gateImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
                uploadedImage = uploadedImage + 1;
            }
        }
        return uploadedImage;
    }

    public static String getImageTypeDescription(Context ctx, int type) {

        ImageType imageType = ImageType.values()[type];

        switch (imageType) {
            case IMPORT:
                return ctx.getResources().getString(R.string.image_type_description_import);

            case EXPORT:
                return ctx.getResources().getString(R.string.image_type_description_export);

            case AUDIT:
                return ctx.getResources().getString(R.string.image_type_description_report);

            case REPAIRED:
            default:
                return ctx.getResources().getString(R.string.image_type_description_repaired);
        }
    }

    /**
     * Cấu hình cho Text box theo chuẩn iso
     */
    public static void setupEditText(final EditText editText) {

        final Pattern pattern = Pattern.compile("^[a-zA-Z]{4}");

        // Cấu hình filter cho text box
        // Chỉ cho phép nhập chữ vào số
        InputFilter isLetterAndDigitFilter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        // Chỉ cho phép nhập 4 kí tự và 7 số
        InputFilter validCharacterFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                // dest là kết quả sau khi được append source character vào
                // source là kí tự được user nhập vào

                if (dest.length() > 10)
                    return "";


                if (dest.length() < 4) {
                    Pattern temp = Pattern.compile("^[a-zA-Z]");
                    Matcher matcher = temp.matcher(source);
                    if (!matcher.matches()) {
                        return "";
                    }
                    return source.toString().toUpperCase();
                } else {
                    Pattern temp = Pattern.compile("[0-9]$");
                    Matcher matcher = temp.matcher(source);
                    if (!matcher.matches()) {
                        return "";
                    }
                    return source.toString().toUpperCase();
                }
            }
        };

        // Set input filter for search text box
        editText.setFilters(new InputFilter[]{isLetterAndDigitFilter, validCharacterFilter});

        // Set keyboard type
        if (editText.getText().toString().length() > 4) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Matcher matcher = pattern.matcher(s);
                if (s.length() < 4) {
                    if (editText.getInputType() != InputType.TYPE_CLASS_TEXT) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    }
                } else if (matcher.matches()) {
                    if (editText.getInputType() != InputType.TYPE_CLASS_NUMBER) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static String parseUrltoUri(String url) {
        if (url.contains("file://")) {
            String uri = url.substring(7);
            return uri;
        }
        return null;
    }

    public static String getImageNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String name = url.substring(url.lastIndexOf("/") + 1, url.length());
        return name;
    }

    public static String getUuidFromImageName(String imageName) {
        if (TextUtils.isEmpty(imageName)) {
            return "";
        }

        int index = 0;
        for (int i = 0; i < 6; i++)
            index = imageName.indexOf("-", index + 1);

        String uuid = imageName.substring(index + 1, imageName.length());
        uuid = uuid.substring(0, uuid.length() - 4);
        return uuid;
    }

    public static String subString(String s) {
        return s.substring(s.length() - 53, s.length() - 32);
    }

    /**
     * Use to find out when device is truely connect to internet
     *
     * @return
     */
    public static boolean canReachInternet() {

        Runtime runtime = Runtime.getRuntime();
        try {
//			Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
            int mExitValue = mIpAddrProcess.waitFor();
            if (mExitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ignore) {
        } catch (IOException e) {
        }
        return false;
    }

    /*
    * Write log to text file in Download Directory
    * */
    public static void writeToLogFile(Object object, String containerId) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (File f : downloadDir.listFiles()) {
            if (f.isFile() && f.getName().equals("CJay_Log.txt")) {
                try {
                    BufferedWriter buf = new BufferedWriter(new FileWriter(f, true));
                    if (object instanceof Session) {
                        Session session = (Session) object;
                        buf.append("Begin to upload ContainerID: " + session.getContainerId() + "| Step: " + session.getLocalStep());
                    } else if (object instanceof AuditItem) {
                        AuditItem item = (AuditItem) object;
                        buf.append("Begin to upload item: " + item.getComponentCode()
                                + " " + item.getDamageCode() + " " + item.getRepairCode()
                                + "| ContainerID: " + containerId);
                    } else {
                        Logger.w("not find class of this object");
                    }
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
