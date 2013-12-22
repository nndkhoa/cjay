package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Issue;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class IssueDaoImpl extends BaseDaoImpl<Issue, Integer> implements
		IIssueDao {

	protected IssueDaoImpl(ConnectionSource connectionSource,
			Class<Issue> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	@Override
	public List<Issue> getAllIssues() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListIssues(List<Issue> issues) throws SQLException {
		for (Issue issue : issues) {
			this.createOrUpdate(issue);
		}
	}

	@Override
	public void addIssue(Issue issue) throws SQLException {
		this.createOrUpdate(issue);
	}

	@Override
	public void deleteAllIssues() throws SQLException {
		List<Issue> issues = getAllIssues();
		for (Issue issue : issues) {
			this.delete(issue);
		}
	}

}
