package com.cloudjay.cjay;

import java.net.SocketTimeoutException;
import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;

/**
 * 
 * Login activity is extended from CJayActivity and mix AsyncTask<> and Android
 * Annotation to log user in.
 * 
 * @author tieubao
 * 
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends CJayActivity {

	private static final String LOG_TAG = "LoginActivity";

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.cloudjay.cjay.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mPassword = "123456";

	@Extra(EXTRA_EMAIL)
	String mEmail = "giamdinhcong@test.com";
	// String mEmail = "giamdinhcong1.icd1@pip.com.vn";
	// String mEmail = "giamdinhsuachua.icd1@pip.com.vn";
	// String mEmail = "giamdinhsuachua@test.com";
	// String mEmail = "tosuachua@test.com";

	// UI references.
	@ViewById(R.id.email)
	EditText mEmailView;

	@ViewById(R.id.password)
	EditText mPasswordView;

	@ViewById(R.id.login_form)
	View mLoginFormView;

	@ViewById(R.id.login_status)
	View mLoginStatusView;

	@ViewById(R.id.login_status_message)
	TextView mLoginStatusMessageView;

	@ViewById(R.id.sign_in_button)
	Button loginButton;

	@Click(R.id.sign_in_button)
	void loginButtonClicked() {
		attemptLogin();
	}

	@AfterViews
	void init() {
		mEmailView.setText(mEmail);
		mPasswordView.setText(mPassword);

		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_ACTION_GO) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {

		try {
			Logger.Log(LOG_TAG, "trying to login ... ");

			if (mAuthTask != null) {
				return;
			}

			// Reset errors.
			mEmailView.setError(null);
			mPasswordView.setError(null);

			// Store values at the time of the login attempt.
			mEmail = mEmailView.getText().toString();
			mPassword = mPasswordView.getText().toString();

			boolean cancel = false;
			View focusView = null;

			// Check for a valid password.
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView
						.setError(getString(R.string.error_password_field_required));
				focusView = mPasswordView;
				cancel = true;
			} else if (mPassword.length() < 4) {
				mPasswordView
						.setError(getString(R.string.error_invalid_password));
				focusView = mPasswordView;
				cancel = true;
			}

			// Check for a valid email address.
			if (TextUtils.isEmpty(mEmail)) {
				mEmailView
						.setError(getString(R.string.error_email_field_required));
				focusView = mEmailView;
				cancel = true;
			} else if (!mEmail.contains("@")) {
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}

			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();

			} else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView
						.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);
			}
		} catch (Exception e) {
			showCrouton(R.string.error_try_again);
		}
	}

	@UiThread
	void changeProgressText(int stringRes) {
		mLoginStatusMessageView.setText(stringRes);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@UiThread
	void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// attempt authentication against a network service.
			try {

				String userToken = CJayClient.getInstance().getUserToken(
						mEmail, mPassword, LoginActivity.this);

				if (TextUtils.isEmpty(userToken)) {
					// Wrong credential --> return and display error alert
					return false;
				} else {
					// Time to get data from Server and save to database
					changeProgressText(R.string.login_progress_signing_in);

					DataCenter.getInstance().saveCredential(LoginActivity.this,
							userToken);
					changeProgressText(R.string.login_progress_loading_data);

					// DataCenter.getInstance().fetchData(LoginActivity.this);
					DataCenter.getInstance().updateListISOCode(
							LoginActivity.this);
					return true;
				}

			} catch (SocketTimeoutException se) {
				showCrouton(R.string.alert_ssl_timeout);
				se.printStackTrace();
				cancel(isFinishing());

			} catch (JSONException e) {
				showCrouton(R.string.alert_server_error);
				e.printStackTrace();
				cancel(isFinishing());

			} catch (NoConnectionException e) {
				showCrouton(R.string.alert_no_network);
				cancel(isFinishing());

			} catch (SQLException e) {
				e.printStackTrace();
				showCrouton(R.string.alert_try_again);
				cancel(isFinishing());

			} catch (Exception e) {
				e.printStackTrace();
				showCrouton(R.string.alert_try_again);
				cancel(isFinishing());
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {

				// Navigate user to Main Activity based on user role
				Logger.Log(LOG_TAG, "Login successfully");
				CJayApplication.startCJayHomeActivity(LoginActivity.this);
				finish();

			} else {
				Logger.Log(LOG_TAG, "Incorrect Username|Password");
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
