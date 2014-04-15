package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.Operator;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class OperatorDaoImpl extends BaseDaoImpl<Operator, Integer> implements IOperatorDao {

	public OperatorDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Operator.class);
	}

	@Override
	public void addListOperators(List<Operator> operators) throws SQLException {
		for (Operator operator : operators) {
			createOrUpdate(operator);
		}
	}

	@Override
	public void addOperator(Operator operator) throws SQLException {
		createOrUpdate(operator);
	}

	public void bulkInsert(SQLiteDatabase db, List<Operator> operators) {

		try {
			db.beginTransaction();

			for (Operator operator : operators) {

				ContentValues values = new ContentValues();
				values.put(Operator.FIELD_ID, operator.getId());
				values.put(Operator.FIELD_CODE, operator.getCode());
				values.put(Operator.FIELD_NAME, operator.getName());
				db.insert("operator", null, values);

			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void deleteAllOperators() throws SQLException {
		List<Operator> operators = getAllOperators();
		for (Operator operator : operators) {
			this.delete(operator);
		}
	}

	public Operator findOperator(String operatorCode) throws SQLException {
		List<Operator> listOperators = queryForEq(Operator.FIELD_CODE, operatorCode);

		if (listOperators.isEmpty())
			return null;
		else
			return listOperators.get(0);
	}

	@Override
	public List<Operator> getAllOperators() throws SQLException {
		List<Operator> operators = queryForAll();

		Collections.sort(operators, new Comparator<Operator>() {
			@Override
			public int compare(Operator lhs, Operator rhs) {
				return lhs.getCode().compareToIgnoreCase(rhs.getCode());
			}
		});

		return operators;
	}

	@Override
	public boolean isEmpty() throws SQLException {
		Operator operator = queryForFirst(queryBuilder().prepare());
		if (null == operator) return true;

		return false;
	}
}
