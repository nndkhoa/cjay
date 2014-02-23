package com.cloudjay.cjay.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.dao.UserDaoImpl;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;

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
public class DataCenter {

	private static final String LOG_TAG = "DataCenter";
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

	public void initialize(IDatabaseManager databaseManager) {
		Logger.Log(LOG_TAG, "initialize");
		this.databaseManager = databaseManager;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public List<Operator> getListOperators(Context context) {
		Logger.Log(LOG_TAG, "get list Operators");

		try {
			return getDatabaseManager().getHelper(context).getOperatorDaoImpl()
					.getAllOperators();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ComponentCode> getListComponents(Context context) {
		Logger.Log(LOG_TAG, "get list Components");

		try {
			return getDatabaseManager().getHelper(context)
					.getComponentCodeDaoImpl().getAllComponentCodes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ComponentCode> getListComponents(Context context, String date) {
		Logger.Log(LOG_TAG, "get list Components from: " + date);

		try {
			return CJayClient.getInstance().getComponentCodes(context, date);
		} catch (NoConnectionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<DamageCode> getListDamageCodes(Context context) {
		Logger.Log(LOG_TAG, "get list Damage Codes");

		try {
			return getDatabaseManager().getHelper(context)
					.getDamageCodeDaoImpl().getAllDamageCodes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get list of damage codes from last {@link date}
	 * 
	 * @param context
	 * @param date
	 * @return
	 */
	public List<DamageCode> getListDamageCodes(Context context, String date) {
		Logger.Log(LOG_TAG, "get list Damage Codes from: " + date);

		try {
			return CJayClient.getInstance().getDamageCodes(context, date);
		} catch (NoConnectionException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get List of repair codes
	 * 
	 * @param context
	 * @return
	 */
	public List<RepairCode> getListRepairCodes(Context context) {
		Logger.Log(LOG_TAG, "get list Repair Codes");

		try {
			return getDatabaseManager().getHelper(context)
					.getRepairCodeDaoImpl().getAllRepairCodes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get List of repair codes from last `date`
	 * 
	 * @param context
	 * @param date
	 * @return
	 */
	public List<RepairCode> getListRepairCodes(Context context, String date) {
		Logger.Log(LOG_TAG, "get list Repair Codes from: " + date);

		try {
			return CJayClient.getInstance().getRepairCodes(context, date);
		} catch (NoConnectionException e) {
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

			Logger.Log(LOG_TAG, "User role: " + currentUser.getRoleName());

			DepotDaoImpl depotDaoImpl;

			depotDaoImpl = getDatabaseManager().getHelper(context)
					.getDepotDaoImpl();

			UserDaoImpl userDaoImpl = getDatabaseManager().getHelper(context)
					.getUserDaoImpl();

			List<Depot> depots = depotDaoImpl.queryForEq(Depot.DEPOT_CODE,
					currentUser.getDepotCode());

			if (null != depots && !depots.isEmpty()) {
				currentUser.setDepot(depots.get(0));
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

	public List<ContainerSession> getListContainerSessions(Context context) {
		Logger.Log(LOG_TAG, "get list Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl().getAllContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListCheckOutContainerSessions(
			Context context) {
		Logger.Log(LOG_TAG, "get list check out Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListCheckOutContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Cursor getCheckOutContainerSessionCursor(Context context) {

		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getCheckOutContainerSessionCursor();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Cursor filterCheckoutCursor(Context context, CharSequence constraint) {

		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.filterCheckOutCursor(constraint);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListReportedContainerSessions(
			Context context) {
		Logger.Log(LOG_TAG, "get list reported Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListReportedContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListReportingContainerSessions(
			Context context) {
		Logger.Log(LOG_TAG, "get list reporting Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListReportingContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListNotReportedContainerSessions(
			Context context) {
		Logger.Log(LOG_TAG, "get list not reported Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListNotReportedContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListPendingContainerSessions(
			Context context) {
		Logger.Log(LOG_TAG, "get list pending Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListPendingContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ContainerSession> getListFixedContainerSessions(Context context) {
		Logger.Log(LOG_TAG, "get list fixed Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl()
					.getListFixedContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getListOperatorNames(Context context) {
		List<Operator> operators = this.getListOperators(context);
		List<String> operatorNames = new ArrayList<String>();
		for (Operator operator : operators) {
			if (operator.getName().length() > 0) {
				operatorNames.add(operator.getName());
			}
		}
		return operatorNames;
	}

	public List<ContainerSession> getListUploadContainerSessions(Context context) {
		try {

			List<ContainerSession> result = getDatabaseManager()
					.getHelper(context).getContainerSessionDaoImpl()
					.getListUploadContainerSessions();

			if (result != null) {
				Logger.Log(
						LOG_TAG,
						"Upload list number of items: "
								+ Integer.toString(result.size()));
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return List<ContainerSession> that is created by current user and stored
	 * in local database.
	 * 
	 * @param context
	 * @return List of Container Sessions that created by current user
	 * @see ContainerSession
	 * @since 1.0
	 */
	public List<ContainerSession> getListLocalContainerSessions(Context context) {

		Logger.Log(LOG_TAG, "get list local Container sessions");
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl().getLocalContainerSessions();
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
		Logger.Log(LOG_TAG,
				"remove Container Session with Id = " + Integer.toString(id));
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
	 */
	public void updateListContainerSessions(Context ctx)
			throws NoConnectionException, SQLException {

		Logger.Log(LOG_TAG, "***\nUPDATE LIST CONTAINER SESSIONS\n***");

		try {
			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			// 3. Update list ContainerSessions
			Logger.Log(LOG_TAG, "get list container sessions");
			List<ContainerSession> containerSessions = null;

			if (containerSessionDaoImpl.isEmpty()) {

				Logger.Log(LOG_TAG,
						"get new list container sessions based on user role");

				containerSessions = CJayClient.getInstance()
						.getAllContainerSessions(ctx);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
						nowString);

			} else {

				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list container sessions from last time: "
								+ date);

				containerSessions = CJayClient.getInstance()
						.getContainerSessions(ctx, date);

				// TODO: need to refactor after implement push notification
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
						nowString);

				if (containerSessions == null) {
					Logger.Log(LOG_TAG, "----> NO new container sessions");
				} else {
					Logger.Log(
							LOG_TAG,
							"----> Has "
									+ Integer.toString(containerSessions.size())
									+ " new container sessions");
				}

				Logger.Log(
						LOG_TAG,
						"Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE));
			}

			// NOTE: already added inside
			// if (null != containerSessions) {
			// containerSessionDaoImpl
			// .addListContainerSessions(containerSessions);
			// }

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get new operators from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 */
	public void updateListOperators(Context ctx) throws NoConnectionException,
			SQLException {

		Logger.Log(LOG_TAG, "***\nUPDATE LIST OPERATORS\n***");
		try {
			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();

			// get list operator
			List<Operator> operators = null;
			if (operatorDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "no Operator");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE,
						nowString);

				operators = CJayClient.getInstance().getOperators(ctx);

			} else {

				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list operator from last time: " + date);

				operators = CJayClient.getInstance().getOperators(ctx, date);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE,
						nowString);

				if (operators == null) {
					Logger.Log(LOG_TAG, "----> NO new operators");
				} else {
					Logger.Log(LOG_TAG,
							"----> Has " + Integer.toString(operators.size())
									+ " new operators");
				}

				Logger.Log(
						LOG_TAG,
						"Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_RESOURCE_OPERATOR_LAST_UPDATE));
			}
			if (null != operators)
				operatorDaoImpl.addListOperators(operators);

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		Logger.Log(LOG_TAG, "***\nUPDATE LIST DAMAGE\n***");
		try {
			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();

			// Get list damage
			List<DamageCode> damageCodes = null;
			if (damageCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "no Damage Code");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE,
						nowString);
				damageCodes = CJayClient.getInstance().getDamageCodes(ctx);

			} else {
				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list damage codes from last time: " + date);

				damageCodes = CJayClient.getInstance()
						.getDamageCodes(ctx, date);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE,
						nowString);

				if (damageCodes == null) {
					Logger.Log(LOG_TAG, "----> NO new damage codes");
				} else {
					Logger.Log(LOG_TAG,
							"----> Has " + Integer.toString(damageCodes.size())
									+ " new damage codes");
				}

				Logger.Log(
						LOG_TAG,
						"Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_RESOURCE_DAMAGE_LAST_UPDATE));
			}

			if (null != damageCodes)
				damageCodeDaoImpl.addListDamageCodes(damageCodes);

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get new component codes from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 */
	public void updateListComponentCodes(Context ctx)
			throws NoConnectionException, SQLException {

		Logger.Log(LOG_TAG, "***\nUPDATE LIST COMPONENT\n***");

		try {
			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			ComponentCodeDaoImpl componentCodeDaoImpl = databaseManager
					.getHelper(ctx).getComponentCodeDaoImpl();

			// Get list Component
			List<ComponentCode> componentCodes = null;
			if (componentCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "no Component Code");

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE,
						nowString);

				componentCodes = CJayClient.getInstance()
						.getComponentCodes(ctx);

			} else {

				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list component codes from last time: "
								+ date);

				componentCodes = CJayClient.getInstance().getComponentCodes(
						ctx, date);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE,
						nowString);

				if (componentCodes == null) {
					Logger.Log(LOG_TAG, "----> NO new component codes");
				} else {
					Logger.Log(
							LOG_TAG,
							"----> Has "
									+ Integer.toString(componentCodes.size())
									+ " new component codes");
				}

				Logger.Log(
						LOG_TAG,
						"Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_RESOURCE_COMPONENT_LAST_UPDATE));
			}

			if (null != componentCodes)
				componentCodeDaoImpl.addListComponentCodes(componentCodes);

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		Logger.Log(LOG_TAG, "***\nUPDATE LIST REPAIR\n***");
		try {
			Date now = new Date();

			// 2013-11-10T21:05:24 (do not have timezone info)
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT);
			String nowString = dateFormat.format(now);

			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();

			// Get list Repair
			List<RepairCode> repairCodes = null;
			if (repairCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "no Repair Code");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE,
						nowString);

				repairCodes = CJayClient.getInstance().getRepairCodes(ctx);

			} else {

				String date = PreferencesUtil.getPrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE);

				Logger.Log(LOG_TAG,
						"get updated list repair codes from last time: " + date);

				repairCodes = CJayClient.getInstance()
						.getRepairCodes(ctx, date);

				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE,
						nowString);

				if (repairCodes == null) {
					Logger.Log(LOG_TAG, "----> NO new repair codes");
				} else {
					Logger.Log(LOG_TAG,
							"----> Has " + Integer.toString(repairCodes.size())
									+ " new repair codes");
				}

				Logger.Log(
						LOG_TAG,
						"Last update from "
								+ PreferencesUtil
										.getPrefsValue(
												ctx,
												PreferencesUtil.PREF_RESOURCE_REPAIR_LAST_UPDATE));
			}
			if (null != repairCodes)
				repairCodeDaoImpl.addListRepairCodes(repairCodes);

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get new ISO Code from server
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 * @throws SQLException
	 */
	public void updateListISOCode(Context ctx) throws NoConnectionException,
			SQLException {

		Logger.Log(LOG_TAG, "***\nUPDATE ALL ISO CODE\n***");
		try {

			updateListOperators(ctx);
			updateListDamageCodes(ctx);
			updateListRepairCodes(ctx);
			updateListComponentCodes(ctx);

		} catch (NoConnectionException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetch data from server based on current user role.
	 * 
	 * @param ctx
	 * @throws NoConnectionException
	 *             if there is no connection to internet
	 */
	public void fetchData(Context ctx) throws NoConnectionException {

		Logger.Log(LOG_TAG, "***\nFETCHING DATA ...\n***");

		if (isFetchingData(ctx)) {
			Logger.Log(LOG_TAG, "fetchData() is already running");
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
}
