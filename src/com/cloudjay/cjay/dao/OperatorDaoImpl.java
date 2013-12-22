package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Operator;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class OperatorDaoImpl extends BaseDaoImpl<Operator, Integer> implements
		IOperatorDao {

	protected OperatorDaoImpl(ConnectionSource connectionSource,
			Class<Operator> dataClass) throws SQLException {
		super(connectionSource, dataClass);
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
		if (null == operator)
			return true;

		return false;
	}
}
