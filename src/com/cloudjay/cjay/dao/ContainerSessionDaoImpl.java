package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class ContainerSessionDaoImpl extends
		BaseDaoImpl<ContainerSession, Integer> implements IContainerSessionDao {

	protected ContainerSessionDaoImpl(ConnectionSource connectionSource,
			Class<ContainerSession> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	@Override
	public List<ContainerSession> getAllContainerSessions() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListContainerSessions(
			List<ContainerSession> containerSessions) throws SQLException {
		for (ContainerSession containerSession : containerSessions) {
			this.createOrUpdate(containerSession);
		}
	}

	@Override
	public void addContainerSessions(ContainerSession containerSession)
			throws SQLException {
		this.createOrUpdate(containerSession);
	}

	@Override
	public void deleteAllContainerSessions() throws SQLException {
		List<ContainerSession> containerSessions = getAllContainerSessions();
		for (ContainerSession containerSession : containerSessions) {
			this.delete(containerSession);
		}
	}

}
