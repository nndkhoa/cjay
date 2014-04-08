package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Issue;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class IssueDaoImpl extends BaseDaoImpl<Issue, String> implements IIssueDao {

	public IssueDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Issue.class);
	}

	@Override
	public void addIssue(Issue issue) throws SQLException {
		createOrUpdate(issue);
	}

	@Override
	public void addListIssues(List<Issue> issues) throws SQLException {
		for (Issue issue : issues) {
			createOrUpdate(issue);
		}
	}

	@Override
	public void deleteAllIssues() throws SQLException {
		List<Issue> issues = getAllIssues();
		for (Issue issue : issues) {
			this.delete(issue);
		}
	}

	@Override
	public List<Issue> getAllIssues() throws SQLException {
		return queryForAll();
	}

}
