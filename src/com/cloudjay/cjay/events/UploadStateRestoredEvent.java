package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.ContainerSession;

/**
 * 
 * Trigger when user change `Upload State` of item in `UploadsFragment`.
 * 
 * @author tieubao
 * 
 */

public class UploadStateRestoredEvent {

	private final ContainerSession containerSession;

	public UploadStateRestoredEvent(ContainerSession upload) {
		containerSession = upload;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

}
