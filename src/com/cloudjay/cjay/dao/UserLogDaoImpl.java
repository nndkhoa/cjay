package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.UserLog;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class UserLogDaoImpl extends BaseDaoImpl<UserLog, Integer> implements IUserLogDao {

	public UserLogDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, UserLog.class);
	}

	@Override
	public void addListLogs(List<UserLog> userLogs) throws SQLException {
		for (UserLog userLog : userLogs) {
			createOrUpdate(userLog);
		}

	}

	@Override
	public void addLog(UserLog userLog) throws SQLException {
		createOrUpdate(userLog);
	}

	@Override
	public void deleteAllLogs() throws SQLException {
		List<UserLog> userLogs = getAllLogs();
		for (UserLog userLog : userLogs) {
			this.delete(userLog);
		}

	}

	@Override
	public List<UserLog> getAllLogs() throws SQLException {
		return queryForAll();
	}

	@Override
	public boolean isEmpty() throws SQLException {
		UserLog userLog = queryForFirst(queryBuilder().prepare());
		if (null == userLog) return true;

		return false;
	}
}