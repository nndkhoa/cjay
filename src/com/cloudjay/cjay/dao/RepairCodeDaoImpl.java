package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.RepairCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class RepairCodeDaoImpl extends BaseDaoImpl<RepairCode, Integer>
		implements IRepairCodeDao {

	public RepairCodeDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, RepairCode.class);
	}

	@Override
	public List<RepairCode> getAllRepairCodes() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListRepairCodes(List<RepairCode> repairCodes)
			throws SQLException {
		for (RepairCode repairCode : repairCodes) {
			this.createOrUpdate(repairCode);
		}
	}

	@Override
	public void addRepairCode(RepairCode repairCode) throws SQLException {
		this.create(repairCode);
	}

	@Override
	public void deleteAllRepairCodes() throws SQLException {
		List<RepairCode> repairCodes = getAllRepairCodes();
		for (RepairCode repairCode : repairCodes) {
			this.delete(repairCode);
		}
	}

	@Override
	public boolean isEmpty() throws SQLException {
		RepairCode repairCode = this.queryForFirst(null);
		if (null == repairCode)
			return true;

		return false;
	}

}