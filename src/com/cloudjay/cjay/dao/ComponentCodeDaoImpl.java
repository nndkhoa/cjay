package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.RepairCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class ComponentCodeDaoImpl extends BaseDaoImpl<ComponentCode, Integer>
		implements IComponentCodeDao {

	public ComponentCodeDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, ComponentCode.class);
	}

	@Override
	public List<ComponentCode> getAllComponentCodes() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListComponentCodes(List<ComponentCode> componentCodes)
			throws SQLException {
		if (null != componentCodes) {
			for (ComponentCode componentCode : componentCodes) {
				this.createOrUpdate(componentCode);
			}
		}

	}

	@Override
	public void addComponentCode(ComponentCode componentCode)
			throws SQLException {
		if (null != componentCode) {
			this.createOrUpdate(componentCode);
		}
	}

	@Override
	public void deleteAllComponentCodes() throws SQLException {
		List<ComponentCode> componentCodes = getAllComponentCodes();
		if (null != componentCodes) {
			for (ComponentCode componentCode : componentCodes) {
				this.delete(componentCode);
			}
		}
	}

	@Override
	public ComponentCode findComponentCode(String componentCode)
			throws SQLException {
		List<ComponentCode> listComponentCodes = queryForEq(ComponentCode.CODE,
				componentCode);

		if (listComponentCodes.isEmpty()) {
			return null;
		} else {
			return listComponentCodes.get(0);
		}
	}
}
