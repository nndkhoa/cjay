package com.cloudjay.cjay.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.Trace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.dao.UserDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.ContainerSessionResult;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import de.greenrobot.event.EventBus;

/**
 * 
 * Nơi tập trung các hàm xử lý trả kết quả từ Server hoặc từ Local Database.
 * 
 * 1. Nếu server cập nhật thêm bảng CODE mới --> get data từ server merge vào db
 * rồi thực hiện truy vấn trả kết quả từ db. Tuy nhiên, để tiết kiệm chi phí,
 * việc cập nhật db chỉ được triggered từ "Notification, lúc start app hoặc lúc
 * force refresh".
 * 
 * 2. Khi resume app thực hiện việc kiểm tra để lấy data mới nhất về
 * 
 * 3. Tất cả mọi hàm trả kết quả từ DataCenter phải luôn cho kết quả mới nhất
 * 
 * Note:
 * 
 * - update --> call CJayClient to update data from server.
 * 
 * - get --> get from local database
 * 
 * - remove --> remove from local database
 * 
 * @author tieubao
 * 
 */
@SuppressLint("SimpleDateFormat")
@EBean(scope = Scope.Singleton)
public class DataCenter {

	public static AsyncTask<Void, Integer, Void> LoadDataTask;
	public static AsyncTask<Void, Void, String> RegisterGCMTask;

	private static DataCenter instance = null;
	private IDatabaseManager databaseManager = null;

	public DataCenter() {
	}

	/**
	 * Apply Singleton Pattern and return only one instance of class DataCenter
	 * 
	 * @return instance of Singleton class {@code DataCenter}.
	 */
	public static DataCenter getInstance() {
		if (null == instance) {
			instance = new DataCenter();
		}
		return instance;
	}

	public static DatabaseHelper getDatabaseHelper(Context context) {
		return getInstance().getDatabaseManager().getHelper(context);
	}

