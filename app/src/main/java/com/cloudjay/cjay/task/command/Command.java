package com.cloudjay.cjay.task.command;

import com.squareup.tape.Task;

import org.androidannotations.annotations.EBean;

/**
 * Implement Callback để khi chạy xong sẽ phải gọi onSuccess để getNextItem()
 */
public abstract class Command implements Task<Command.Callback> {

	public interface Callback {
		void onSuccess(String url);

		void onFailure(Throwable e);
	}

	protected abstract void run();

	@Override
	public void execute(Callback callback) {
		try {
			run();
			callback.onSuccess("");
		} catch (Throwable e) {
			callback.onFailure(e);
		}

	}
}
