package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.NotificationItemReceivedEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.UploadType;

import de.greenrobot.event.EventBus;

/**
 * Will be called after upload successfully
 */
public class SaveSessionCommand extends Command {
	Context context;
	Session session;
	UploadType uploadType = null;
	Integer popUpNotification;

	public SaveSessionCommand(Context context, Session session, UploadType uploadType) {
		this.context = context;
		this.session = session;
		this.uploadType = uploadType;
	}

	public SaveSessionCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
		this.uploadType = null;
	}

	public SaveSessionCommand(Context context, Session session, Integer notification) {
		this.context = context;
		this.session = session;
		this.popUpNotification = notification;
	}

	@Override
	public void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		boolean success = dataCenter.addOrUpdateSession(context, session);
		EventBus.getDefault().post(new ContainerGotEvent(session, session.getContainerId()));

		if (uploadType != null && success)
			EventBus.getDefault().post(new UploadSucceededEvent(session, UploadType.SESSION));

		if (popUpNotification != null) {
			EventBus.getDefault().post(new NotificationItemReceivedEvent(session, popUpNotification));
		}
	}
}
