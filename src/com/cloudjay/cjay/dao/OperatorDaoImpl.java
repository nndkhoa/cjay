package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Operator;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class OperatorDaoImpl extends BaseDaoImpl<Operator, Integer> implements
		IOperatorDao {

	public OperatorDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, Operator.class);
	}

	@Override
	public List<Operator> getAllOperators() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListOperators(List<Operator> operators) throws SQLException {
		for (Operator operator : operators) {
			this.createOrUpdate(operator);
		}
	}

	@Override
	public void addOperator(Operator operator) throws SQLException {
		this.createOrUpdate(operator);
	}

	@Override
	public void deleteAllOperators() throws SQLException {
		List<Operator> operators = getAllOperators();
		for (Operator operator : operators) {
			this.delete(operator);
		}
	}

	@Override
	public boolean isEmpty() throws SQLException {
		Operator operator = this.queryForFirst(null);
		// this.queryBuilder().prepare()
		if (null == operator)
			return true;

		return false;
	}
}
