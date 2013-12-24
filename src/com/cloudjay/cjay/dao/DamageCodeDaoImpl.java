package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.DamageCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class DamageCodeDaoImpl extends BaseDaoImpl<DamageCode, Integer>
		implements IDamageCodeDao {

	public DamageCodeDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, DamageCode.class);
	}

	@Override
	public List<DamageCode> getAllDamageCodes() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListDamageCodes(List<DamageCode> damageCodes)
			throws SQLException {
		for (DamageCode damageCode : damageCodes) {
			this.createOrUpdate(damageCode);
		}
	}

	@Override
	public void addDamageCode(DamageCode damageCode) throws SQLException {
		this.createOrUpdate(damageCode);
	}

	@Override
	public void deleteAllDamageCodes() throws SQLException {
		List<DamageCode> damageCodes = getAllDamageCodes();
		for (DamageCode damageCode : damageCodes) {
			this.delete(damageCode);
		}
	}

	@Override
	public boolean isEmpty() throws SQLException {
		DamageCode damageCode = this.queryForFirst(this.queryBuilder()
				.prepare());
		if (null == damageCode)
			return true;

		return false;
	}
}
