package com.cloudjay.cjay.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.account.AccountGeneral;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.RetrofitError;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AccountAuthenticatorActivity {

	public LoginActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountManager = AccountManager.get(this);
	}

	public static final String PARAM_AUTH_TOKEN_TYPE = "auth.token";

	// Inject DataCenter to this Activity
	@Bean
	DataCenter dataCenter;

	@SystemService
	InputMethodManager inputMethodManager;

	AccountManager accountManager;

	String mToken;
	String email;
	String password;

	//region VIEWS
	@ViewById(R.id.btn_login)
	public Button btnLogin;

	@ViewById(R.id.email)
	public EditText etEmail;

	@ViewById(R.id.password)
	public EditText etPassword;

	@ViewById(R.id.iv_app)
	public ImageView iv;

	@ViewById(R.id.ll_root)
	LinearLayout llRoot;

	@ViewById(R.id.login_form)
	ScrollView svLoginForm;

	@ViewById(R.id.ll_search_progress)
	LinearLayout llLoginStatus;

	@ViewById(R.id.login_status_message)
	TextView tvLoginStatusMessage;

	@SystemService
	InputMethodManager inputManager;
	//endregion

	/**
	 * Add account to Account Manager
	 *
	 * @param email
	 * @param password
	 * @param token
	 */
	void addNewAccount(String email, String password, String token) {

		AccountManager manager = AccountManager.get(this);
		String accountType = this.getIntent().getStringExtra(
				PARAM_AUTH_TOKEN_TYPE);

		if (accountType == null) {
			accountType = AccountGeneral.ACCOUNT_TYPE;
			Logger.Log("Account type: " + accountType);
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

	/**
	 * > MAIN FUNCTION
	 * <p/>
	 * 1. Get data từ 2 text views username và password
	 * 2. Validate ở client các trường hợp lỗi và thông báo
	 * 3. Gửi username và password lên cho server
	 * 4. Nhận token trả về và tiến hành gửi request get iso codes và operators
	 * 5. Login thành công thì chuyển trang Home (HomeActivity)
	 */
	@Background
	void doLogin() {

		// Query Token from server and add account to account manager
		try {
			mToken = dataCenter.getToken(email, password);
			if (null != mToken) {

				Logger.Log("Login successfully");

				addNewAccount(email, password, mToken);
				PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_TOKEN, mToken);

				// Continue to fetch List Operators and Iso Codes
				showGettingDataTextView();
				dataCenter.fetchOperators(getApplicationContext());
				dataCenter.fetchIsoCodes(getApplicationContext());

				User user = dataCenter.getCurrentUserAsync(this);
				PreferencesUtil.putObject(this, PreferencesUtil.PREF_CURRENT_USER, user);
				if (null != user) {

					Utils.startAlarm(getApplicationContext());

					// Navigate to Home Activity
					Logger.Log("Navigate to Home Activity");
					Intent intent = new Intent(getApplicationContext(), HomeActivity_.class);
					startActivity(intent);
					finish();

				} else {
					Logger.w("Cannot fetch user information");
					showProgress(false);
					showCrouton(getResources().getString(R.string.error_try_again));
				}

			} else {

				Logger.w("Login failed");
				showProgress(false);
				showError(etEmail, R.string.error_incorrect_password);
			}
		} catch (RetrofitError error) {

			showProgress(false);
			setError(getString(R.string.error_invalid_password));
			showCrouton(error.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			showCrouton(e.getMessage());
			showProgress(false);
		}
	}

	@UiThread
	void setError(String msg) {
		etPassword.setError(msg);
	}

	@UiThread
	void showCrouton(String message) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, message, Style.ALERT);
		crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});
		crouton.show();
	}

	@UiThread
	void showProgress(final boolean show) {
		llLoginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
		svLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@UiThread
	void showError(EditText view, int textResId) {
		view.setError(getString(textResId));
	}

	@Click(R.id.btn_login)
	void btnLoginClicked() {
		performLogin();
	}

	@AfterViews
	void doAfterViews() {

		// Config EditText Password auto login when user press Enter
		etPassword.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
						keyCode == EditorInfo.IME_ACTION_DONE ||
						keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
								keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					if (!keyEvent.isShiftPressed()) {
						performLogin();
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * 1. Check input
	 * 2. Call doLogin() to send request to server
	 */
	void performLogin() {

		// Hide Keyboard
		inputMethodManager.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

		// Check input
		email = etEmail.getText().toString();
		password = etPassword.getText().toString();

		View focusView = null;
		boolean cancel = false;

		// Check for a valid password.
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

		// Done validation process. Try to log user in
		if (cancel) {
			focusView.requestFocus();
		} else if (NetworkHelper.isConnected(this)) {

			if (inputManager != null) {
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}

			showProgress(true);
			doLogin();
		} else {
			Utils.showCrouton(this, R.string.error_connection);
		}
	}

	@UiThread
	void showGettingDataTextView() {
		tvLoginStatusMessage.setText(getResources().getString(R.string.login_progress_loading_data));
	}
}
