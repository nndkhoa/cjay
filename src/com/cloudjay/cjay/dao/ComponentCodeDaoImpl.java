package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.DamageCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class ComponentCodeDaoImpl extends BaseDaoImpl<ComponentCode, Integer>
		implements IComponentCodeDao {

	public ComponentCodeDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, ComponentCode.class);
	}

	public void bulkInsert(SQLiteDatabase db, List<ComponentCode> componentCodes) {

		try {
			db.beginTransaction();

			for (ComponentCode componentCode : componentCodes) {
				ContentValues values = new ContentValues();
				values.put(ComponentCode.CODE, componentCode.getCode());
				values.put(ComponentCode.DISPLAY_NAME, componentCode.getName());
				db.insert("component_code", null, values);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
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
	public boolean isEmpty() throws SQLException {
		ComponentCode componentCode = this.queryForFirst(this.queryBuilder()
				.prepare());

		if (null == componentCode)
			return true;

		return false;
	}

	@Override
	public ComponentCode findByCode(String componentCode) throws SQLException {
		List<ComponentCode> listComponentCodes = queryForEq(ComponentCode.CODE,
				componentCode);

		if (listComponentCodes.isEmpty()) {
			return null;
		} else {
			return listComponentCodes.get(0);
		}
	}
}
