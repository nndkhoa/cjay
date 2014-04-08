package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ComponentCode;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IComponentCodeDao extends Dao<ComponentCode, Integer> {

	void addComponentCode(ComponentCode componentCode) throws SQLException;

	void addListComponentCodes(List<ComponentCode> componentCodes) throws SQLException;

	void deleteAllComponentCodes() throws SQLException;

	ComponentCode findByCode(String componentCode) throws SQLException;

	List<ComponentCode> getAllComponentCodes() throws SQLException;

	boolean isEmpty() throws SQLException;
}
