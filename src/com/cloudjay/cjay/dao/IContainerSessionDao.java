package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IContainerSessionDao extends Dao<ContainerSession, String> {
	List<ContainerSession> getAllContainerSessions() throws SQLException;

	List<ContainerSession> getLocalContainerSessions() throws SQLException;

	List<ContainerSession> getListUploadContainerSessions() throws SQLException;

	List<ContainerSession> getListCheckOutContainerSessions()
			throws SQLException;

	void addListContainerSessions(List<ContainerSession> containerSessions)
			throws SQLException;

	void addContainerSessions(ContainerSession containerSession)
			throws SQLException;

	void deleteAllContainerSessions() throws SQLException;

	boolean isEmpty() throws SQLException;

	ContainerSession findByUuid(String uuid) throws SQLException;

	ContainerSession getNextWaiting() throws SQLException;

}
