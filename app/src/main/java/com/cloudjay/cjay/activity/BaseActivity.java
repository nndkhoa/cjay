package com.cloudjay.cjay.activity;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class BaseActivity extends FragmentActivity {

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
