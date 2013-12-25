package com.cloudjay.cjay.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
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
	 * Get data from server
	 */
	public static void fetchData(Context context) {
		Logger.Log(LOG_TAG, "fetching data ...");
		CJayClient.getInstance().fetchData(context);
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
	
	public List<String> getListOperatorNames(Context context) {
		List<Operator> operators = this.getListOperators(context);
		List<String> operatorNames = new ArrayList<String>();
		for (Operator operator : operators) {
			operatorNames.add(operator.getName());
		}
		return operatorNames;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
}
