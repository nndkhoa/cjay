package com.cloudjay.cjay.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cloudjay.cjay.CJayApplication;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class BaseActivity extends FragmentActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//
		CJayApplication app = CJayApplication.get(this);
		app.inject(this);
	}

	/**
	 * Show error had define in String resource to user
	 * @param textResId
	 */
	public void showCrouton(int textResId) {
		Crouton.cancelAllCroutons();
		final Crouton crouton = Crouton.makeText(this, textResId, Style.ALERT);
		crouton.setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
		crouton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Crouton.hide(crouton);
			}
		});
		crouton.show();
	}
}
