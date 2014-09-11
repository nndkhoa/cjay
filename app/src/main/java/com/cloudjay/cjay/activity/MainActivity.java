package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.network.NetworkClient;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

		String result = NetworkClient.getInstance().getToken("giamdinhcong@test.com","123456");
	}

	public static Intent getCallingIntent(Context context) {
		return new Intent(context, MainActivity.class);
	}


}
