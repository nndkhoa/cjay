package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;

/**
 * Trigger when Upload State of ContainerSession changed to
 * STATE_UPLOAD_COMPLETED
 * 
 * @author tieubao
 * 
 */
public class ContainerSessionUpdatedEvent {

	private final List<ContainerSession> listContainerSessions;

	public ContainerSessionUpdatedEvent(ContainerSession upload) {
		listContainerSessions = new ArrayList<ContainerSession>();
		listContainerSessions.add(upload);
	}

	public ContainerSessionUpdatedEvent(List<ContainerSession> uploads) {
		listContainerSessions = uploads;
	}

	public ContainerSession getTarget() {
		if (isSingleChange())
			return listContainerSessions.get(0);
		else
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
	}

	public List<ContainerSession> getTargets() {
		return listContainerSessions;
	}

	public boolean isSingleChange() {
		return listContainerSessions.size() == 1;
	}

}
