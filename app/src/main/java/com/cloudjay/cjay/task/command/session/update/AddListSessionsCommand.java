package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import java.util.List;

public class AddListSessionsCommand extends Command {

	Context context;
	List<Session> sessions;

	public AddListSessionsCommand(Context context, List<Session> sessions) {
		this.context = context;
		this.sessions = sessions;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.processListSession(context, sessions);
	}
}
