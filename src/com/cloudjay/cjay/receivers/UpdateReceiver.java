package com.cloudjay.cjay.receivers;

import java.io.IOException;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class UpdateReceiver extends BroadcastReceiver {

	protected static final String LOG_TAG = "UpdateReceiver";
	GoogleCloudMessaging gcm;
	Context mContext;
	String regid;

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.Log(LOG_TAG, "onReceive()");
		mContext = context;

		if (Utils.checkPlayServices(context)) {
			gcm = GoogleCloudMessaging.getInstance(context);
			regid = Utils.getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	public void registerInBackground() {

		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(mContext);
					}

					regid = gcm.register(CJayConstant.SENDER_ID);
					Log.d("registration Id", regid + "");
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
				} catch (IOException ex) {
					// may catch "SERVICE_NOT_AVAILABLE"
					msg = "Error :" + ex.getMessage();

					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				} catch (NoConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.d(LOG_TAG, msg + "\n");
			}
		}.execute(null, null, null);
	}

	public void sendRegistrationIdToBackend() throws NoConnectionException {
		// Your implementation here.
		try {
			CJayClient.getInstance().addGCMDevice(regid, mContext);

			// When Submit Server Successfully, save it here!.
			Utils.storeRegistrationId(mContext, regid);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Can't Register device with the Back-end!");
		} catch (NoConnectionException e) {
			Log.e(LOG_TAG, "No Connection");
			throw e;
			// showCrouton(R.string.alert_no_network);
		}
	}
}
