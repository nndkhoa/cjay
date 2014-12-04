package com.cloudjay.cjay.task.command.session.get;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.ContainerForUploadGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class GetSessionForUploadCommand extends Command {

	Context context;
	String containerId;

	public GetSessionForUploadCommand(Context context, String containerId) {
		this.context = context;
		this.containerId = containerId;
	}

	@Override
	public void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		Session session = dataCenter.getSession(context, containerId);

		if (session != null) {
			List<Session> list = new ArrayList<>();
			list.add(session);
			EventBus.getDefault().post(new ContainerForUploadGotEvent(list, containerId));
		}
	}
}
