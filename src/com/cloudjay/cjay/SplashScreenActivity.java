package com.cloudjay.cjay;

import java.util.Calendar;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Session;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;

public class SplashScreenActivity extends Activity {

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
			backgroundImageView
					.setBackgroundResource(R.drawable.container_terminal_night);
		} else {
			backgroundImageView
					.setBackgroundResource(R.drawable.container_terminal_day);
		}

		new Handler().postDelayed(new Runnable() {
			public void run() {

				// restore Session
				Session session = Session.restore(getApplicationContext());
				if (null == session) {
					// user did not sign in
					startActivity(new Intent(SplashScreenActivity.this,
							LoginActivity_.class));
				} else {
					// user signed in
					session.extendAccessTokenIfNeeded(getApplicationContext());
					startActivity(new Intent(SplashScreenActivity.this,
							MainActivity_.class));
				}

				finish();
			}
		}, CJayConstant.SPLASH_TIME_OUT);
	}

}
