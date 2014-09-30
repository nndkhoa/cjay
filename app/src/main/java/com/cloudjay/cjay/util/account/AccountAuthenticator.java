package com.cloudjay.cjay.util.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.cloudjay.cjay.activity.HomeActivity;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static com.cloudjay.cjay.util.account.AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS;
import static com.cloudjay.cjay.util.account.AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS_LABEL;
import static com.cloudjay.cjay.util.account.AccountGeneral.AUTH_TOKEN_TYPE_READ_ONLY;
import static com.cloudjay.cjay.util.account.AccountGeneral.AUTH_TOKEN_TYPE_READ_ONLY_LABEL;

/**
 * TODO: write description
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {
	Context mContext;

	public AccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		final Bundle result;
		final Intent intent;

		intent = new Intent(this.mContext, HomeActivity.class);
		intent.putExtra(AccountGeneral.ARG_ACCOUNT_TYPE, accountType);
		intent.putExtra(AccountGeneral.ARG_AUTH_TYPE, authTokenType);
		intent.putExtra(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		result = new Bundle();
		result.putParcelable(AccountManager.KEY_INTENT, intent);

		return result;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

		// If the caller requested an authToken type we don't support, then
		// return an error
		if (!authTokenType.equals(AccountGeneral.AUTH_TOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager am = AccountManager.get(mContext);

		String authToken = am.peekAuthToken(account, authTokenType);

		// Lets give another try to authenticate the user
//		if (TextUtils.isEmpty(authToken)) {
//			final String password = am.getPassword(account);
//			if (password != null) {
//				try {
//					Log.d("udinic", TAG + "> re-authenticating with the existing password");
//					authToken = sServerAuthenticate.userSignIn(account.name, password, authTokenType);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			return result;
		}
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		if (AUTH_TOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
			return AUTH_TOKEN_TYPE_FULL_ACCESS_LABEL;
		else if (AUTH_TOKEN_TYPE_READ_ONLY.equals(authTokenType))
			return AUTH_TOKEN_TYPE_READ_ONLY_LABEL;
		else
			return authTokenType + " (Label)";
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		final Bundle result = new Bundle();
		result.putBoolean(KEY_BOOLEAN_RESULT, false);
		return result;
	}
}
