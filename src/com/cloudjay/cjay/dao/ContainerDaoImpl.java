package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import android.database.Cursor;

import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class ContainerDaoImpl extends BaseDaoImpl<Container, Integer> implements
		IContainerDao {

	public ContainerDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, Container.class);
	}

	@Override
	public List<Container> getAllContainers() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListContainers(List<Container> containers)
			throws SQLException {
		for (Container container : containers) {
			this.createOrUpdate(container);
		}
	}

	@Override
	public void addContainer(Container container) throws SQLException {
		this.createOrUpdate(container);
	}

	public Cursor getAllContainersCursor() throws SQLException {
		Cursor cursor = null;

		CloseableIterator<Container> iterator = this.iterator(this
				.queryBuilder().prepare());

		try {
			// get the raw results which can be cast under Android
			AndroidDatabaseResults results = (AndroidDatabaseResults) iterator
					.getRawResults();
			cursor = results.getRawCursor();

		} finally {
			// iterator.closeQuietly();
		}

		return cursor;
	}

	@Override
	public void deleteAllContainers() throws SQLException {
		List<Container> containers = getAllContainers();
		for (Container container : containers) {
			this.delete(container);
		}
	}

}
