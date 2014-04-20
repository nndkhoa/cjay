package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.ContainerSession;

/**
 * 
 * Trigger when user change `Upload State` of item in `UploadsFragment`.
 * 
 * @author tieubao
 * 
 */

public class UploadStateChangedEvent {

	private final ContainerSession containerSession;

	public UploadStateChangedEvent(ContainerSession upload) {
		containerSession = upload;
	}

	public ContainerSession getTarget() {
		return containerSession;
	}

}
