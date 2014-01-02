package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.RepairCode;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IComponentCodeDao extends Dao<ComponentCode, Integer> {

	List<ComponentCode> getAllComponentCodes() throws SQLException;

	void addListComponentCodes(List<ComponentCode> componentCodes)
			throws SQLException;

	void addComponentCode(ComponentCode componentCode) throws SQLException;

	void deleteAllComponentCodes() throws SQLException;

	boolean isEmpty() throws SQLException;

	ComponentCode findComponentCode(String componentCode) throws SQLException;
}
