package com.cloudjay.cjay.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.UserDaoImpl;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;

/**
 * 
 * Nơi tập trung các hàm xử lý trả kết quả từ Server hoặc từ Local Database.
 * 
 * 1. Nếu server cập nhật thêm bảng CODE mới --> get data từ server merge vào db
 * rồi thực hiện truy vấn trả kết quả từ db
 * 
 * 2. Khi resume app thực hiện việc kiểm tra để lấy data mới nhất về
 * 
 * 3. Tất cả mọi hàm trả kết quả từ DataCenter phải luôn cho kết quả mới nhất
 * 
 * @author tieubao
 * 
 */
public class DataCenter {

	private static final String LOG_TAG = "DataCenter";

	private static DataCenter instance = null;
	private IDatabaseManager databaseManager = null;

	private TmpContainerSession tmpContainerSession = null;
	private ContainerSession currentSession = null;

	public DataCenter() {
	}

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

	public static void reload(Context context) {
		Logger.Log(LOG_TAG, "reload");
		CJayClient.getInstance().fetchData(context);
	}

	/**
	 * Save data to server
	 */
	public void setCurrentSession(ContainerSession session) {
		currentSession = session;
	}

	public ContainerSession getCurrentSession() {
		return currentSession;
	}

	public void setTmpCurrentSession(TmpContainerSession session) {
		tmpContainerSession = session;
	}

	public TmpContainerSession getTmpCurrentSession() {
		return tmpContainerSession;
	}

	/**
	 * Get data from server
	 */
	public static void fetchData(Context context) {
		Logger.Log(LOG_TAG, "fetching data ...");
		CJayClient.getInstance().fetchData(context);
	}

	public List<Operator> getListOperators(Context context) {
		Logger.Log(LOG_TAG, "get list Operators");

		try {
			return getDatabaseManager().getHelper(context).getOperatorDaoImpl().getAllOperators();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ComponentCode> getListComponents(Context context) {
		Logger.Log(LOG_TAG, "get list Components");

		try {
			return getDatabaseManager().getHelper(context).getComponentCodeDaoImpl().getAllComponentCodes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

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
	
	public List<ContainerSession> getListReportedContainerSessions(Context context) {
		Logger.Log(LOG_TAG, "get list reported Container sessions");
		try {
			return getDatabaseManager().getHelper(context).getContainerSessionDaoImpl().getListReportedContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ContainerSession> getListReportingContainerSessions(Context context) {
		Logger.Log(LOG_TAG, "get list reporting Container sessions");
		try {
			return getDatabaseManager().getHelper(context).getContainerSessionDaoImpl().getListReportingContainerSessions();
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

	public List<ContainerSession> getListLocalContainerSessions(Context context) {
		try {
			return getDatabaseManager().getHelper(context)
					.getContainerSessionDaoImpl().getLocalContainerSessions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
}
