package com.cloudjay.cjay.task.command;

import com.snappydb.SnappydbException;
import com.squareup.tape.Task;

/**
 * Implement Callback để khi chạy xong sẽ phải gọi onSuccess để getNextItem()
 */
public abstract class Command implements Task<Command.Callback> {

	public interface Callback {
		void onSuccess(String url);

		void onFailure(Throwable e);
	}

	protected abstract void run() throws SnappydbException;

	@Override
	public void execute(Callback callback) {
		try {
			run();
			callback.onSuccess("");
		} catch (Throwable e) {
			e.printStackTrace();
			callback.onFailure(e);
		}
	}
}
