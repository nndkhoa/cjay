package com.cloudjay.cjay.model;

import java.util.Date;

import com.cloudjay.cjay.util.StringHelper;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * { "container_id": "abcdef12313", "image_id_path": "abcdef12313.jpg",
 * "operator_code": "BS", "check_in_time": "2013-12-20T20:14:47",
 * "check_out_time": null, "gate_report_images": [ { "type": 0, "image_name":
 * "asd" }, { "type": 0, "image_name": "asd21" }, { "type": 1, "image_name":
 * "1233313" } ] }
 * 
 * @author tieubao
 * 
 */

@DatabaseTable(tableName = "container_session")
public class ContainerSession {

	private static final String CHECK_OUT_TIME = "check_out_time";
	private static final String CHECK_IN_TIME = "check_in_time";
	private static final String IMAGE_ID_PATH = "image_id_path";
	private static final String ID = "id";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = IMAGE_ID_PATH)
	private String image_id_path;

	@DatabaseField(columnName = CHECK_IN_TIME)
	private Date check_in_time;

	@DatabaseField(columnName = CHECK_OUT_TIME)
	private Date check_out_time;

	// container_id
	// operator_code
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	private Container container;

	// gate_report_images where type = 0 (check in) | 1 (check out)
	@ForeignCollectionField(eager = true)
	private ForeignCollection<CJayImage> cJayImages;

	//
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Issue> issues;

	public String getOperatorName() {
		return container.getOperator().getName();
	}

	public String getContainerId() {
		return container.getContainerId();
	}

	public String getCheckInTime() {
		return StringHelper.getRelativeDate(check_in_time.toString());
	}

	public String getCheckOutTime() {
		return StringHelper.getRelativeDate(check_out_time.toString());
	}

	public String getImage() {
		return image_id_path;
	}
}
