package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStartedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;

import de.greenrobot.event.EventBus;

public class PrepareForUploadingCommand extends Command {

	Context context;
	Session session;

	public PrepareForUploadingCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void run() {
		Step step = Step.values()[session.getLocalStep()];
		DataCenter_.getInstance_(context).prepareForUploading(context, session, step);
	}
}
