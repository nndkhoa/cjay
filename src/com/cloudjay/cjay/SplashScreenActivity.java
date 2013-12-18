package com.cloudjay.cjay;

import java.util.Calendar;

import com.cloudjay.cjay.util.CJayConstant;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;

public class SplashScreenActivity extends Activity {

	ImageView backgroundImageView = null;
	Boolean isSignedIn = false;

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

				if (isSignedIn) {
					startActivity(new Intent(SplashScreenActivity.this,
							MainActivity_.class));
				} else {
					startActivity(new Intent(SplashScreenActivity.this,
							LoginActivity.class));
				}

				finish();
			}
		}, CJayConstant.SPLASH_TIME_OUT);
	}

}
