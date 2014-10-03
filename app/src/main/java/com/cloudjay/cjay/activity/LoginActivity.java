package com.cloudjay.cjay.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.account.AccountGeneral;

import org.androidannotations.annotations.EActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.RetrofitError;

@EActivity
public class LoginActivity extends AccountAuthenticatorActivity {

	public static final String PARAM_AUTH_TOKEN_TYPE = "auth.token";
	private AccountManager mAccountManager;
	private AlertDialog mAlertDialog;
	private boolean mInvalidate;
	public String mtoken;
	String email;
	String password;

	@InjectView(R.id.btn_login)
	Button mLoginButton;

	@InjectView(R.id.email)
	EditText etEmail;

	@InjectView(R.id.password)
	EditText etPassword;

	@InjectView(R.id.iv_app)
	ImageView imageView;

	@InjectView(R.id.rootLayout)
	LinearLayout ll_root;

	@InjectView(R.id.login_form)
	ScrollView login_form;

	// TODO: need to refactor all layout name
	@InjectView(R.id.login_status)
	LinearLayout ll_login_status;

	@InjectView(R.id.login_status_message)
	TextView tvLoginStatusMessage;

	AccountManager accountManager;
	InputMethodManager inputManager;

	void getUserToken() {
		showAccountPicker(AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS, false);
	}

	private void showAccountPicker(final String authtokenTypeFullAccess, boolean b) {

		mInvalidate = b;
		final Account availableAccounts[] = accountManager
				.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

		if (availableAccounts.length == 0) {
			Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
		} else {
			String name[] = new String[availableAccounts.length];
			for (int i = 0; i < availableAccounts.length; i++) {
				name[i] = availableAccounts[i].name;
			}

			// Account picker
			mAlertDialog = new AlertDialog.Builder(this)
					.setTitle("Pick Account")
					.setAdapter(
							new ArrayAdapter<String>(getBaseContext(),
									android.R.layout.simple_list_item_1, name),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
								                    int which) {
									if (mInvalidate)
										invalidateAuthToken(availableAccounts[which], authtokenTypeFullAccess);
									else
										getExistingAccountAuthToken(availableAccounts[which], authtokenTypeFullAccess);
								}
							}).create();

