package com.cloudjay.cjay.util;

import java.util.List;

import com.cloudjay.cjay.model.Operator;

/**
 * 
 * Nơi tập trung các hàm xử lý trả kết quả từ Server hoặc từ Local Database
 * 
 * @author tieubao
 * 
 */
public class DataCenter {

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

	public List<Operator> getListOperators() {
		return null;
	}

}
