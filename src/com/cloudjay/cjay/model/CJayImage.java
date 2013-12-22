package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cjay_image", daoClass = CJayImageDaoImpl.class)
public class CJayImage {

	private static final String ID = "id";
	private static final String URL = "url";
	private static final String LOCAL_URL = "local_url";
	private static final String TYPE = "type";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = URL)
	private String url;

	@DatabaseField(columnName = LOCAL_URL)
	private String local_url;

	/**
	 * TYPE include: in | out | issue | repaired
	 */
	@DatabaseField(columnName = TYPE)
	private String type;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private ContainerSession containerSession;

}
