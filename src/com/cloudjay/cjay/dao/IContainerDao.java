package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Container;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IContainerDao extends Dao<Container, Integer> {
	List<Container> getAllContainers() throws SQLException;

	void addListContainers(List<Container> containers) throws SQLException;

	void addContainer(Container container) throws SQLException;

	void deleteAllContainers() throws SQLException;
}
