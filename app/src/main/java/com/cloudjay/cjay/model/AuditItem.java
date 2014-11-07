package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;


/* Field uuid để phân biệt AuditItem dưới Local, trước khi upload lên server */
@Generated("org.jsonschema2pojo")
public class AuditItem {

	//region ATTR
	@Expose
	private long id;

	/**
	 * Id of Container Session
	 */
	@Expose
	private long session;

	@SerializedName("damage_code")
	@Expose
	private String damageCode;

	@SerializedName("damage_code_id")
	@Expose
	private long damageCodeId;

	@SerializedName("repair_code")
	@Expose
	private String repairCode;

	@SerializedName("repair_code_id")
	@Expose
	private long repairCodeId;

	@SerializedName("component_code")
	@Expose
	private String componentCode;

	@SerializedName("component_name")
	@Expose
	private String componentName;

	@SerializedName("component_code_id")
	@Expose
	private long componentCodeId;

	@SerializedName("location_code")
	@Expose
	private String locationCode;

	@Expose
	private double length;

	@Expose
	private double height;

	@Expose
	private long quantity;

	@SerializedName("is_allowed")
	@Expose
	private Boolean isAllowed;

	@SerializedName("audit_images")
	@Expose
	private List<AuditImage> auditImages;

	@SerializedName("created_at")
	@Expose
	private String createdAt;

	@SerializedName("modified_at")
	@Expose
	private String modifiedAt;

	private String uuid;

	@SerializedName("is_audited")
	@Expose
	private boolean isAudited;

	@SerializedName("is_repaired")
	@Expose
	private boolean isRepaired;

	@SerializedName("is_approved")
	@Expose
	private boolean isApproved;

	private int uploadStatus;
	//endregion

	//region GETTER AND SETTER
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public AuditItem withId(long id) {
		this.id = id;
		return this;
	}

	public String getDamageCode() {
		return damageCode;
	}

	public void setDamageCode(String damageCode) {
		this.damageCode = damageCode;
	}

	public AuditItem withDamageCode(String damageCode) {
		this.damageCode = damageCode;
		return this;
	}

	public long getDamageCodeId() {
		return damageCodeId;
	}

	public void setDamageCodeId(long damageCodeId) {
		this.damageCodeId = damageCodeId;
	}

	public AuditItem withDamageCodeId(long damageCodeId) {
		this.damageCodeId = damageCodeId;
		return this;
	}

	public String getRepairCode() {
		return repairCode;
	}

	public void setRepairCode(String repairCode) {
		this.repairCode = repairCode;
	}

	public AuditItem withRepairCode(String repairCode) {
		this.repairCode = repairCode;
		return this;
	}

	public long getRepairCodeId() {
		return repairCodeId;
	}

	public void setRepairCodeId(long repairCodeId) {
		this.repairCodeId = repairCodeId;
	}

	public AuditItem withRepairCodeId(long repairCodeId) {
		this.repairCodeId = repairCodeId;
		return this;
	}

	public String getComponentCode() {
		return componentCode;
	}

	public void setComponentCode(String componentCode) {
		this.componentCode = componentCode;
	}

