package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class UploadItemDaoImpl extends BaseDaoImpl<UploadItem, Integer>
		implements IUploadItemDao {
	public UploadItemDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, UploadItem.class);
	}

	public void addItem(UploadItem item) throws SQLException {
		this.create(item);
	}

	public void uploadItem(UploadItem item) throws SQLException {
		this.update(item);
	}

	public List<UploadItem> getAllItems() throws SQLException {
		return this.queryForAll();
	}

	public UploadItem findByUUID(String uuID) throws SQLException {
		List<UploadItem> result = this.queryForEq("uuid", uuID);
		if (result != null) {
			return result.get(0);
		}

		return null;
	}

	public UploadItem findById(int id) throws SQLException {
		List<UploadItem> result = this.queryForEq("id", id);
		if (result != null) {
			return result.get(0);
		}

		return null;
	}
}
