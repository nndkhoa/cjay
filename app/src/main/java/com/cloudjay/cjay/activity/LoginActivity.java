package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.LoginSuccessEvent;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.NetworkClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LoginActivity extends Activity {

	@InjectView(R.id.btn_login)
	Button mLoginButton;

    @OnClick(R.id.btn_login)
    void doLogin() {
        Log.e("OK", "OK");
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
//		EventBus.getDefault().register(this);
/*
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
*/

		//NetworkClient.getInstance().getToken("giamdinhcong@test.com", "123456");


	}

	/*public void onEvent(LoginSuccessEvent loginSuccessEvent) {


	}*/

//	public void onEventLoginSuccessEvent() {
//		Log.i("OK", "OK");
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	//	EventBus.getDefault().unregister(this);
	}
}
