package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.DamageCode;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IDamageCodeDao extends Dao<DamageCode, Integer> {
	void addDamageCode(DamageCode damageCode) throws SQLException;

	void addListDamageCodes(List<DamageCode> damageCodes) throws SQLException;

	void deleteAllDamageCodes() throws SQLException;

	List<DamageCode> getAllDamageCodes() throws SQLException;

	boolean isEmpty() throws SQLException;
}
