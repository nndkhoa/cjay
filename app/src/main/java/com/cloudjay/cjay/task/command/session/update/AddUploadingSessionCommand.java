package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;

/**
 * Add container to collection UPLOADING database
 */
public class AddUploadingSessionCommand extends Command {

	Context context;
	Session session;

	public AddUploadingSessionCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addSession(context, session, CJayConstant.PREFIX_UPLOADING);
	}
}
