package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import android.database.Cursor;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

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

		long startTime = System.currentTimeMillis();
		Logger.Log("*** add List of Container Sessions ***");
		if (containerSessions != null) {
			for (ContainerSession containerSession : containerSessions) {
				addContainerSession(containerSession);
			}
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));
	}

	private void bulkInsertDataByCallBatchTasks(
			final List<ContainerSession> containerSessions) throws SQLException {

		long startTime = System.currentTimeMillis();
		Logger.Log("*** bulkInsertDataByCallBatchTasks ***");
		if (containerSessions != null) {

			this.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (ContainerSession containerSession : containerSessions) {
						addContainerSession(containerSession);
					}
					return null;
				}

			});
		}

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));
	}

	private void bulkInsertDataBySavePoint(
			final List<ContainerSession> containerSessions) {

		long startTime = System.currentTimeMillis();
		Logger.Log("***bulkInsertDataBySavePoint***");

		if (containerSessions != null) {

			DatabaseConnection conn = null;
			Savepoint savepoint = null;
			try {
				conn = this.startThreadConnection();
				savepoint = conn.setSavePoint("bulk_insert");
				for (ContainerSession containerSession : containerSessions) {
					addContainerSession(containerSession);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						conn.commit(savepoint);
						this.endThreadConnection(conn);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));

	}

	@Override
	public void addContainerSession(ContainerSession containerSession)
			throws SQLException {

		if (containerSession != null) {
			int containerSessionId = containerSession.getId();

			if (containerSessionId == 0) { // new Container Session
				Logger.Log(

				"Insert new Container with ID: "
						+ Integer.toString(containerSessionId) + " Name: "
						+ containerSession.getContainerId());
				this.createOrUpdate(containerSession);

			} else { // existed Container Session

				ContainerSession result = this
						.queryForFirst(this
								.queryBuilder()
								.where()
								.eq(ContainerSession.FIELD_ID,
										containerSession.getId()).prepare());

				// update UUID if needed
				if (null != result) {
					Logger.Log("Update container session UUID");
					containerSession.setUuid(result.getUuid());
				}

				Logger.Log("Update Container Session with ID: "
						+ Integer.toString(containerSessionId) + " | Name: "
						+ containerSession.getContainerId());

				this.createOrUpdate(containerSession);
			}
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
	public void delete(int id) throws SQLException {

		if (id == -1) {
			Logger.Log("Container Session ID = -1");

		} else { // existed Container Session

			ContainerSession result = this.queryForFirst(this.queryBuilder()
					.where().eq(ContainerSession.FIELD_ID, id).prepare());

			if (null != result) {
				this.delete(result);
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
		List<ContainerSession> result = this.queryForEq(
				ContainerSession.FIELD_UUID, uuid);
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

		// Logger.Log( "getNextWaiting() at ContainerSessionDaoImpl");

		ContainerSession result = null;
		List<ContainerSession> containerSessions = this
				.query(this
						.queryBuilder()
						.where()
						.eq(ContainerSession.FIELD_STATE,
								ContainerSession.STATE_UPLOAD_WAITING).and()
						.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, true)
						.prepare());

		Logger.Log("Number of containers in queue: "
				+ Integer.toString(containerSessions.size()));
		for (ContainerSession containerSession : containerSessions) {

			boolean flag = true;
			Collection<CJayImage> cJayImages = containerSession.getCJayImages();

			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getUploadState() != CJayImage.STATE_UPLOAD_COMPLETED) {

					// Logger.e(containerSession.getContainerId()
					// + ": Some cJayImages are still not uploaded.");
					//
					// Logger.e("CJayImage Url: " + cJayImage.getUri());
					// Logger.e("CJayImage Type: "
					// + Integer.toString(cJayImage.getType()));
					// Logger.e("CJayImage Upload State: "
					// + Integer.toString(cJayImage.getUploadState()));

					// TODO: Try to upload CJayImage

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
	@Override
	public List<ContainerSession> getListUploadContainerSessions()
			throws SQLException {

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get List UPLOAD Container Sessions ***");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder().where()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, true).and()
				.eq(ContainerSession.FIELD_CLEARED, false).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference) + " ms");

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

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get list LOCAL Container sessions ***");
		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder()
				.where()
				.eq(ContainerSession.FIELD_LOCAL, true)
				.and()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.and()
				.ne(ContainerSession.FIELD_STATE,
						ContainerSession.STATE_UPLOAD_COMPLETED).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference) + " ms");

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

		long startTime = System.currentTimeMillis();

		Logger.Log("*** get List CHECKOUT Container Sessions ***");
		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder().where()
				.eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").and()
				.eq(ContainerSession.FIELD_LOCAL, false).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));

		return containerSessions;
	}

	public PreparedQuery<ContainerSession> getListCheckOutPreparedQuery()
			throws SQLException {

		return this.queryBuilder().where()
				.eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").and()
				.eq(ContainerSession.FIELD_LOCAL, false).prepare();
	}

	public Cursor filterCheckOutCursor(CharSequence constraint)
			throws SQLException {

		Cursor cursor = null;

		QueryBuilder<ContainerSession, String> queryBuilder = this
				.queryBuilder();

		queryBuilder.where().eq(ContainerSession.FIELD_CHECK_OUT_TIME, "")
				.and().eq(ContainerSession.FIELD_LOCAL, false);

		CloseableIterator<ContainerSession> iterator = this
				.iterator(queryBuilder.prepare());

		try {
			// get the raw results which can be cast under Android
			AndroidDatabaseResults results = (AndroidDatabaseResults) iterator
					.getRawResults();
			cursor = results.getRawCursor();

		} finally {

			// iterator.closeQuietly();
		}

		return cursor;
	}

	public Cursor getCheckOutContainerSessionCursor() throws SQLException {

		Cursor cursor = null;

		QueryBuilder<ContainerSession, String> queryBuilder = this
				.queryBuilder();

		queryBuilder.where().eq(ContainerSession.FIELD_CHECK_OUT_TIME, "")
				.and().eq(ContainerSession.FIELD_LOCAL, false);

		// ContainerDaoImpl containerDaoImpl = new ContainerDaoImpl(
		// connectionSource);
		// QueryBuilder<Container, Integer> containerQueryBuilder =
		// containerDaoImpl
		// .queryBuilder();
		//
		// queryBuilder.join(containerQueryBuilder);

		CloseableIterator<ContainerSession> iterator = this
				.iterator(queryBuilder.prepare());

		try {
			// get the raw results which can be cast under Android
			AndroidDatabaseResults results = (AndroidDatabaseResults) iterator
					.getRawResults();
			cursor = results.getRawCursor();

		} finally {

			// iterator.closeQuietly();
		}

		return cursor;
	}

	@Override
	public List<ContainerSession> getListReportedContainerSessions()
			throws SQLException {
		// Logger.Log( "getListReportedContainerSessions()");
		//
		// List<ContainerSession> containerSessions =
		// getNotUploadedContainerSessions();
		// List<ContainerSession> reportedContainerSessions = new
		// ArrayList<ContainerSession>();
		//
		// for (ContainerSession containerSession : containerSessions) {
		// boolean hasReportTypeImages = false;
		// boolean hasUnreportedImages = false;
		// for (CJayImage cJayImage : containerSession.getCJayImages()) {
		// if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
		// hasReportTypeImages = true;
		// if (cJayImage.getIssue() == null) {
		// hasUnreportedImages = true;
		// break;
		// }
		// }
		// }
		//
		// // reported container sessions:
		// // - containers have report images,
		// // - and report images have issue
		// if (hasReportTypeImages && !hasUnreportedImages) {
		// reportedContainerSessions.add(containerSession);
		// }
		// }
		//
		// return reportedContainerSessions;

		return null; // Vu: don't need this function for now. But keep for
						// reference
	}

	@Override
	public List<ContainerSession> getListNotReportedContainerSessions()
			throws SQLException {
		Logger.Log("*** get List NOT REPORTED Container Sessions ***");

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

		Logger.Log("*** get List REPORTING Container Sessions ***");

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

			// reporting container sessions:
			// - containers have report images,
			if (hasReportTypeImages) {
				reportingContainerSessions.add(containerSession);
			}
		}

		return reportingContainerSessions;
	}

	@Override
	public List<ContainerSession> getListPendingContainerSessions()
			throws SQLException {
		Logger.Log("*** get List PENDING Container Sessions ***");

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

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get List FIXED Container Sessions ***");

		List<ContainerSession> containerSessions = this.query(this
				.queryBuilder()
				.where()
				.eq(ContainerSession.FIELD_FIXED, true)
				.and()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.and()
				.ne(ContainerSession.FIELD_STATE,
						ContainerSession.STATE_UPLOAD_COMPLETED).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));

		return containerSessions;
	}

	@Override
	public List<ContainerSession> getNotUploadedContainerSessions()
			throws SQLException {
		Logger.Log("*** get List NOT UPLOADED Container Sessions ***");
		return this.query(this.queryBuilder().where()
				.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false)
				.prepare());
	}
}