	public AuditItem withComponentCode(String componentCode) {
		this.componentCode = componentCode;
		return this;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public AuditItem withComponentName(String componentName) {
		this.componentName = componentName;
		return this;
	}

	public long getComponentCodeId() {
		return componentCodeId;
	}

	public void setComponentCodeId(long componentCodeId) {
		this.componentCodeId = componentCodeId;
	}

	public AuditItem withComponentCodeId(long componentCodeId) {
		this.componentCodeId = componentCodeId;
		return this;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public AuditItem withLocationCode(String locationCode) {
		this.locationCode = locationCode;
		return this;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public AuditItem withLength(double length) {
		this.length = length;
		return this;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public AuditItem withHeight(double height) {
		this.height = height;
		return this;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public AuditItem withQuantity(long quantity) {
		this.quantity = quantity;
		return this;
	}

	public Boolean isIsAllowed() {
		return isAllowed;
	}

	public void setIsAllowed(Boolean isAllowed) {
		this.isAllowed = isAllowed;
	}

	public AuditItem withIsAllowed(Boolean isAllowed) {
		this.isAllowed = isAllowed;
		return this;
	}

	public List<AuditImage> getAuditImages() {
		return auditImages;
	}

	public void setAuditImages(List<AuditImage> auditImages) {
		this.auditImages = auditImages;
	}

	public AuditItem withAuditImages(List<AuditImage> auditImages) {
		this.auditImages = auditImages;
		return this;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public AuditItem withCreatedAt(String createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public AuditItem withModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
		return this;
	}

	public void setUuid(String audiItemUUID) {
		this.uuid = audiItemUUID;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}

	public boolean getApproved() {
		return this.isApproved;
	}

	public void setRepaired(boolean isRepaired) {
		this.isRepaired = isRepaired;
	}


	public boolean getRepaired() {
		return this.isRepaired;
	}

	public void setAudited(boolean isAudited) {
		this.isAudited = isAudited;
	}

	public boolean getAudited() {
		return this.isAudited;
	}

	public int getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(int status) {
		this.uploadStatus = status;
	}

	public void setUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
	}

	public AuditItem withUploadStatus(int status) {
		this.uploadStatus = status;
		return this;
	}

	public AuditItem withUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
		return this;
	}

	public long getSession() {
		return session;
	}

	public void setSession(long session) {
		this.session = session;
	}
	//endregion

	public AuditItem() {
		auditImages = new ArrayList<>();
		locationCode = "";
		componentCodeId = 0;
		repairCodeId = 0;
		damageCodeId = 0;
		isAllowed = null;
	}

	public JsonObject getAuditItemToUpload() {
		JsonArray auditImage = this.getAuditImagesToUpLoad();
		JsonObject auditItem = new JsonObject();
		auditItem.addProperty("damage_code_id", this.getDamageCodeId());
		auditItem.addProperty("repair_code_id", this.getRepairCodeId());
		auditItem.addProperty("component_code_id", this.getComponentCodeId());
		auditItem.addProperty("location_code", this.getLocationCode());
		auditItem.addProperty("length", this.getLength());
		auditItem.addProperty("height", this.getHeight());
		auditItem.addProperty("quantity", this.getQuantity());
		auditItem.add("audit_images", auditImage);
		return auditItem;
	}

	/**
	 * Get audit image name for post audit item
	 * Return JSONArray of audit image list look like [{name: '....'}, {name: '....'}, ...] for upload
	 *
	 * @return
	 * @throws JSONException
	 */
	public JsonArray getAuditImagesToUpLoad() {
		JsonArray audit_image = new JsonArray();
		Logger.e(String.valueOf(auditImages.size()));
		for (AuditImage auditImage : this.auditImages) {
			if (auditImage.getType() == ImageType.AUDIT.getValue()) {
				String auditImageName = auditImage.getName();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", auditImageName);
				audit_image.add(jsonObject);
			}
		}
		return audit_image;
	}

	/**
	 * Get repaired image name for post complete repair
	 * Return JSONArray of repaired image list look like [{name: '....'}, {name: '....'}, ...] for upload
	 *
	 * @return
	 * @throws JSONException
	 */
	public JsonArray getRepairedImageToUpLoad() {
		JsonArray repaired_image = new JsonArray();
		Logger.e(String.valueOf(auditImages.size()));
		for (AuditImage repairedtImage : this.auditImages) {
			if (repairedtImage.getType() == ImageType.REPAIRED.getValue()) {
				String repairedImageName = repairedtImage.getName();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", repairedImageName);
				repaired_image.add(jsonObject);
			}
		}
		return repaired_image;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AuditItem) {
			AuditItem tmp = (AuditItem) obj;

			if ((this.componentCodeId == tmp.componentCodeId
					&& this.damageCodeId == tmp.damageCodeId
					&& this.repairCodeId == tmp.repairCodeId
					&& this.locationCode.equals(tmp.getLocationCode())) || (this.uuid.equals(tmp.uuid))) {
				return true;
			} else {
				return false;
			}
		}

		return super.equals(obj);
	}

	public List<AuditImage> getListIssueImages() {
		List<AuditImage> imageList = new ArrayList<AuditImage>();

		for (AuditImage auditImage : this.getAuditImages()) {
			if (auditImage.getType() == ImageType.AUDIT.value) {
				imageList.add(auditImage);
			}
		}
		return imageList;
	}

	public List<AuditImage> getListRepairedImages() {
		List<AuditImage> imageList = new ArrayList<AuditImage>();

		for (AuditImage auditImage : this.getAuditImages()) {
			if (auditImage.getType() == ImageType.REPAIRED.value) {
				imageList.add(auditImage);
			}
		}
		return imageList;
	}

	public AuditItem mergeAuditItem(AuditItem auditItemServer) {

		this.setId(auditItemServer.getId());
		this.setIsAllowed(auditItemServer.isIsAllowed());
		this.setAudited(true);
		/*if (!auditItemServer.isIsAllowed())
	        this.setRepaired(true);*/

		//merge audit Image
		List<AuditImage> mergedAuditImages = this.auditImages;
		for (AuditImage auditImage : this.auditImages) {
			for (AuditImage auditImageServer : auditItemServer.getAuditImages()) {
				Logger.e(auditImage.getName());
				Logger.e(auditImageServer.getUrl());
				if (auditImage.getName().equals(Utils.getImageNameFromUrl(auditImageServer.getUrl()))) {
					auditImage.mergeAuditImage(auditImageServer);
				}
			}
		}
		this.setAuditImages(mergedAuditImages);
		return this;
	}

	public boolean isWashTypeItem() {

		if (this.getComponentCode() != null
				&& this.getDamageCode() != null
				&& this.getRepairCode() != null
				&& this.getLocationCode() != null) {
			if (this.getComponentCode().equals("FWA")
					&& this.getDamageCode().equals("DB")
					&& this.getRepairCode().equals("WW")
					&& this.getLocationCode().equals("BXXX")) {
				return true;
			}
		}

		return false;
	}
}