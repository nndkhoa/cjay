package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class CJayImageDaoImpl extends BaseDaoImpl<CJayImage, String> implements ICJayImageDao {

	public CJayImageDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, CJayImage.class);
	}

	@Override
	public void addCJayImage(CJayImage cJayImage) throws SQLException {
		createOrUpdate(cJayImage);
	}

	@Override
	public void addListCJayImages(List<CJayImage> cJayImages) throws SQLException {
		for (CJayImage cJayImage : cJayImages) {
			createOrUpdate(cJayImage);
		}
	}

	@Override
	public void deleteAllCJayImages() throws SQLException {
		List<CJayImage> cJayImages = getAllCJayImages();
		for (CJayImage cJayImage : cJayImages) {
			this.delete(cJayImage);
		}
	}

	@Override
	public CJayImage findByUuid(String uuid) throws SQLException {
		List<CJayImage> result = queryForEq("uuid", uuid);
		if (result != null && result.size() > 0) return result.get(0);

		return null;
	}

	@Override
	public List<CJayImage> getAllCJayImages() throws SQLException {
		return queryForAll();
	}

	@Override
	public CJayImage getNextWaiting() throws SQLException {

		List<CJayImage> result = queryForEq("state", CJayImage.STATE_UPLOAD_WAITING);

		if (result != null && result.size() > 0) {

			Logger.e("Total item in ImageQueue: " + result.size());
			for (CJayImage cJayImage : result) {
				Logger.Log(cJayImage.getImageName());
			}

			return result.get(0);

		}

		// Logger.Log("No images to upload");
		return null;
	}
}
