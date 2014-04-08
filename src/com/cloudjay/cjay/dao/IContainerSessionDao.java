package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IContainerSessionDao extends Dao<ContainerSession, String> {
	void addContainerSession(ContainerSession containerSession) throws SQLException;

	void addListContainerSessions(List<ContainerSession> containerSessions) throws SQLException;

	void delete(int id) throws SQLException;

	void deleteAllContainerSessions() throws SQLException;

	ContainerSession findByUuid(String uuid) throws SQLException;

	List<ContainerSession> getAllContainerSessions() throws SQLException;

	List<ContainerSession> getListCheckOutContainerSessions() throws SQLException;

	List<ContainerSession> getListFixedContainerSessions() throws SQLException;

	List<ContainerSession> getListNotReportedContainerSessions() throws SQLException;

	List<ContainerSession> getListPendingContainerSessions() throws SQLException;

	List<ContainerSession> getListReportedContainerSessions() throws SQLException;

	List<ContainerSession> getListReportingContainerSessions() throws SQLException;

	List<ContainerSession> getListUploadContainerSessions() throws SQLException;

	List<ContainerSession> getLocalContainerSessions() throws SQLException;

	ContainerSession getNextWaiting() throws SQLException;

	List<ContainerSession> getNotUploadedContainerSessions() throws SQLException;

	boolean isEmpty() throws SQLException;

}
