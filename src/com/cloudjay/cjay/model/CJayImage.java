package com.cloudjay.cjay.model;

import java.util.Date;

import android.R.integer;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cjay_image", daoClass = CJayImageDaoImpl.class)
public class CJayImage {

	private static final String ID = "id";
	private static final String IMAGE_NAME = "image_name";
	private static final String LOCAL_URL = "local_url";
	private static final String TYPE = "type";
	private static final String TIME_POSTED = "time_posted";

	public CJayImage(int id, int type, Date date, String image_name) {
		this.id = id;
		this.setType(type);
		this.image_name = image_name;
		this.time_posted = date;
	}

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = IMAGE_NAME)
	private String image_name;

	@DatabaseField(columnName = LOCAL_URL)
	private String local_url;

	@DatabaseField(columnName = TIME_POSTED)
	private Date time_posted;

	/**
	 * TYPE include: in | out | issue | repaired
	 */
	@DatabaseField(columnName = TYPE)
	private int type;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private ContainerSession containerSession;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private Issue issue;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Date getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(Date time_posted) {
		this.time_posted = time_posted;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

}
