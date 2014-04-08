package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;

/**
 * 
 * Trigger when container session was deleted, created, edited. View
 * `ContainerSessionDaoImpl` for more information.
 * 
 * @author tieubao
 * 
 */

public class ContainerSessionChangedEvent {

	private final List<ContainerSession> listContainerSessions;

	public ContainerSessionChangedEvent(ContainerSession containerSession) {
		listContainerSessions = new ArrayList<ContainerSession>();
		listContainerSessions.add(containerSession);
	}

	public ContainerSessionChangedEvent(List<ContainerSession> containerSessions) {
		listContainerSessions = containerSessions;
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
