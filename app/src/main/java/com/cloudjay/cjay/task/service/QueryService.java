package com.cloudjay.cjay.task.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.command.CommandQueue;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

@EService
public class QueryService extends Service implements Command.Callback {

	@Bean
	CommandQueue queue;

	public boolean isProcessing() {
		return processing;
	}
	private boolean processing;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		executeNext();
		return START_STICKY;
	}

	private void executeNext() {

		if (processing) return; // Only one task at a time.

		Command task = queue.peek();
		if (task != null) {

			processing = true;
			task.execute(this);

		} else {

			Logger.d("Service stopping!");
			stopSelf(); // No more tasks are present. Stop.
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.v("Start query service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onSuccess(String url) {
		processing = false;
		queue.remove();
		executeNext();
	}

	@Override
	public void onFailure(Throwable e) {
		e.printStackTrace();
	}
}
