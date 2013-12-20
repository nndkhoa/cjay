package com.cloudjay.cjay.model;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;

/**
 * Created by Huy Do on 15/10/13.
 */

public interface IUserDao extends Dao<User, Integer> {
	List<User> getAllUsers() throws SQLException;

	void addListUsers(List<User> users) throws SQLException;

	void addUser(User user) throws SQLException;

	void deleteAllUsers() throws SQLException;

	User getMainUser() throws SQLException;
}
