package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.Issue;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class IssueDaoImpl extends BaseDaoImpl<Issue, String> implements IIssueDao {

	public IssueDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Issue.class);
	}

	public void bulkInsert(SQLiteDatabase db, List<AuditReportItem> auditReportItems, String containerSessionUuid) {

		try {
			db.beginTransaction();

			for (AuditReportItem auditReportItem : auditReportItems) {

				ContentValues values = new ContentValues();
				values.put("_id", UUID.randomUUID().toString());
				values.put("containerSession_id", containerSessionUuid);
				values.put("id", auditReportItem.getId());
				values.put("componentCode_id", auditReportItem.getComponentId());
				values.put("damageCode_id", auditReportItem.getDamageId());
				values.put("repairCode_id", auditReportItem.getRepairId());
				values.put("locationCode", auditReportItem.getLocationCode());
				values.put("quantity", auditReportItem.getQuantity());
				values.put("length", auditReportItem.getLength());
				values.put("height", auditReportItem.getHeight());
				values.put("is_fix_allowed", auditReportItem.isFixAllowed());
				db.insertWithOnConflict("issue", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
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

	@Override
	public Issue findByUuid(String uuid) throws SQLException {
		List<Issue> result = queryForEq(Issue.FIELD_UUID, uuid);
		if (result != null && result.size() > 0) return result.get(0);

		return null;
	}
}
