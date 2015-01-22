package com.cloudjay.cjay.task.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.event.upload.UploadObjectRemovedEvent;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.task.command.UploadQueue;
import com.cloudjay.cjay.task.command.cjayobject.StartUploadingCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveSessionCommand;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Trace;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * This intent service should be called in 2 cases:
 * - First time start app / reboot device, alarm manager will trigger automatically
 * - When user want to upload new item
 */
@EService
public class UploadIntentService extends Service {

    @Bean
    DataCenter dataCenter;

    @Bean
    UploadQueue queue;

    /**
     * Biến processing sẽ có giá trị false khi không còn task nào để thực hiện
     */
    public static boolean processing;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * Receive upload succeeded event then run Remove upload item command.
     *
     * @param event
     */
    public void onEvent(UploadSucceededEvent event) {
        Logger.Log("on UploadSucceededEvent");
        processing = false;
        queue.remove();
    }

    /**
     * Triggered after upload item was removed, then it continues on execution.
     *
     * @param event
     */
    public void onEvent(UploadObjectRemovedEvent event) {
        Logger.Log("on UploadObjectRemovedEvent");
        executeNext();
    }

    @Trace
    public void onEvent(UploadStoppedEvent event) {
        Logger.w("Upload failed, container " + event.session.getContainerId());
        processing = false;
        queue.remove();
//		executeNext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void executeNext() {

        StackTraceElement[] trace = new Throwable().getStackTrace();
        Logger.Log("Open DB " + trace[1].getFileName() + "#" + trace[1].getMethodName() + "() | Line: " + trace[1].getLineNumber());
//
//        Logger.Log("on executeNext");

        if (processing) return; // Only one task at a time.

        // Check if user is logged in or not
        String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);
        if (!TextUtils.isEmpty(token) && Utils.canReachInternet()) {

            JobManager manager = App.getJobManager();
            if (manager.count() != 0) {
                if (processing == false) {

                    // Zombies appear
                    manager.clear();
                    dataCenter.add(new StartUploadingCommand(getApplicationContext()));
                } else {
                    Logger.Log("There is already job in the queue");
                }

            } else {
                dataCenter.add(new StartUploadingCommand(getApplicationContext()));
            }

        } else {
            Logger.w("There was problems. Please check credential or connectivity.");
            Logger.w("Upload service will be stopped.");
            processing = false;
            stopSelf();
        }
    }
}
