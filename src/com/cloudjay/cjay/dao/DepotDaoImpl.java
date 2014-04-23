package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Depot;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class DepotDaoImpl extends BaseDaoImpl<Depot, Integer> implements IDepotDao {

	public DepotDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Depot.class);
	}

	@Override
	public void addDepot(Depot depot) throws SQLException {
		createOrUpdate(depot);
	}

	@Override
	public void addListDepots(List<Depot> depots) throws SQLException {
		for (Depot depot : depots) {
			createOrUpdate(depot);
		}
	}

	@Override
	public void deleteAllDepots() throws SQLException {
		List<Depot> depots = getAllDepots();
		for (Depot depot : depots) {
			this.delete(depot);
		}
	}

	@Override
	public List<Depot> getAllDepots() throws SQLException {
		return queryForAll();
	}

	@Override
	public Depot findDepot(String depotCode) throws SQLException {
		return queryForFirst(this.queryBuilder().where().eq(Depot.DEPOT_CODE, depotCode).prepare());
	}
}