	public void initialize(IDatabaseManager databaseManager) {
		Logger.Log("initializing ...");
		this.databaseManager = databaseManager;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public List<Operator> getListOperators(Context context) {
		Logger.Log("get list Operators");

		try {
			return getDatabaseManager().getHelper(context).getOperatorDaoImpl()
					.getAllOperators();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Save credential of user to local database. After that, user is verified
	 * that signed in.
	 * 
	 * @param context
	 * @param token
	 * @return
	 */
	public User saveCredential(Context context, String token) {
		try {

			User currentUser = CJayClient.getInstance().getCurrentUser(token,
					context);

			currentUser.setAccessToken(token);
			currentUser.setMainAccount(true);

			Logger.Log("User role: " + currentUser.getRoleName());
			DepotDaoImpl depotDaoImpl = getDatabaseManager().getHelper(context)
					.getDepotDaoImpl();
			UserDaoImpl userDaoImpl = getDatabaseManager().getHelper(context)
					.getUserDaoImpl();

			Depot result = depotDaoImpl
					.queryForFirst(depotDaoImpl.queryBuilder().where()
							.eq(Depot.DEPOT_CODE, currentUser.getDepotCode())
							.prepare());

			if (null != result) {
				currentUser.setDepot(result);
			} else {
				Depot depot = new Depot();
				depot.setDepotCode(currentUser.getDepotCode());
				depot.setDepotName(currentUser.getDepotCode());
				depotDaoImpl.addDepot(depot);
				currentUser.setDepot(depot);
			}

			userDaoImpl.addUser(currentUser);
			return currentUser;

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cursor getUserLogCursor(Context context) {
		String queryString = "SELECT * FROM user_log";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getDamageCodesCursor(Context context) {

		String queryString = "SELECT id as _id, display_name, code FROM damage_code";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getRepairCodesCursor(Context context) {

		String queryString = "SELECT id as _id, display_name, code FROM repair_code";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getComponentCodesCursor(Context context) {

		String queryString = "SELECT id as _id, display_name, code FROM component_code";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getCheckOutContainerSessionCursor(Context context) {
		String queryString = "SELECT * FROM cs_export_validation_view";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getLocalContainerSessionCursor(Context context) {
		String queryString = "SELECT * FROM cs_import_validation_view";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getNotReportedContainerSessionCursor(Context context) {

		// String queryString = "SELECT cs.* FROM csiview AS cs"
		// + " WHERE cs.upload_confirmation = 0 AND cs._id NOT IN ("
		// + " SELECT csview._id"
		// +
		// " FROM cjay_image JOIN csview ON cjay_image.containerSession_id = csview._id"
		// + " WHERE cjay_image.type = 2)";

		String queryString = "SELECT cs.* FROM csi_auditor_validation_view AS cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs._id NOT IN ("
				+ " SELECT container_session._id"
				+ " FROM cjay_image JOIN container_session ON cjay_image.containerSession_id = container_session._id"
				+ " WHERE cjay_image.type = 2)";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getReportingContainerSessionCursor(Context context) {
		String queryString = "SELECT cs.* FROM csi_auditor_validation_view AS cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs._id IN ("
				+ " SELECT container_session._id"
				+ " FROM cjay_image JOIN container_session ON cjay_image.containerSession_id = container_session._id"
				+ " WHERE cjay_image.type = 2)";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getPendingContainerSessionCursor(Context context) {
		String queryString = "SELECT * FROM csi_repair_validation_view cs WHERE cs.upload_confirmation = 0 AND cs.state <> 4";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getFixedContainerSessionCursor(Context context) {
		String queryString = "SELECT * FROM csi_repair_validation_view cs WHERE cs.upload_confirmation = 0 AND cs.fixed = 1 AND cs.state <> 4";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] {});
	}

	public Cursor getAllContainersCursor(Context context) {

		try {
			return getDatabaseManager().getHelper(context)
					.getContainerDaoImpl().getAllContainersCursor();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Cursor getIssueItemCursorByContainer(Context context,
			String containerSessionUUID, int imageType) {

		String queryString = "SELECT * FROM issue_item_view WHERE containerSession_id LIKE ? AND type = ?";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString,
				new String[] { containerSessionUUID + "%",
						String.valueOf(imageType) });
	}

	public Cursor getCJayImagesCursorByContainer(Context context,
			String containerSessionUUID, int imageType) {
		String queryString = "SELECT * FROM cjay_image WHERE containerSession_id LIKE ? AND type = ?";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString,
				new String[] { containerSessionUUID + "%",
						String.valueOf(imageType) });
	}

	public Cursor filterLocalCursor(Context context, CharSequence constraint) {

		String queryString = "SELECT * FROM cs_import_validation_view"
				+ " WHERE container_id LIKE ? ORDER BY container_id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public Cursor filterUserLogCursor(Context context, CharSequence constraint) {

		String queryString = "SELECT * FROM user_log"
				+ " WHERE (content LIKE ?) OR (time LIKE ?) ORDER BY time LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context)
				.rawQuery(
						queryString,
						new String[] { "%" + constraint + "%",
								"%" + constraint + "%" });
	}

	public Cursor filterCheckoutCursor(Context context, CharSequence constraint) {

		String queryString = "SELECT * FROM cs_export_validation_view"
				+ " WHERE container_id LIKE ? ORDER BY container_id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public Cursor filterDamageCodeCursor(Context context,
			CharSequence constraint) {

		String queryString = "SELECT id as _id, code, display_name FROM damage_code"
				+ " WHERE code LIKE ? ORDER BY id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { "%" + constraint + "%" });
	}

	public Cursor filterRepairCodeCursor(Context context,
			CharSequence constraint) {

		String queryString = "SELECT id as _id, code, display_name FROM repair_code"
				+ " WHERE code LIKE ? ORDER BY id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { "%" + constraint + "%" });
	}

	public Cursor filterComponentCodeCursor(Context context,
			CharSequence constraint) {

		String queryString = "SELECT id as _id, code, display_name FROM component_code"
				+ " WHERE code LIKE ? ORDER BY id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { "%" + constraint + "%" });
	}

	public Cursor filterNotReportedCursor(Context context,
			CharSequence constraint) {

		String queryString = "SELECT cs.* FROM csi_auditor_validation_view AS cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs._id NOT IN "
				+ " (SELECT container_session._id"
				+ " FROM cjay_image JOIN container_session ON cjay_image.containerSession_id = container_session._id"
				+ " WHERE cjay_image.type = 2) AND cs.container_id LIKE ? ORDER BY cs.container_id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public Cursor filterReportingCursor(Context context, CharSequence constraint) {

		String queryString = "SELECT cs.* FROM csi_auditor_validation_view AS cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs._id IN "
				+ " (SELECT container_session._id"
				+ " FROM cjay_image JOIN container_session ON cjay_image.containerSession_id = container_session._id"
				+ " WHERE cjay_image.type = 2) AND cs.container_id LIKE ? ORDER BY cs.container_id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public Cursor filterPendingCursor(Context context, CharSequence constraint) {

		String queryString = "SELECT * FROM csi_repair_validation_view cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs.fixed = 0 AND cs.state <> 4 AND cs.container_id LIKE ? ORDER BY cs.container_id LIMIT 100";
		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public Cursor filterFixedCursor(Context context, CharSequence constraint) {
		String queryString = "SELECT * FROM csi_repair_validation_view cs"
				+ " WHERE cs.upload_confirmation = 0 AND cs.fixed = 1 AND cs.state <> 4 AND cs.container_id LIKE ? ORDER BY cs.container_id LIMIT 100";

		return getDatabaseManager().getReadableDatabase(context).rawQuery(
				queryString, new String[] { constraint + "%" });
	}

	public List<ContainerSession> getListReportedContainerSessions(
			Context context) {
		Logger.Log("get list reported Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListReportedContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListUploadContainerSessions(Context context) {
		try {

			List<ContainerSession> result = getDatabaseManager()
					.getHelper(context).getContainerSessionDaoImpl()
					.getListUploadContainerSessions();

			if (result != null) {
				Logger.Log("Upload list number of items: "
						+ Integer.toString(result.size()));
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Remove Container Session that have following {@code id}
	 * 
	 * @param context
	 * @param id
	 *            {@code id} of container Session
	 * @return {@code true} if remove completed. {@code false} if error happen.
	 * @see ContainerSession
	 * @since 1.0
	 */
	public boolean removeContainerSession(Context context, int id) {
		Logger.Log("remove Container Session with Id = " + Integer.toString(id));
		try {

			getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl().delete(id);
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get new Container Sessions from last time
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to Internet
	 * @throws SQLException
	 * @throws NullSessionException
	 */
	@Trace(level = Log.INFO)
	public void updateListContainerSessions(Context ctx)
			throws NoConnectionException, SQLException, NullSessionException {

		Logger.Log("*** UPDATE LIST CONTAINER SESSIONS ***");
		long startTime = System.currentTimeMillis();
		PreferencesUtil.storePrefsValue(ctx,
				PreferencesUtil.PREF_IS_UPDATING_DATA, true);

		try {
			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
			String nowString = dateFormat.format(new Date());

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			// 3. Update list ContainerSessions
			int page = 1;
			String nextUrl = "";
			String lastUpdate = "";

			if (containerSessionDaoImpl.isEmpty()) {

			} else {

				lastUpdate = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE);

				Logger.Log("get updated list container sessions from last time: "
						+ lastUpdate);
			}

			do {
				List<ContainerSession> containerSessions = new ArrayList<ContainerSession>();
				ContainerSessionResult result = null;

				result = CJayClient.getInstance().getContainerSessionsByPage(
						ctx, lastUpdate, page);

				if (null != result) {
					page = page + 1;
					nextUrl = result.getNext();

					List<TmpContainerSession> tmpContainerSessions = result
							.getResults();

					if (null != tmpContainerSessions) {

						for (TmpContainerSession tmpSession : tmpContainerSessions) {
							ContainerSession containerSession = Mapper
									.getInstance().toContainerSession(
											tmpSession, ctx);

							if (null != containerSession) {
								containerSessions.add(containerSession);
							}

						}
					}

					containerSessionDaoImpl
							.bulkInsertDataBySavePoint(containerSessions);

					// containerSessionDaoImpl
					// .bulkInsertDataByCallBatchTasks(containerSessions);

					if (null != containerSessions
							&& !containerSessions.isEmpty()) {

						EventBus.getDefault().post(
								new ContainerSessionChangedEvent(
										containerSessions));

					}
				}

			} while (!TextUtils.isEmpty(nextUrl));

			
			// List<ContainerSession> containerSessions = new
			// ArrayList<ContainerSession>();
			// List<TmpContainerSession> tmpContainerSessions = CJayClient
			// .getInstance().getContainerSessions(ctx, lastUpdate);
			//
			// if (null != tmpContainerSessions) {
			//
			// for (TmpContainerSession tmpSession : tmpContainerSessions) {
			// ContainerSession containerSession = Mapper.getInstance()
			// .toContainerSession(tmpSession, ctx);
			//
			// if (null != containerSession) {
			// containerSessions.add(containerSession);
			// }
			//
			// }
			// }
			//
			// containerSessionDaoImpl
			// .bulkInsertDataBySavePoint(containerSessions);

			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
					nowString);

			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_IS_UPDATING_DATA, false);

		} catch (NoConnectionException e) {
			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_IS_UPDATING_DATA, false);
			throw e;
		} catch (SQLException e) {
			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_IS_UPDATING_DATA, false);
			throw e;
		} catch (NullSessionException e) {

			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_IS_UPDATING_DATA, false);
			throw e;
		} catch (Exception e) {
			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_IS_UPDATING_DATA, false);
			e.printStackTrace();
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Get new operators from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 * @throws NullSessionException
	 */
	@SuppressLint("SimpleDateFormat")
	public void updateListOperators(Context ctx) throws NoConnectionException,
			SQLException, NullSessionException {

		Logger.Log("*** UPDATE LIST OPERATORS ***");
		long startTime = System.currentTimeMillis();

		try {

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
			String nowString = dateFormat.format(new Date());

			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();

			List<Operator> operators = null;
			String lastUpdate = "";

			if (operatorDaoImpl.isEmpty()) {

			} else {
				lastUpdate = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE);
			}

			operators = CJayClient.getInstance().getOperators(ctx, lastUpdate);

			if (operators == null) {
				Logger.w("----> NO new operators");
			} else {
				Logger.w("----> Has " + Integer.toString(operators.size())
						+ " new operators");
			}

			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE,
					nowString);

			if (null != operators) {
				operatorDaoImpl.bulkInsert(DataCenter.getDatabaseHelper(ctx)
						.getWritableDatabase(), operators);

			}

		} catch (NoConnectionException e) {
			throw e;
		} catch (NullSessionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Get new damage codes from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 */
	public void updateListDamageCodes(Context ctx)
			throws NoConnectionException, SQLException {

		Logger.Log("*** UPDATE LIST DAMAGE ***");
		long startTime = System.currentTimeMillis();
		try {
			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
			String nowString = dateFormat.format(new Date());
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();

			// Get list damage
			List<DamageCode> damageCodes = null;
			String lastUpdate = "";
			if (damageCodeDaoImpl.isEmpty()) {

			} else {
				lastUpdate = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE);
			}

			damageCodes = CJayClient.getInstance().getDamageCodes(ctx,
					lastUpdate);

			PreferencesUtil
					.storePrefsValue(ctx,
							PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE,
							nowString);

			if (damageCodes == null) {
				Logger.w("----> NO new damage codes");
			} else {
				Logger.w("----> Has " + Integer.toString(damageCodes.size())
						+ " new damage codes");
			}

			if (null != damageCodes) {
				damageCodeDaoImpl.bulkInsert(DataCenter.getDatabaseHelper(ctx)
						.getWritableDatabase(), damageCodes);
			}

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Get new component codes from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 * @throws NullSessionException
	 */
	public void updateListComponentCodes(Context ctx)
			throws NoConnectionException, SQLException, NullSessionException {

		Logger.Log("*** UPDATE LIST COMPONENT ***");
		long startTime = System.currentTimeMillis();
		try {
			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
			String nowString = dateFormat.format(new Date());

			ComponentCodeDaoImpl componentCodeDaoImpl = databaseManager
					.getHelper(ctx).getComponentCodeDaoImpl();

			// Get list Component
			List<ComponentCode> componentCodes = null;
			String lastUpdate = "";
			if (componentCodeDaoImpl.isEmpty()) {

			} else {
				lastUpdate = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE);
			}

			componentCodes = CJayClient.getInstance().getComponentCodes(ctx,
					lastUpdate);

			PreferencesUtil.storePrefsValue(ctx,
					PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE,
					nowString);

			if (componentCodes == null) {
				Logger.w("----> NO new component codes");
			} else {
				Logger.w("----> Has " + Integer.toString(componentCodes.size())
						+ " new component codes");
			}

			if (null != componentCodes) {
				componentCodeDaoImpl
						.bulkInsert(DataCenter.getDatabaseHelper(ctx)
								.getWritableDatabase(), componentCodes);

			}

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (NullSessionException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Get new repair codes from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 */
	public void updateListRepairCodes(Context ctx)
			throws NoConnectionException, SQLException {

		Logger.Log("*** UPDATE LIST REPAIR ***");
		long startTime = System.currentTimeMillis();
		try {
			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
			String nowString = dateFormat.format(new Date());

			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();

			// Get list Repair
			List<RepairCode> repairCodes = null;
			String lastUpdate = "";
			if (repairCodeDaoImpl.isEmpty()) {

			} else {

				lastUpdate = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE);
			}

			repairCodes = CJayClient.getInstance().getRepairCodes(ctx,
					lastUpdate);

			PreferencesUtil
					.storePrefsValue(ctx,
							PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE,
							nowString);

			if (repairCodes == null) {
				Logger.w("----> NO new repair codes");
			} else {
				Logger.w("----> Has " + Integer.toString(repairCodes.size())
						+ " new repair codes");
			}

			if (null != repairCodes) {
				repairCodeDaoImpl.bulkInsert(DataCenter.getDatabaseHelper(ctx)
						.getWritableDatabase(), repairCodes);
			}

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Get new ISO Code from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 * @throws NullSessionException
	 */
	public void updateListISOCode(Context ctx) throws NoConnectionException,
			SQLException, NullSessionException {

		Logger.Log("*** UPDATE ALL ISO CODE ***");
		long startTime = System.currentTimeMillis();

		try {

			updateListOperators(ctx);
			updateListDamageCodes(ctx);
			updateListRepairCodes(ctx);
			updateListComponentCodes(ctx);

		} catch (NullSessionException e) {
			throw e;
		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	/**
	 * Fetch data from server based on current user role.
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws NullSessionException
	 */
	public void fetchData(Context ctx) throws NoConnectionException,
			NullSessionException {

		Logger.Log("*** FETCHING DATA ... ***");

		if (isFetchingData(ctx)) {
			Logger.Log("fetchData() is already running");
			return;
		} else {
			try {
				// Mark that Application
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_IS_FETCHING_DATA, true);

				updateListISOCode(ctx);
				updateListContainerSessions(ctx);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_IS_FETCHING_DATA, false);

			} catch (NoConnectionException e) {
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_IS_FETCHING_DATA, false);
				throw e;
			} catch (SQLException e) {
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_IS_FETCHING_DATA, false);
				e.printStackTrace();
			} catch (NullSessionException e) {
				throw e;
			} catch (Exception e) {
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_IS_FETCHING_DATA, false);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Indicate that {@link #fetchData(Context)} is running or not
	 * 
	 * @param context
	 * @return {@code PreferencesUtil.PREF_IS_FETCHING_DATA} value
	 */
	public boolean isFetchingData(Context context) {
		return context.getSharedPreferences(PreferencesUtil.PREFS, 0)
				.getBoolean(PreferencesUtil.PREF_IS_FETCHING_DATA, false) == true;
	}

	/**
	 * 
	 * Indicate that {@link #updateListContainerSessions(Context)} is running or
	 * not
	 * 
	 * @param context
	 * @return
	 */
	public boolean isUpdating(Context context) {
		return context.getSharedPreferences(PreferencesUtil.PREFS, 0)
				.getBoolean(PreferencesUtil.PREF_IS_UPDATING_DATA, false) == true;
	}

	@Background
	public void editContainerSession(Context ctx,
			ContainerSession containerSession, String containerId,
			String operatorCode) {

		try {
			if (containerSession.getContainerId().equals(containerId)
					&& containerSession.getOperatorCode().equals(operatorCode)) {
				// do nothing

			} else {

				DatabaseHelper databaseHelper = getDatabaseManager().getHelper(
						ctx);

				OperatorDaoImpl operatorDaoImpl = databaseHelper
						.getOperatorDaoImpl();
				ContainerDaoImpl containerDaoImpl = databaseHelper
						.getContainerDaoImpl();
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseHelper
						.getContainerSessionDaoImpl();
				CJayImageDaoImpl cJayImageDaoImpl = databaseHelper
						.getCJayImageDaoImpl();

				// find operator
				Operator operator = operatorDaoImpl.findOperator(operatorCode);

				// update container details
				Container container = containerSession.getContainer();
				container.setContainerId(containerId);
				container.setOperator(operator);

				// update cjay image files name if they begin with file://
				List<CJayImage> cJayImages = (List<CJayImage>) containerSession
						.getCJayImages();

				String replaceString = containerId + ".jpg";
				for (CJayImage cJayImage : cJayImages) {

					String filePath = cJayImage.getUri();
					if (filePath.startsWith("file://")) {
						filePath.replace(".jpg", replaceString);
						cJayImageDaoImpl.update(cJayImage);
					}

				}

				// update database
				containerDaoImpl.update(container);
				containerSessionDaoImpl.update(containerSession);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
