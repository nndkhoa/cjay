package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.squareup.okhttp.internal.Util;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LoginActivity extends Activity {

	@InjectView(R.id.btn_login)
	Button mLoginButton;
	@InjectView(R.id.email)
	EditText etemail;
	@InjectView(R.id.password)
	EditText etpassword;

	@OnClick(R.id.btn_login)
	void doLogin() {
		String email = etemail.getText().toString();
		String password = etpassword.getText().toString();
		String token = NetworkClient.getInstance().getToken(this,email, password);
		Log.e("Results: ", token);
//		String currentUser = NetworkClient.getInstance().getCurrentUser(this,"Token "+token);
//		Log.e("Current User: ", currentUser);
		String autho = "Token "+token;
		Logger.e(autho);
		String repairCodes = NetworkClient.getInstance().getRepairCodes(this, autho, "");
		Log.e("Repair Codes: ", repairCodes);
		ContentValues values = new ContentValues();
		values.put(User.ACCESS_TOKEN, "dasdefefdsfdsfdsfs");
		values.put(User.EMAIL, "giamdinhcong@test.com");
		values.put(User.ROLE, "full_permission");
		values.put(User.MOTHER_NAME, "fuck you");
		values.put(User.FATHER_NAME, "motherfucker");
		getContentResolver().insert(User.URI, values);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
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
