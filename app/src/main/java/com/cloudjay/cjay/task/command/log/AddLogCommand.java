package com.cloudjay.cjay.task.command.log;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

/**
 * Add container to database
 */
public class AddLogCommand extends Command {

	Context context;
	Session session;

	public AddLogCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void run() {

		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addSession(context, session);

		try {
			DB db = App.getDB(context);
			String key = session.getContainerId();
			db.put(key, session);

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
}
