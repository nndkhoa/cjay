package com.cloudjay.cjay;

import java.io.IOException;
import java.sql.SQLException;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@EActivity
public class CJayActivity extends SherlockFragmentActivity implements
		ICJayActivity {

	private static final String LOG_TAG = "CJayActivity";

	private Session session;
	private DataCenter dataCenter;

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	public Session getSession() {
		return session;
	}

	public User getCurrentUser() {
		if (null == session)
			session = Session.restore(getApplicationContext());

		return session.getCurrentUser();
	}

	@Override
	protected void onCreate(Bundle arg0) {

		// Thread.setDefaultUncaughtExceptionHandler(new
		// UncaughtExceptionHandler() {
		//
		// @Override
		// public void uncaughtException(Thread arg0, Throwable arg1) {
		// showCrouton(R.string.alert_try_again);
		//
		// // ACRA.getErrorReporter().handleSilentException(arg1);
		// // ACRA.getErrorReporter().handleException(new
		// // Exception("Just for the stacktrace"));
		// }
		//
		// });

		super.onCreate(arg0);
		session = Session.restore(getApplicationContext());

		// if (android.os.Build.VERSION.SDK_INT > 9) {
		// StrictMode.ThreadPolicy policy = new
		// StrictMode.ThreadPolicy.Builder()
		// .permitAll().build();
		// StrictMode.setThreadPolicy(policy);
		// }
	}

	@Override
	public Context getContext() {
		return this;
	}

	GoogleCloudMessaging gcm;
	Context context;
	String regid;

	@Override
	protected void onResume() {

		Logger.Log(LOG_TAG, "***\nonResume - DataCenter.reload\n***");

		if (null != session) {

			if (this instanceof SplashScreenActivity) {
				Logger.Log(LOG_TAG, "Call from SplashScreenActivity");
				new AsyncTask<Void, Integer, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						try {
							DataCenter.getInstance().fetchData(
									getApplicationContext());
						} catch (NoConnectionException e) {
							e.printStackTrace();
							showCrouton(R.string.alert_no_network);
						}
						return null;
					}

				}.execute();

			} else {

				Logger.Log(LOG_TAG, "Call from others Activity");
				new AsyncTask<Void, Integer, Void>() {

					protected void onPreExecute() {
						Logger.Log(LOG_TAG, "onPreExecute");
						EventBus.getDefault().post(new PreLoadDataEvent());
					};

					protected void onPostExecute(Void result) {
						Logger.Log(LOG_TAG, "onPostExecute");
						EventBus.getDefault().post(new PostLoadDataEvent());
					};

					@Override
					protected Void doInBackground(Void... params) {

						try {
							DataCenter.getInstance()
									.updateListContainerSessions(
											getApplicationContext());

						} catch (NoConnectionException e) {
							e.printStackTrace();
							showCrouton(R.string.alert_no_network);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return null;
					}

				}.execute();
			}

			context = getApplicationContext();

			// Check device for Play Services APK.
			if (checkPlayServices()) {
				gcm = GoogleCloudMessaging.getInstance(this);
				regid = Utils.getRegistrationId(context);

				if (regid.isEmpty()) {
					registerInBackground();
				}
			}
		}

		super.onResume();
	}

	private void sendRegistrationIdToBackend() {
		// Your implementation here.
		try {
			CJayClient.getInstance().addGCMDevice(regid, context);
			// When Submit Server Successfully, save it here!.
			Utils.storeRegistrationId(context, regid);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Can't Register device with the Back-end!");
		} catch (NoConnectionException e) {
			showCrouton(R.string.alert_no_network);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
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
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.d(LOG_TAG, msg + "\n");
			}
		}.execute(null, null, null);
	}

	private boolean checkPlayServices() {
		Logger.Log(LOG_TAG, "checkPlayServices()");

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						CJayConstant.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.e("DEVICE_UNSUPPORTED", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	@UiThread
	public void showCrouton(int textResId) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, textResId, Style.ALERT)
				.setConfiguration(
						new Configuration.Builder().setDuration(
								Configuration.DURATION_INFINITE).build());

		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});

		crouton.show();
	}

	@UiThread
	protected void showCrouton(String message) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, message, Style.ALERT)
				.setConfiguration(
						new Configuration.Builder().setDuration(
								Configuration.DURATION_INFINITE).build());

		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});

		crouton.show();
	}
}