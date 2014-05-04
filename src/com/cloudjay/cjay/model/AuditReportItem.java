package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cloudjay.cjay.util.Logger;

public class AuditReportItem {

	private int id;
	private int damage_id;
	private int repair_id;
	private int component_id;
	private String location_code;
	private String length;
	private String height;
	private String quantity;
	private String time_posted;
	private boolean is_fix_allowed;

	private List<AuditReportImage> audit_report_images;

	public AuditReportItem() {

	}

	public AuditReportItem(Issue issue) {

		if (null != issue) {
			setId(issue.getId());
			setDamageId(issue.getDamageCode().getId());
			setRepairId(issue.getRepairCode().getId());
			setComponentId(issue.getComponentCode().getId());
			setLength(issue.getLength());
			setHeight(issue.getHeight());
			setQuantity(issue.getQuantity());
			setLocationCode(issue.getLocationCode());

			ArrayList<AuditReportImage> auditReportImages = new ArrayList<AuditReportImage>();
			Collection<CJayImage> cJayImages = issue.getCJayImages();
			if (null != cJayImages) {
				for (CJayImage cJayImage : cJayImages) {

					if (cJayImage.getType() == CJayImage.TYPE_REPORT || cJayImage.getType() == CJayImage.TYPE_REPAIRED) {
						AuditReportImage image = new AuditReportImage(cJayImage.getId(), cJayImage.getType(),
																		cJayImage.getTimePosted(),
																		cJayImage.getImageName(), cJayImage.getUri());
						auditReportImages.add(image);
					}
				}

				if (auditReportImages.size() > 0) {
					setAuditReportImages(auditReportImages);
				} else {
					Logger.e("audit_report_images is NULL.");
				}

			} else {
				Logger.e("Issue do not have CJayImages. audit_report_images will be NULL.");
			}

		} else {
			Logger.e("Issue is NULL");
		}

	}

	public List<AuditReportImage> getAuditReportImages() {
		return audit_report_images;
	}

	public int getComponentId() {
		return component_id;
	}

	public int getDamageId() {
		return damage_id;
	}

	public String getHeight() {
		return height;
	}

	public int getId() {
		return id;
	}

	public String getLength() {
		return length;
	}

	public String getLocationCode() {
		return location_code;
	}

	public String getQuantity() {
		return quantity;
	}

	public int getRepairId() {
		return repair_id;
	}

	public void setAuditReportImages(List<AuditReportImage> audit_report_images) {
		this.audit_report_images = audit_report_images;
	}

	public void setComponentId(int component_id) {
		this.component_id = component_id;
	}

	public void setDamageId(int damage_id) {
		this.damage_id = damage_id;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public void setLocationCode(String location_code) {
		this.location_code = location_code;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public void setRepairId(int repair_id) {
		this.repair_id = repair_id;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public boolean isFixAllowed() {
		return is_fix_allowed;
	}

	public void setFixAllowed(boolean is_fix_allowed) {
		this.is_fix_allowed = is_fix_allowed;
	}

}
