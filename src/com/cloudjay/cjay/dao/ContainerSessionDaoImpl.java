package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class ContainerSessionDaoImpl extends
		BaseDaoImpl<ContainerSession, String> implements IContainerSessionDao {

	private static final String LOG_TAG = "ContainerSessionDaoImpl";

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

		Logger.Log(LOG_TAG, "getNextWaiting() at ContainerSessionDaoImpl");
		ContainerSession result = null;

		List<ContainerSession> containerSessions = this
				.query(this
						.queryBuilder()
						.where()
						.eq(ContainerSession.FIELD_STATE,
								ContainerSession.STATE_UPLOAD_WAITING).and()
						.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, true)
						.prepare());

		for (ContainerSession containerSession : containerSessions) {

			boolean flag = true;
			Collection<CJayImage> cJayImages = containerSession.getCJayImages();

			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getUploadState() != CJayImage.STATE_UPLOAD_COMPLETED) {
					flag = false;
					break;
				}
			}

			if (flag == true) {
				result = containerSession;
			}
		}

		return result;
	}

	/**
	 * Get all container session items should be show on Upload Fragment.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<ContainerSession> getListUploadContainerSessions()
			throws SQLException {

		Logger.Log(LOG_TAG, "getListUploadContainerSessions()");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder().where()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, true).and()
				.eq(ContainerSession.FIELD_CLEARED, false).prepare());

		return containerSessions;

	}
}
