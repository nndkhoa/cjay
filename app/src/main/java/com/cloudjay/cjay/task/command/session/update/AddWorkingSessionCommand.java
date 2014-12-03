package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;

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
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addSession(context, session, CJayConstant.PREFIX_WORKING);
		EventBus.getDefault().post(new WorkingSessionCreatedEvent(session));
	}
}
