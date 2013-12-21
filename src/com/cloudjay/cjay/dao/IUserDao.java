package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.User;
import com.j256.ormlite.dao.Dao;

/**
 * 
 * @author tieubao
 * 
 */
public interface IUserDao extends Dao<User, Integer> {
	List<User> getAllUsers() throws SQLException;

	void addListUsers(List<User> users) throws SQLException;

	void addUser(User user) throws SQLException;

	void deleteAllUsers() throws SQLException;

	User getMainUser() throws SQLException;
}
