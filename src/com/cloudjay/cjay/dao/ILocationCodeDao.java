package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.LocationCode;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface ILocationCodeDao extends Dao<LocationCode, Integer> {
	List<LocationCode> getAllLocationCodes() throws SQLException;

	void addListLocationCodes(List<LocationCode> locationCodes)
			throws SQLException;

	void addLocationCode(LocationCode locationCode) throws SQLException;

	void deleteAllLocationCodes() throws SQLException;
}
