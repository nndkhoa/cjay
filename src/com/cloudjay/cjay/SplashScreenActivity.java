package com.cloudjay.cjay;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.Logger;

public class SplashScreenActivity extends CJayActivity {

	ImageView backgroundImageView = null;
	Boolean isSignedIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.Log("onCreate SplashScreenActivity");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		backgroundImageView = (ImageView) findViewById(R.id.splash_screen_background);

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		Boolean isNight = hour < 6 || hour > 18;
		try {
			if (isNight) {
				Logger.Log("at Night");

				backgroundImageView.setImageResource(R.drawable.container_terminal_night);
			} else {
				Logger.Log("at Daytime");
				backgroundImageView.setImageResource(R.drawable.container_terminal_day);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				CJaySession session = getSession();
				if (null == getSession()) {

					// user did not sign in
					Logger.Log("Session is NULL. User did not sign in.");
					startActivity(new Intent(SplashScreenActivity.this, LoginActivity_.class));

				} else {

					// user signed in
					Logger.Log("User signed in");
					session.extendAccessTokenIfNeeded(getApplicationContext());
					CJayApplication.startCJayHomeActivity(SplashScreenActivity.this);

				}

				finish();
			}
		}, CJayConstant.SPLASH_TIME_OUT);
	}

}
