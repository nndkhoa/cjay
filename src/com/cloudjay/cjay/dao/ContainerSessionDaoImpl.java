package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class ContainerSessionDaoImpl extends
		BaseDaoImpl<ContainerSession, String> implements IContainerSessionDao {

	public ContainerSessionDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, ContainerSession.class);
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

	@Override
	public boolean isEmpty() throws SQLException {
		ContainerSession containerSession = this.queryForFirst(this
				.queryBuilder().prepare());
		if (null == containerSession)
			return true;

		return false;
	}

	@Override
	public ContainerSession findByUuid(String uuid) throws SQLException {
		List<ContainerSession> result = this.queryForEq("uuid", uuid);
		if (result != null && result.size() > 0) {
			return result.get(0);
		}

		return null;
	}

	@Override
	public ContainerSession getNextWaiting() throws SQLException {
		List<ContainerSession> result = this.queryForEq("state",
				ContainerSession.STATE_UPLOAD_WAITING);

		if (result != null && result.size() > 0) {
			return result.get(0);
		}

		return null;
	}

}
