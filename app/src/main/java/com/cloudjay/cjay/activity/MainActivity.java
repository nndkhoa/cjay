package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cloudjay.cjay.R;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public static Intent getCallingIntent(Context context) {
		return new Intent(context, MainActivity.class);
	}


}
