package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Depot;
import com.j256.ormlite.dao.Dao;

public interface IDepotDao extends Dao<Depot, Integer> {
	List<Depot> getAllDepots() throws SQLException;

	void addListDepots(List<Depot> depots) throws SQLException;

	void addDepot(Depot depot) throws SQLException;

	void deleteAllDepots() throws SQLException;

}
