package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.DamageCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class DamageCodeDaoImpl extends BaseDaoImpl<DamageCode, Integer> implements IDamageCodeDao {

	public DamageCodeDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, DamageCode.class);
	}

	@Override
	public void addDamageCode(DamageCode damageCode) throws SQLException {
		createOrUpdate(damageCode);
	}

	@Override
	public void addListDamageCodes(List<DamageCode> damageCodes) throws SQLException {
		for (DamageCode damageCode : damageCodes) {
			createOrUpdate(damageCode);
		}
	}

	public void bulkInsert(SQLiteDatabase db, List<DamageCode> damageCodes) {

		try {
			db.beginTransaction();

			for (DamageCode damageCode : damageCodes) {

				ContentValues values = new ContentValues();
				values.put(DamageCode.ID, damageCode.getId());
				values.put(DamageCode.CODE, damageCode.getCode());
				values.put(DamageCode.DISPLAY_NAME, damageCode.getName());
				db.insert("damage_code", null, values);

			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void deleteAllDamageCodes() throws SQLException {
		List<DamageCode> damageCodes = getAllDamageCodes();
		for (DamageCode damageCode : damageCodes) {
			this.delete(damageCode);
		}
	}

	public DamageCode findByCode(String damageCode) throws SQLException {
		List<DamageCode> listDamageCodes = queryForEq(DamageCode.CODE, damageCode);

		if (listDamageCodes.isEmpty())
			return null;
		else
			return listDamageCodes.get(0);
	}

	@Override
	public List<DamageCode> getAllDamageCodes() throws SQLException {
		return queryForAll();
	}

	@Override
	public boolean isEmpty() throws SQLException {
		DamageCode damageCode = queryForFirst(queryBuilder().prepare());
		if (null == damageCode) return true;

		return false;
	}
}
