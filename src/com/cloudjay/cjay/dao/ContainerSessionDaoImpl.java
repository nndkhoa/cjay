package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.dao.BaseDaoImpl;
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
		if (containerSessions != null) {
			for (ContainerSession containerSession : containerSessions) {
				this.createOrUpdate(containerSession);
			}
		}
	}

	@Override
	public void addContainerSessions(ContainerSession containerSession)
			throws SQLException {
		if (containerSession != null) {
			this.createOrUpdate(containerSession);
		}
	}

	@Override
	public void deleteAllContainerSessions() throws SQLException {
		List<ContainerSession> containerSessions = getAllContainerSessions();

		if (containerSessions != null) {
			for (ContainerSession containerSession : containerSessions) {
				this.delete(containerSession);
			}
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

	/**
	 * 
	 * return ContainerSession obj that has
	 * 
	 * - upload_confirmation = true
	 * 
	 * - upload state = WAITING
	 * 
	 * - all cjayimages are uploaded
	 * 
	 */
	@Override
	public ContainerSession getNextWaiting() throws SQLException {

		// Logger.Log(LOG_TAG, "getNextWaiting() at ContainerSessionDaoImpl");

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
				// int uploadState = cJayImage.getUploadState();
				// if (uploadState == CJayImage.STATE_NONE || uploadState == cj)

				if (cJayImage.getUploadState() != CJayImage.STATE_UPLOAD_COMPLETED
						&& cJayImage.getUploadState() != CJayImage.STATE_UPLOAD_IN_PROGRESS) {

					Logger.Log(LOG_TAG,
							"Some cJayImages are still not uploaded");
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
	 * - Have uploaded
	 * 
	 * - Clear state = false
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

	/**
	 * Get all containers which available on Import Fragment.
	 * 
	 * - Upload confirmation = false
	 * 
	 * - Upload state <> complete
	 * 
	 * - Local = true
	 */
	@Override
	public List<ContainerSession> getLocalContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getLocalContainerSessions()");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder()
				.where()
				.eq(ContainerSession.FIELD_LOCAL, true)
				.and()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.and()
				.ne(ContainerSession.FIELD_STATE,
						ContainerSession.STATE_UPLOAD_COMPLETED).prepare());

		return containerSessions;
	}

	/**
	 * Get all containers that have not uploaded yet
	 * 
	 * - check_out_time = null
	 * 
	 * -
	 */
	@Override
	public List<ContainerSession> getListCheckOutContainerSessions()
			throws SQLException {

		Logger.Log(LOG_TAG, "getListCheckOutContainerSessions()");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder().where()
				.eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").prepare());

		return containerSessions;
	}

	@Override
	public List<ContainerSession> getListReportedContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getListReportedContainerSessions()");
		
		List<ContainerSession> containerSessions = getNotUploadedContainerSessions();
		List<ContainerSession> reportedContainerSessions = new ArrayList<ContainerSession>();

		for (ContainerSession containerSession : containerSessions) {
			boolean hasReportTypeImages = false;
			boolean hasUnreportedImages = false;
			for (CJayImage cJayImage : containerSession.getCJayImages()) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
					hasReportTypeImages = true;
					if (cJayImage.getIssue() == null) {
						hasUnreportedImages = true;
						break;
					}
				}
			}

			// reported container sessions:
			// - containers have report images,
			// - and report images have issue
			if (hasReportTypeImages && !hasUnreportedImages) {
				reportedContainerSessions.add(containerSession);
			}
		}

		return reportedContainerSessions;
	}
	
	@Override
	public List<ContainerSession> getListNotReportedContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getListNotReportedContainerSessions()");

		List<ContainerSession> containerSessions = getNotUploadedContainerSessions();
		List<ContainerSession> reportingContainerSessions = new ArrayList<ContainerSession>();

		for (ContainerSession containerSession : containerSessions) {
			boolean hasReportTypeImages = false;
			for (CJayImage cJayImage : containerSession.getCJayImages()) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
					hasReportTypeImages = true;
					break;
				}
			}

			// unreported container sessions:
			// - containers don't have report images
			if (!hasReportTypeImages) {
				reportingContainerSessions.add(containerSession);
			}
		}

		return reportingContainerSessions;
	}

	@Override
	public List<ContainerSession> getListReportingContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getListReportingContainerSessions()");

		List<ContainerSession> containerSessions = getNotUploadedContainerSessions();
		List<ContainerSession> reportingContainerSessions = new ArrayList<ContainerSession>();

		for (ContainerSession containerSession : containerSessions) {
			boolean hasReportTypeImages = false;
			boolean hasUnreportedImages = false;
			for (CJayImage cJayImage : containerSession.getCJayImages()) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
					hasReportTypeImages = true;
					if (cJayImage.getIssue() == null) {
						hasUnreportedImages = true;
						break;
					}
				}
			}

			// reporting container sessions:
			// - containers have report images,
			// - and report images don't have issue
			if (hasReportTypeImages && hasUnreportedImages) {
				reportingContainerSessions.add(containerSession);
			}
		}

		return reportingContainerSessions;
	}

	@Override
	public List<ContainerSession> getListPendingContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getListPendingContainerSessions()");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder()
				.where()
				.eq(ContainerSession.FIELD_FIXED, false)
				.and()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.and()
				.ne(ContainerSession.FIELD_STATE,
						ContainerSession.STATE_UPLOAD_COMPLETED).prepare());

		return containerSessions;
	}

	@Override
	public List<ContainerSession> getListFixedContainerSessions()
			throws SQLException {
		Logger.Log(LOG_TAG, "getListFixedContainerSessions()");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder()
				.where()
				.eq(ContainerSession.FIELD_FIXED, true)
				.and()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.and()
				.ne(ContainerSession.FIELD_STATE,
						ContainerSession.STATE_UPLOAD_COMPLETED).prepare());

		return containerSessions;
	}
	
	private List<ContainerSession> getNotUploadedContainerSessions() throws SQLException {
		return this.query(this
				.queryBuilder().where()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.prepare());
	}
}
