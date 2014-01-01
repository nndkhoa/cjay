package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Issue;
import com.j256.ormlite.dao.Dao;

public interface IIssueDao extends Dao<Issue, String> {
	List<Issue> getAllIssues() throws SQLException;

	void addListIssues(List<Issue> issues) throws SQLException;

	void addIssue(Issue issue) throws SQLException;

	void deleteAllIssues() throws SQLException;

}
