package com.cloudjay.cjay.events;

import com.cloudjay.cjay.model.ContainerSession;

public class UploadStateChangedEvent {

	private final ContainerSession containerSession;

	public UploadStateChangedEvent(ContainerSession upload) {
		containerSession = upload;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

}
