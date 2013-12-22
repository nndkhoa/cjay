package com.cloudjay.cjay.util;

import java.util.List;

import android.content.Context;

import com.cloudjay.cjay.model.ContainerSession;
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

	public static DataCenter instance = null;

	public DataCenter() {
		// TODO Auto-generated constructor stub
	}

	public DataCenter getInstance() {
		if (null == instance) {
			instance = new DataCenter();
		}
		return instance;
	}

	public static void initialize() {

	}

	public static void reload() {

	}

	/**
	 * Get data from server
	 */
	public static void fetchData() {

		// 1. fetch `new ISO code` from the `last time`
		Logger.Log(LOG_TAG, "fetching data ...");

		if (CJayClient.getInstance().checkIfServerHasNewMetadata()) {

		}


		// 2.

	}

	public static List<Operator> getListOperators(Context context) {
		return CJayClient.getInstance().getOperators(context);
	}

	public static List<ContainerSession> getListContainerSessions(
			Context context) {
		return CJayClient.getInstance().getContainerSessions(context);
	}
}
