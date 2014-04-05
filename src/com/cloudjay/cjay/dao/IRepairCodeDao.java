package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.RepairCode;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IRepairCodeDao extends Dao<RepairCode, Integer> {
	List<RepairCode> getAllRepairCodes() throws SQLException;

	void addListRepairCodes(List<RepairCode> repairCodes) throws SQLException;

	void addRepairCode(RepairCode repairCode) throws SQLException;

	void deleteAllRepairCodes() throws SQLException;

	boolean isEmpty() throws SQLException;

	RepairCode findByCode(String repairCode) throws SQLException;
}
