package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import android.database.Cursor;

import com.cloudjay.cjay.model.Container;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IContainerDao extends Dao<Container, Integer> {
	void addContainer(Container container) throws SQLException;

	void addListContainers(List<Container> containers) throws SQLException;

	void deleteAllContainers() throws SQLException;

	List<Container> getAllContainers() throws SQLException;

	Container findContainer(String containerId) throws SQLException;

	Cursor getAllContainersCursor() throws SQLException;
}
