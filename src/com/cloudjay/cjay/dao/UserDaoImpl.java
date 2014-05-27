package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class UserDaoImpl extends BaseDaoImpl<User, Integer> implements IUserDao {

	public UserDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, User.class);
	}

	@Override
	public void addListUsers(List<User> users) throws SQLException {
		for (User user : users) {
			createOrUpdate(user);
		}
	}

	@Override
	public void addUser(User user) throws SQLException {
		createOrUpdate(user);
	}

	@Override
	public void deleteAllUsers() throws SQLException {
		List<User> users = getAllUsers();
		for (User user : users) {
			this.delete(user);
		}
	}

	@Override
	public List<User> getAllUsers() throws SQLException {
		return queryForAll();
	}

	@Override
	public User getMainUser() throws SQLException {

		User user = null;
		List<User> users = query(queryBuilder().where().eq(User.IS_MAIN_ACCOUNT, true).prepare());

		if (!users.isEmpty()) {
			user = users.get(0);
		}

		return user;
	}
}
