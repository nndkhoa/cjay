package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.command.CommandQueue;
import com.cloudjay.cjay.util.Logger;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.EService;

@EIntentService
public class QueryService extends IntentService implements Command.Callback {

	@Bean
	CommandQueue queue;

	private boolean running;

	public QueryService() {
		super("QueryService");
	}

//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		executeNext();
//		return START_STICKY;
//	}

	private void executeNext() {
		if (running) return; // Only one task at a time.

		Command task = queue.peek();
		if (task != null) {

			running = true;
			task.execute(this);

		} else {

			Logger.d("Service stopping!");
			stopSelf(); // No more tasks are present. Stop.
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		executeNext();
	}

	@Override
	public void onSuccess(String url) {
		running = false;
		queue.remove();
		executeNext();
	}

	@Override
	public void onFailure(Throwable e) {
		running = false;
		queue.remove();
		executeNext();
	}
}
