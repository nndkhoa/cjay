package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Step;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Add container to collection WORKING database
 */
public class AddWorkingSessionCommand extends Command {

	Context context;
	Session session;

	public AddWorkingSessionCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void run() {
		try {
			DB db = App.getDB(context);
			String key = CJayConstant.PREFIX_WORKING + session.getContainerId();
			db.put(key, session);

			// Notify to Working Fragment
			EventBus.getDefault().post(new WorkingSessionCreatedEvent(session));

		} catch (SnappydbException e) {
			e.printStackTrace();
		}
	}
}
