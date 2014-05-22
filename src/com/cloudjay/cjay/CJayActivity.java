package com.cloudjay.cjay;

import java.io.IOException;
import java.sql.SQLException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.events.LogUserActivityEvent;
import com.cloudjay.cjay.events.PostLoadDataEvent;
import com.cloudjay.cjay.events.PreLoadDataEvent;
import com.cloudjay.cjay.events.UserLoggedOutEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.InvokeType;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@EActivity
@OptionsMenu(R.menu.menu_base)
public class CJayActivity extends SherlockFragmentActivity {

	@Bean
	CJaySession session;

	@Bean
	DataCenter dataCenter;

	int usernameMenuClickCount = 0;
	GoogleCloudMessaging gcm;
	Context context;
	String regid;
	boolean isActivityRunning;

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, CJayConstant.PLAY_SERVICES_RESOLUTION_REQUEST)
										.show();
			} else {
				Log.e("DEVICE_UNSUPPORTED", "This device is not supported.");
				finish();
			}

			return false;
		}
		return true;
	}

	public Context getContext() {
		return this;
	}

	public User getCurrentUser() {

		if (null == session) {
			session = CJaySession.restore(getApplicationContext());
		}

		return session.getCurrentUser();
	}

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	public CJaySession getSession() {
		return session;
	}

	public boolean isRunning() {
		return isActivityRunning;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		EventBus.getDefault().register(this);
		super.onCreate(arg0);
		session = CJaySession.restore(getApplicationContext());
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	public void onEvent(LogUserActivityEvent event) {
		DataCenter.getDatabaseHelper(context).addUsageLog(event.getTarget());
	}

	public void onEvent(UserLoggedOutEvent event) {

		Logger.Log("onEvent UserLoggedOutEvent");

		if (DataCenter.LoadDataTask.getStatus() == AsyncTask.Status.RUNNING) {
			Logger.Log("BGTask is running");
			DataCenter.LoadDataTask.cancel(true);
		}

		if (DataCenter.LoadDataTask.getStatus() == AsyncTask.Status.RUNNING) {
			Logger.Log("BGTask is still running ????");
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.menu_setting:
		// Intent intent = new Intent(this, SettingsActivity.class);
		// startActivity(intent);
		// return true;

			case R.id.menu_logout:
				showLogoutPrompt();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		isActivityRunning = false;

		super.onPause();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_username).setTitle(getCurrentUser().getFullName());

		menu.findItem(R.id.menu_role).setTitle(getCurrentUser().getRoleName());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {

		super.onResume();

		// Logger.Log("*** onResume - DataCenter.reload ***");
		long startTime = System.currentTimeMillis();

		if (null != session) {

			if (this instanceof SplashScreenActivity) {
				new AsyncTask<Void, Integer, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						try {
							DataCenter.getInstance().fetchData(getApplicationContext());
						} catch (NoConnectionException e) {

						} catch (NullSessionException e) {
							CJayApplication.logOutInstantly(context);
							finish();
						}
						return null;
					}
				}.execute();

			} else {

				DataCenter.LoadDataTask = new AsyncTask<Void, Integer, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						try {

							boolean initialize = true;
							try {
								initialize = PreferencesUtil.getPrefsValue(	context, PreferencesUtil.PREF_INITIALIZED,
																			false);
							} catch (Exception e) {
								Logger.e(e.getMessage());
							}

							if (!initialize) {
								Logger.Log("update List CS FIRST TIME");
								DataCenter.getInstance()
											.updateListContainerSessions(getApplicationContext(),
																			CJayClient.REQUEST_TYPE_MODIFIED,
																			InvokeType.FIRST_TIME);

							} else {

								// Logger.Log("update List CS FOLLOWING TIME");
								DataCenter.getInstance()
											.updateListContainerSessions(getApplicationContext(),
																			CJayClient.REQUEST_TYPE_MODIFIED,
																			InvokeType.FOLLOWING);
							}

						} catch (NoConnectionException e) {
							showCrouton(R.string.alert_no_network);
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (NullSessionException e) {
							CJayApplication.logOutInstantly(context);
							finish();
						}
						return null;
					};

					@Override
					protected void onPostExecute(Void result) {
						EventBus.getDefault().post(new PostLoadDataEvent());
					};

					@Override
					protected void onPreExecute() {
						EventBus.getDefault().post(new PreLoadDataEvent());
					}

				};

				DataCenter.LoadDataTask.execute();
			}

			context = getApplicationContext();

			// Check device for Play Services APK.
			if (checkPlayServices()) {

				gcm = GoogleCloudMessaging.getInstance(this);
				regid = Utils.getRegistrationId(context);

				if (TextUtils.isEmpty(regid)) {
					registerInBackground();
				}
			}

			long difference = System.currentTimeMillis() - startTime;
			// Logger.w("---> Total time: " + Long.toString(difference));
		}

		isActivityRunning = true;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's shared preferences.
	 */
	private void registerInBackground() {

		DataCenter.RegisterGCMTask = new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				Logger.Log("begin to register GCM device in background");
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}

					regid = gcm.register(CJayConstant.SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server
					// over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the
					// message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
				} catch (IOException ex) {
					// may catch "SERVICE_NOT_AVAILABLE"
					msg = "Error :" + ex.getMessage();

					// If there is an error, don't just keep trying to
					// register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Logger.d(msg + "\n");
			}
		};

		DataCenter.RegisterGCMTask.execute(null, null, null);
	}

	private void sendRegistrationIdToBackend() {
		// Your implementation here.
		try {
			CJayClient.getInstance().addGCMDevice(regid, context);

			if (TextUtils.isEmpty(regid)) {
				Logger.e("Cannot send Registration ID to Server");
			} else {

				// When Submit Server Successfully, save it here!.
				Utils.storeRegistrationId(context, regid);
				DataCenter.getDatabaseHelper(context).addUsageLog("Register #GCM device");

			}
		} catch (JSONException e) {
			Logger.e("Can't Register device with the Back-end!");

		} catch (NoConnectionException e) {
			showCrouton(R.string.alert_no_network);

		} catch (NullSessionException e) {
			Logger.Log("Session is NULL");
		}
	}

	public void setDataCenter(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	@UiThread
	public void showCrouton(int textResId) {

		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, textResId, Style.ALERT)
										.setConfiguration(	new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE)
																						.build());

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
										.setConfiguration(	new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE)
																						.build());

		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});

		crouton.show();
	}

	protected void showLogoutPrompt() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.logout_prompt_title);

		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getSession().deleteSession(getApplicationContext());
				startActivity(new Intent(getApplicationContext(), LoginActivity_.class));
				finish();

				PreferencesUtil.clearPrefs(context);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}

	@OptionsItem(R.id.menu_username)
	void usernameMenuItemSelected() {

		usernameMenuClickCount++;
		int left = CJayConstant.HIDDEN_LOG_THRESHOLD - usernameMenuClickCount;

		if (left <= 2 && left > 0) {
			Toast.makeText(this, "You have to click " + Integer.toString(left) + " to open Settings",
							Toast.LENGTH_SHORT).show();
		}

		if (left == 0) {

			// Open Settings
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

			usernameMenuClickCount = 0;
		}

	}

	@Background
	void refresh() {
		try {
			DataCenter.getInstance().fetchData(this, true);
		} catch (NoConnectionException e) {
			showCrouton(R.string.alert_no_network);
		} catch (NullSessionException e) {
			CJayApplication.logOutInstantly(this);
			onDestroy();
		} catch (Exception e) {
			showCrouton(R.string.alert_try_again);
			e.printStackTrace();
		}
	}

	@OptionsItem(R.id.menu_refresh)
	void refreshItemSelected() {
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				try {
					DataCenter.getInstance().fetchData(getApplicationContext(), true);
				} catch (NoConnectionException e) {
					showCrouton(R.string.alert_no_network);

				} catch (NullSessionException e) {
					CJayApplication.logOutInstantly(getApplicationContext());
					onDestroy();

				} catch (Exception e) {
					showCrouton(R.string.alert_try_again);
					e.printStackTrace();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				EventBus.getDefault().post(new PostLoadDataEvent());
			};

			@Override
			protected void onPreExecute() {
				EventBus.getDefault().post(new PreLoadDataEvent());
			}

		}.execute();
	}

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}
}