package com.cloudjay.cjay.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.account.AccountGeneral;
import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LoginActivity extends AccountAuthenticatorActivity {

	public static final String PARAM_AUTH_TOKEN_TYPE = "auth.token";
	private AccountManager mAccountManager;
	private AlertDialog mAlertDialog;
	private boolean mInvalidate;
	public String mToken;

	@InjectView(R.id.btn_login)
	Button mLoginButton;

	@InjectView(R.id.email)
	EditText etEmail;

	@InjectView(R.id.password)
	EditText etPassword;

	@InjectView(R.id.btn_getUser)
	Button btnGetUser;

	@InjectView(R.id.btnOpenMainAcitivty)
	Button btnOpenMain;

	AccountManager accountManager;

	@OnClick(R.id.btn_getUser)
	void getUserToken() {
		showAccountPicker(AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS, false);
	}

	private void showAccountPicker(final String authTokenTypeFullAccess, boolean b) {
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
										invalidateAuthToken(availableAccounts[which], authTokenTypeFullAccess);
									else
										getExistingAccountAuthToken(availableAccounts[which], authTokenTypeFullAccess);
								}
							}
					).create();
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

					final String authToken = bnd
							.getString(AccountManager.KEY_AUTHTOKEN);
					showMessage((authToken != null) ? "SUCCESS!\ntoken: "
							+ authToken : "FAIL");
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

		String email = etEmail.getText().toString();
		String password = etPassword.getText().toString();
		String token = NetworkClient.getInstance().getToken(this, email, password);
		mToken = "Token " + token;
		Log.e("Results: ", token);
		//Session user = NetworkClient.getInstance().getContainerSessionById(getApplicationContext(), "Token " + token,7325);

		// Logger.e(user.toString());
		if (null != token) {
			addNewAccount(email, password, token, AccountGeneral.AUTH_TOKEN_TYPE);
		}
//		String currentUser = NetworkClient.getInstance().getCurrentUser(this,"Token "+token);
//		Log.e("Current User: ", currentUser);
		String autho = "Token " + token;
		Logger.e(autho);

	}

	@OnClick(R.id.btnOpenMainAcitivty)
	void open() {
		Logger.i("mToken: " + mToken);
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("token", mToken);
		startActivity(i);
	}

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

		ButterKnife.inject(this);
		EventBus.getDefault().register(this);

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads()
				.detectDiskWrites()
				.detectNetwork()   // or .detectAll() for all detectable problems
				.penaltyLog()
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects()
				.penaltyLog()
				.penaltyDeath()
				.build());
	}

	public void onEvent(LoginSuccessEvent loginSuccessEvent) {
		Log.e("EventBus: ", "OK");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//	EventBus.getDefault().unregister(this);
	}
}
