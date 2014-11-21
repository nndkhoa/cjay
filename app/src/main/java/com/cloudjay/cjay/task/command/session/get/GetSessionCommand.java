package com.cloudjay.cjay.task.command.session.get;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

import de.greenrobot.event.EventBus;

public class GetSessionCommand extends Command {

	Context context;
	String containerId;

	public GetSessionCommand(Context context, String containerId) {
		this.context = context;
		this.containerId = containerId;
	}

	@Override
	public void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		Session session = dataCenter.getSession(context, containerId);

		if (session != null) {
			EventBus.getDefault().post(new ContainerGotEvent(session, containerId));
		}
	}
}
