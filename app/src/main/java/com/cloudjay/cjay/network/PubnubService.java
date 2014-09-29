package com.cloudjay.cjay.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * Created by nambv on 24/09/2014.
 */
public class PubnubService extends Service {

    private NotificationManager notificationManager;

    private static final String PUBLISH_KEY = "publish_key";
    private static final String SUBCRIBE_KEY = "subcribe_key";

    private static final String SESSION_CHANNEL = "session_channel";
    private static final String ISO_CODE_CHANNEL = "iso_code_channel";
    private static final String OPERATOR_CHANNEL = "operator_channel";

    private String[] channels = new String[] {
        SESSION_CHANNEL,
        ISO_CODE_CHANNEL,
        OPERATOR_CHANNEL
    };

    Pubnub pubnub = new Pubnub(PUBLISH_KEY, SUBCRIBE_KEY);

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //get data from bundle
            Bundle b = msg.getData();
            final String channel = b.getString("channel");
            final String message = b.getString("message");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    pushNotification(channel, message);
                }
            }, 200);
        }
    };

    private void pushNotification(String channel, String message) {

        Notification notification = null;

        //Todo: Push notification
        notification = new Notification.Builder(this).setContentTitle(channel)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND).build();

        notificationManager.notify(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyUser(String channel, Object message) {
        Message msg = handler.obtainMessage();

        try {
            final String obj = (String) message;
            Bundle b = new Bundle();
            b.putString("channel", channel);
            b.putString("message", obj);
            msg.setData(b);
            handler.sendMessage(msg);

            Logger.e("Received msg : " + obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        try {
            pubnub.subscribe(channels, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Logger.e("success: " + message.toString());
                    notifyUser(channel, message);
                }

                @Override
                public void errorCallback(String channel, PubnubError pubnubError) {
                    Logger.e("error: " + pubnubError.toString());
                    notifyUser(channel, pubnubError.toString());
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }
}
