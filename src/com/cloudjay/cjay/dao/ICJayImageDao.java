package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.CJayImage;
import com.j256.ormlite.dao.Dao;

public interface ICJayImageDao extends Dao<CJayImage, String> {
	List<CJayImage> getAllCJayImages() throws SQLException;

	void addListCJayImages(List<CJayImage> cJayImages) throws SQLException;

	void addCJayImage(CJayImage cJayImage) throws SQLException;

	void deleteAllCJayImages() throws SQLException;

	CJayImage findByUuid(String uuid) throws SQLException;

	CJayImage getNextWaiting() throws SQLException;
}
