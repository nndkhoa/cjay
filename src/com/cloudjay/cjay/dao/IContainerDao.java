package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IContainerDao extends Dao<Container, Integer> {
	List<Operator> getAllOperators() throws SQLException;

	void addListOperators(List<Operator> operators) throws SQLException;

	void addOperator(Operator operator) throws SQLException;

	void deleteAllOperators() throws SQLException;
}
