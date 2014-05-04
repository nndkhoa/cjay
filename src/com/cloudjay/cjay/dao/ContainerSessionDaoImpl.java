package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.events.LogUserActivityEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.service.PhotoUploadService_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import de.greenrobot.event.EventBus;

public class ContainerSessionDaoImpl extends BaseDaoImpl<ContainerSession, String> implements IContainerSessionDao {

	public ContainerSessionDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, ContainerSession.class);
	}

	/**
	 * Chỉ gọi hàm này khi tạo mới local container session
	 */
	@Override
	public void addContainerSession(ContainerSession containerSession) throws SQLException {
		createOrUpdate(containerSession);

		// if (containerSession != null) {
		// int containerSessionId = containerSession.getId();
		//
		// if (containerSessionId == 0) { // new Container Session
		//
		// // Logger.Log("Insert new Container with ID: " + Integer.toString(containerSessionId) + " Name: "
		// // + containerSession.getContainerId());
		// createOrUpdate(containerSession);
		//
		// } else { // existed Container Session
		//
		// ContainerSession result = queryForFirst(queryBuilder().where()
		// .eq(ContainerSession.FIELD_ID,
		// containerSession.getId()).prepare());
		//
		// // update UUID if needed
		// if (null != result) {
		// // Logger.Log("Update container session UUID");
		// containerSession.setUuid(result.getUuid());
		// }
		//
		// // Logger.Log("Update Container Session with ID: " + Integer.toString(containerSessionId) + " | Name: "
		// // + containerSession.getContainerId());
		//
		// createOrUpdate(containerSession);
		// }
		// }
	}

	@Override
	public void addListContainerSessions(List<ContainerSession> containerSessions) throws SQLException {

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

	public void bulkInsert(SQLiteDatabase db, List<ContainerSession> containerSessions) {

		try {
			db.beginTransaction();

			for (ContainerSession containerSession : containerSessions) {

				if (containerSession != null) {

					int containerSessionId = containerSession.getId();

					if (containerSessionId != 0) {
						String sql = "select " + ContainerSession.FIELD_UUID + " from container_session where "
								+ ContainerSession.FIELD_ID + " = " + containerSessionId;

						Cursor cursor = db.rawQuery(sql, new String[] {});

						if (cursor.moveToFirst()) {
							String containerSessionUuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));

							containerSession.setUuid(containerSessionUuid);
						}
					}
				}

				String sql = "insert or replace into container_session VALUES ('"
						+ containerSession.getRawCheckInTime() + "', '"
						+ Utils.stripNull(containerSession.getRawCheckOutTime()) + "', '" + containerSession.getUuid()
						+ "', " + containerSession.getContainer().getId() + ", '"
						+ Utils.stripNull(containerSession.getImageIdPath()) + "', " + containerSession.getId() + ", "
						+ Utils.toInt(containerSession.isFixed()) + ", " + Utils.toInt(containerSession.isExport())
						+ ", " + containerSession.getUploadState() + ", " + Utils.toInt(containerSession.isOnLocal())
						+ ", " + Utils.toInt(containerSession.hasUploadConfirmed()) + ", "
						+ Utils.toInt(containerSession.isCleared()) + ", " + containerSession.getUploadType() + ")";

				db.execSQL(sql);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void bulkInsertDataByCallBatchTasks(final List<ContainerSession> containerSessions) throws SQLException {

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

	public void bulkInsertDataBySavePoint(final List<ContainerSession> containerSessions) {

		long startTime = System.currentTimeMillis();
		// Logger.Log("***bulkInsertDataBySavePoint***");

		if (containerSessions != null) {

			DatabaseConnection conn = null;
			Savepoint savepoint = null;
			try {

				conn = startThreadConnection();
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
						endThreadConnection(conn);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}

		long difference = System.currentTimeMillis() - startTime;
		// Logger.Log("---> Total time: " + Long.toString(difference));
	}

	@Override
	public void delete(int id) throws SQLException {

		if (id == -1) {
			Logger.e("Container Session ID = -1");
			return;
		} else { // existed Container Session

			ContainerSession result = queryForFirst(queryBuilder().where().eq(ContainerSession.FIELD_ID, id).prepare());

			if (null != result) {

				Logger.Log("Remove container " + result.getContainerId());
				this.delete(result);

			}
		}
	}

	public void updateServerState(int id, int state) throws SQLException {

		if (id == -1) {

			Logger.e("Container Session ID = -1");
			return;

		} else {

			ContainerSession result = queryForFirst(queryBuilder().where().eq(ContainerSession.FIELD_ID, id).prepare());

			if (null != result) {
				Logger.Log("Update ServerState of container " + result.getContainerId() + " to: " + state);
				result.setServerState(state);
				this.update(result);
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

	public Cursor filterCheckOutCursor(CharSequence constraint) throws SQLException {

		Cursor cursor = null;

		QueryBuilder<ContainerSession, String> queryBuilder = queryBuilder();

		queryBuilder.where().eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").and()
					.eq(ContainerSession.FIELD_LOCAL, false);

		CloseableIterator<ContainerSession> iterator = this.iterator(queryBuilder.prepare());

		try {
			// get the raw results which can be cast under Android
			AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
			cursor = results.getRawCursor();

		} finally {

			// iterator.closeQuietly();
		}

		return cursor;
	}

	@Override
	public ContainerSession findByUuid(String uuid) throws SQLException {
		List<ContainerSession> result = queryForEq(ContainerSession.FIELD_UUID, uuid);
		if (result != null && result.size() > 0) return result.get(0);

		return null;
	}

	@Override
	public List<ContainerSession> getAllContainerSessions() throws SQLException {
		return queryForAll();
	}

	public Cursor getCheckOutContainerSessionCursor() throws SQLException {

		Cursor cursor = null;

		QueryBuilder<ContainerSession, String> queryBuilder = queryBuilder();

		queryBuilder.where().eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").and()
					.eq(ContainerSession.FIELD_LOCAL, false);

		// ContainerDaoImpl containerDaoImpl = new ContainerDaoImpl(
		// connectionSource);
		// QueryBuilder<Container, Integer> containerQueryBuilder =
		// containerDaoImpl
		// .queryBuilder();
		//
		// queryBuilder.join(containerQueryBuilder);

		CloseableIterator<ContainerSession> iterator = this.iterator(queryBuilder.prepare());

		try {
			// get the raw results which can be cast under Android
			AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
			cursor = results.getRawCursor();

		} finally {

			// iterator.closeQuietly();
		}

		return cursor;
	}

	/**
	 * Get all containers that have not uploaded yet
	 * 
	 * - check_out_time = null
	 * 
	 * -
	 */
	@Override
	public List<ContainerSession> getListCheckOutContainerSessions() throws SQLException {

		long startTime = System.currentTimeMillis();

		Logger.Log("*** get List CHECKOUT Container Sessions ***");
		List<ContainerSession> containerSessions = query(queryBuilder().where()
																		.eq(ContainerSession.FIELD_CHECK_OUT_TIME, "")
																		.and().eq(ContainerSession.FIELD_LOCAL, false)
																		.prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));

		return containerSessions;
	}

	public PreparedQuery<ContainerSession> getListCheckOutPreparedQuery() throws SQLException {

		return queryBuilder().where().eq(ContainerSession.FIELD_CHECK_OUT_TIME, "").and()
								.eq(ContainerSession.FIELD_LOCAL, false).prepare();
	}

	@Override
	public List<ContainerSession> getListFixedContainerSessions() throws SQLException {

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get List FIXED Container Sessions ***");

		List<ContainerSession> containerSessions = query(queryBuilder().where()
																		.eq(ContainerSession.FIELD_FIXED, true)
																		.and()
																		.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION,
																			false)
																		.and()
																		.ne(ContainerSession.FIELD_STATE,
																			UploadState.COMPLETED.getValue()).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference));

		return containerSessions;
	}

	@Override
	public List<ContainerSession> getListNotReportedContainerSessions() throws SQLException {
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
	public List<ContainerSession> getListPendingContainerSessions() throws SQLException {
		Logger.Log("*** get List PENDING Container Sessions ***");

		List<ContainerSession> containerSessions = query(queryBuilder().where()
																		.eq(ContainerSession.FIELD_FIXED, false)
																		.and()
																		.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION,
																			false)
																		.and()
																		.ne(ContainerSession.FIELD_STATE,
																			UploadState.COMPLETED.getValue()).prepare());

		return containerSessions;
	}

	@Override
	public List<ContainerSession> getListReportedContainerSessions() throws SQLException {
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
	public List<ContainerSession> getListReportingContainerSessions() throws SQLException {

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
	public List<ContainerSession> getListUploadContainerSessions() throws SQLException {

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get List UPLOAD Container Sessions ***");

		List<ContainerSession> containerSessions = query(queryBuilder().where()
																		.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION,
																			true).and()
																		.eq(ContainerSession.FIELD_CLEARED, false)
																		.prepare());

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
	public List<ContainerSession> getLocalContainerSessions() throws SQLException {

		long startTime = System.currentTimeMillis();
		Logger.Log("*** get list LOCAL Container sessions ***");
		List<ContainerSession> containerSessions = query(queryBuilder().where()
																		.eq(ContainerSession.FIELD_LOCAL, true)
																		.and()
																		.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION,
																			false)
																		.and()
																		.ne(ContainerSession.FIELD_STATE,
																			UploadState.COMPLETED.getValue()).prepare());

		long difference = System.currentTimeMillis() - startTime;
		Logger.Log("---> Total time: " + Long.toString(difference) + " ms");

		return containerSessions;
	}

	HashMap<String, Integer> retryCountHashMap = new HashMap<String, Integer>();

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
	public ContainerSession getNextWaiting(Context ctx, SQLiteDatabase db) throws SQLException {

		// Logger.Log( "getNextWaiting() at ContainerSessionDaoImpl");

		ContainerSession result = null;

		QueryBuilder<ContainerSession, String> queryBuilder = queryBuilder();
		queryBuilder.where().eq(ContainerSession.FIELD_STATE, UploadState.WAITING.getValue()).and()
					.eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, true);
		queryBuilder.orderBy(ContainerSession.FIELD_CHECK_IN_TIME, false);
		List<ContainerSession> containerSessions = query(queryBuilder.prepare());

		if (containerSessions == null || containerSessions.size() <= 0) {

			PreferencesUtil.storePrefsValue(ctx, PreferencesUtil.PREF_EMPTY_CONTAINER_QUEUE, true);
			if (Utils.canStopAlarm(ctx) && Utils.isAlarmUp(ctx)) {
				// Logger.Log("No more item to upload. Stop Alarm.");
				// Utils.cancelAlarm(ctx);
			}

			return null;
		} else {

			PreferencesUtil.storePrefsValue(ctx, PreferencesUtil.PREF_EMPTY_CONTAINER_QUEUE, false);
			Logger.w("Total items in ContainerQueue: " + Integer.toString(containerSessions.size()));
		}

		for (ContainerSession containerSession : containerSessions) {

			// imma return if it's temporary container
			// and it should only activate in Gate app
			if (containerSession.isOnLocal() && containerSession.getUploadType() == UploadType.NONE.getValue()) {

				Logger.w("Temporary upload detected. Return " + containerSession.getContainerId());
				EventBus.getDefault().post(	new LogUserActivityEvent(containerSession.getContainerId()
													+ " | #Temporary upload detected."));
				return containerSession;
			}

			boolean flag = true;
			Collection<CJayImage> cJayImages = containerSession.getCJayImages();
			for (CJayImage cJayImage : cJayImages) {

				synchronized (cJayImage) {
					int uploadState = cJayImage.getUploadState();
					if (uploadState != CJayImage.STATE_UPLOAD_COMPLETED) {

						String key = cJayImage.getUuid();
						Logger.Log(cJayImage.getImageName() + " | " + UploadState.values()[uploadState]);
						if (uploadState == CJayImage.STATE_NONE && cJayImage.getUri().startsWith("http")) {

							Logger.w("This cjay image is already stay in server.");
							String sql = "UPDATE cjay_image SET state = " + CJayImage.STATE_UPLOAD_COMPLETED
									+ " WHERE " + CJayImage.FIELD_UUID + " = '" + key + "'";

							db.execSQL(sql);
							break;
						}

						// Increase retry count
						if (retryCountHashMap.containsKey(key)) {

							int count = retryCountHashMap.get(key);
							count++;
							retryCountHashMap.put(key, count);

							Logger.Log(containerSession.getContainerId() + " | Retry count: " + Integer.toString(count));

							// Retry to upload cjayimage
							if (count >= CJayConstant.RETRY_THRESHOLD
									&& !Utils.isRunning(ctx, PhotoUploadService_.class.getName())) {

								Logger.w("Retry to upload CJayImage : " + cJayImage.getImageName());
								EventBus.getDefault().post(	new LogUserActivityEvent("#Retry to upload CJayImage: "
																	+ cJayImage.getImageName() + " | Container: "
																	+ containerSession.getContainerId()));

								// Refresh cjay_image type
								// UPDATE cjay_image SET state = 1, time_posted = '2014-03-21T15:33:01' WHERE uuid =
								// '<uuid>'
								String sql = "UPDATE cjay_image SET state = "
										+ CJayImage.STATE_UPLOAD_WAITING
										+ ", time_posted = '"
										+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE)
										+ "' WHERE " + CJayImage.FIELD_UUID + " = '" + key + "'";

								db.execSQL(sql);

								Intent i = new Intent();
								i.setAction(CJayConstant.INTENT_PHOTO_TAKEN);
								ctx.sendBroadcast(i);

								retryCountHashMap.remove(key);
							}

						} else {
							retryCountHashMap.put(cJayImage.getUuid(), 0);
						}

						flag = false;
						break;
					}
				}

			}

			if (flag == true) {
				result = containerSession;
				Logger.Log("Result: " + containerSession.getContainerId());
			}
		}

		return result;
	}

	@Override
	public List<ContainerSession> getNotUploadedContainerSessions() throws SQLException {
		Logger.Log("*** get List NOT UPLOADED Container Sessions ***");
		return query(queryBuilder().where().eq(ContainerSession.FIELD_UPLOAD_CONFIRMATION, false).prepare());
	}

	@Override
	public boolean isEmpty() throws SQLException {
		ContainerSession containerSession = queryForFirst(queryBuilder().prepare());
		if (null == containerSession) return true;

		return false;
	}
}