			mAlertDialog.show();
		}
	}

	private void invalidateAuthToken(final Account availableAccount, String authtokenTypeFullAccess) {
		final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(availableAccount, authtokenTypeFullAccess, null, this, null, null);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Bundle bnd = future.getResult();
					final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
					mAccountManager.invalidateAuthToken(availableAccount.type, authtoken);

					showMessage(availableAccount.name + " invalidated");
				} catch (Exception e) {
					e.printStackTrace();
					showMessage(e.getMessage());
				}
			}
		}).start();
	}

	private void getExistingAccountAuthToken(Account availableAccount, String authtokenTypeFullAccess) {
		final AccountManagerFuture<Bundle> future = accountManager
				.getAuthToken(availableAccount, authtokenTypeFullAccess, null, this, null, null);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Bundle bnd = future.getResult();

					final String authtoken = bnd
							.getString(AccountManager.KEY_AUTHTOKEN);
					showMessage((authtoken != null) ? "SUCCESS!\ntoken: "
							+ authtoken : "FAIL");
					Logger.e("CJay GetToken Bundle is " + bnd);

				} catch (Exception e) {
					e.printStackTrace();
					showMessage(e.getMessage());
				}
			}
		}).start();
	}

	private void showMessage(final String s) {
		if (TextUtils.isEmpty(s))
			return;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	@OnClick(R.id.btn_login)
	void doLogin() {
		email = etEmail.getText().toString();
		password = etPassword.getText().toString();
		View focusView = null;
		boolean cancel = false;
		//Check connect to internet
		if (!hasConnection()) {
			showCrouton(R.string.error_connection);
		}
		// Check for a valid password.
		else {
			if (TextUtils.isEmpty(password)) {
				etPassword.setError(getString(R.string.error_password_field_required));
				focusView = etPassword;
				cancel = true;
			} else if (password.length() < 6) {
				etPassword.setError(getString(R.string.error_invalid_password));
				focusView = etPassword;
				cancel = true;
			}
			// Check for a valid email address.

			if (TextUtils.isEmpty(email)) {
				etEmail.setError(getString(R.string.error_email_field_required));
				focusView = etEmail;
				cancel = true;
			} else if (!email.contains("@")) {
				etEmail.setError(getString(R.string.error_invalid_email));
				focusView = etEmail;
				cancel = true;
			}
			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();

			} else {
				// Define login asynctask login
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				AsyncTask<Void, Void, Void> login = new AsyncTask<Void, Void, Void>() {
					@Override
					protected void onPreExecute() {
						login_form.setVisibility(View.GONE);
						ll_login_status.setVisibility(View.VISIBLE);

						super.onPreExecute();
					}

					@Override
					protected Void doInBackground(Void... params) {
						String token = null;
						try {
							token = NetworkClient.getInstance().getToken(getApplicationContext(), email, password);
							Log.e("Results: ", token);
							mtoken = token;
							if (null != token) {
								// add account to account manager
								addNewAccount(email, password, token, AccountGeneral.AUTH_TOKEN_TYPE);
							}
							return null;
						} catch (RetrofitError error) {

							return null;
						}

					}

					@Override
					protected void onPostExecute(Void aVoid) {
						//Check login success
						if (null != mtoken) {
							mtoken = "Token " + mtoken;
							// Define get data after login success asyntask
							AsyncTask<Void, Void, Void> getDataAfterLogin = new AsyncTask<Void, Void, Void>() {
								@Override
								protected void onPreExecute() {
									tvLoginStatusMessage.setText(R.string.login_progress_loading_data);
									super.onPreExecute();
								}

								@Override
								protected Void doInBackground(Void... params) {
									//TODO add all data below to database
									User user = NetworkClient.getInstance().getCurrentUser(getApplicationContext(), mtoken);
									NetworkClient.getInstance().getDamageCodes(getApplicationContext(), mtoken, user.getFullName(), null);
									NetworkClient.getInstance().getComponentCodes(getApplicationContext(), mtoken, user.getFullName(), null);
									NetworkClient.getInstance().getRepairCodes(getApplicationContext(), mtoken, user.getFullName(), null);
									NetworkClient.getInstance().getOperators(getApplicationContext(), mtoken, user.getFullName(), null);
	                                /*//get operators from server
                                    List<Operator> operators = NetworkClient.getInstance().getOperators(getApplicationContext(), mtoken, null);
                                    //save operators to client
                                    ContentValues addValues[] = new ContentValues[operators.size()];
                                    int i = 0;
                                    for (Operator operator : operators) {
                                        addValues[i++] = operator.getContentValues();
                                    }
                                    getContentResolver().bulkInsert(Operator.URI, addValues);*/
									return null;
								}

								@Override
								protected void onPostExecute(Void aVoid) {
									Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
									startActivity(intent);
									finish();
									super.onPostExecute(aVoid);
								}
							}.execute();
							super.onPostExecute(aVoid);
						} else {
							ll_login_status.setVisibility(View.GONE);
							login_form.setVisibility(View.VISIBLE);
							etEmail.setError(getString(R.string.error_incorrect_password));
						}
					}

				}.execute();


			}

		}
	}

	/**
	 * Add account to account manager
	 *
	 * @param email
	 * @param password
	 * @param token
	 * @param authTokenType
	 */

	private void addNewAccount(String email, String password, String token, String authTokenType) {
		AccountManager manager = AccountManager.get(this);
		String accountType = this.getIntent().getStringExtra(
				PARAM_AUTH_TOKEN_TYPE);
		if (accountType == null) {
			accountType = AccountGeneral.ACCOUNT_TYPE;
		}

		final Account account = new Account(email, accountType);

		manager.addAccountExplicitly(account, password, null);
		manager.setAuthToken(account, AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS, token);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);
		this.setAccountAuthenticatorResult(intent.getExtras());
		this.setResult(RESULT_OK, intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		accountManager = AccountManager.get(this);
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		ButterKnife.inject(this);
		EventBus.getDefault().register(this);


	}

	public void onEvent(LoginSuccessEvent loginSuccessEvent) {
		Log.e("EventBus: ", "OK");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//	EventBus.getDefault().unregister(this);
	}

	/**
	 * Checks if the device has Internet connection.
	 *
	 * @return <code>true</code> if the phone is connected to the Internet.
	 */
	public boolean hasConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(
				Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}

		return false;
	}

	//Show error
	public void showCrouton(int textResId) {

		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, textResId, Style.ALERT);
		crouton.setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});

		crouton.show();
	}
}
