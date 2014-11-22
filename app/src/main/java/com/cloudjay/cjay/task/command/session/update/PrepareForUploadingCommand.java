package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;

public class PrepareForUploadingCommand extends Command {

	Context context;
	UploadStatus uploadStatus;
	Session session;

	@Override
	protected void run() {

		// Change local step
		if (uploadStatus == UploadStatus.UPLOADING) {
			Step step = Step.values()[session.getLocalStep()];
			switch (step) {
				case IMPORT:
					session.setLocalStep(Step.AUDIT.value);
					break;

				case AUDIT:
					session.setLocalStep(Step.REPAIR.value);
					break;

				case AVAILABLE:
					session.setLocalStep(Step.EXPORTED.value);
					break;

				case REPAIR:
				default:
					session.setLocalStep(Step.AVAILABLE.value);
					break;
			}

			//Change upload status
			session.setUploadStatus(uploadStatus);

			// Add to uploading
			DataCenter_.getInstance_(context).add(new AddUploadingSessionCommand(context, session));
		} else if (uploadStatus == UploadStatus.COMPLETE) {
			session.setUploadStatus(uploadStatus);
		}
	}
}
