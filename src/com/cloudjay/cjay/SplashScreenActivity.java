package com.cloudjay.cjay;

import java.util.Calendar;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Session;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;

public class SplashScreenActivity extends CJayActivity {

	private static final String LOG_TAG = "SplashScreenActivity";

	ImageView backgroundImageView = null;
	Boolean isSignedIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		backgroundImageView = (ImageView) findViewById(R.id.splash_screen_background);

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		Boolean isNight = hour < 6 || hour > 18;

		if (isNight) {
			Logger.Log(LOG_TAG, "at Night");
			backgroundImageView
					.setBackgroundResource(R.drawable.container_terminal_night);
		} else {
			Logger.Log(LOG_TAG, "at Daytime");
			backgroundImageView
					.setBackgroundResource(R.drawable.container_terminal_day);
		}

		new Handler().postDelayed(new Runnable() {
			public void run() {

				// restore Session
				Session session = getSession();
				if (null == getSession()) {
					// user did not sign in
					Logger.Log(LOG_TAG, "session == null");
					startActivity(new Intent(SplashScreenActivity.this,
							LoginActivity_.class));
				} else {
					// user signed in
					Logger.Log(LOG_TAG, "User signed in");
					Logger.Log(LOG_TAG, "Fetching data from server ...");
					session.extendAccessTokenIfNeeded(getApplicationContext());

					CJayApplication
							.startCJayHomeActivity(SplashScreenActivity.this);
				}

				finish();
			}
		}, CJayConstant.SPLASH_TIME_OUT);
	}

}
