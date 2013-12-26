package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;

public interface IUploadItemDao extends Dao<UploadItem, Integer> {
	void addItem(UploadItem item) throws SQLException;

	List<UploadItem> getAllItems() throws SQLException;
}
