package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IDamageCodeDao extends Dao<DamageCode, Integer> {
	List<DamageCode> getAllDamageCodes() throws SQLException;

	void addListDamageCodes(List<DamageCode> damageCodes) throws SQLException;

	void addDamageCode(DamageCode damageCode) throws SQLException;

	void deleteAllDamageCodes() throws SQLException;
}
