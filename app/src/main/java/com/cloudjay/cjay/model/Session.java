package com.cloudjay.cjay.model;


import android.text.TextUtils;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class Session implements Serializable{

	public static final String FIELD_CONTAINER_ID = "container_id";

	//region ATTR
	@Expose
	private long id;

	private int localStep;

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Expose
	private int step;

	public boolean canRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	private boolean retry;

	@SerializedName("pre_status")
	@Expose
	private long preStatus;

	@SerializedName("modified_at")
	@Expose
	private String modifiedAt;

	@Expose
	private long status;

	@SerializedName(FIELD_CONTAINER_ID)
	@Expose
	private String containerId;

	@SerializedName("operator_code")
	@Expose
	private String operatorCode;

	@SerializedName("operator_id")
	@Expose
	private long operatorId;

	@SerializedName("depot_code")
	@Expose
	private String depotCode;

	@SerializedName("depot_id")
	@Expose
	private long depotId;

	@SerializedName("check_in_time")
	@Expose
	private String checkInTime;

	@SerializedName("check_out_time")
	@Expose
	private String checkOutTime;

	private int uploadStatus;

	@SerializedName("gate_images")
	@Expose
	private List<GateImage> gateImages;

	@SerializedName("audit_items")
	@Expose
	private List<AuditItem> auditItems;
	//endregion

	public Session() {
		gateImages = new ArrayList<GateImage>();
		auditItems = new ArrayList<AuditItem>();
	}

	//region GETTER AND SETTER

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Session withId(long id) {
		this.id = id;
		return this;
	}

	public int getLocalStep() {
		return localStep;
	}

	public void setLocalStep(int localStep) {
//		Logger.e(localStep + "");
		this.localStep = localStep;
	}

	public Session withLocalStep(int step) {
//		Logger.e(localStep + "1");
		this.localStep = step;
		return this;
	}

	public Session withStep(int step) {
		this.step = step;
		return this;
	}

	public long getPreStatus() {
		return preStatus;
	}

	public void setPreStatus(long preStatus) {
		this.preStatus = preStatus;
	}

	public Session withPreStatus(long preStatus) {
		this.preStatus = preStatus;
		return this;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public Session withStatus(long status) {
		this.status = status;
		return this;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public Session withContainerId(String containerId) {
		this.containerId = containerId;
		return this;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}

	public Session withOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
		return this;
	}

	public long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(long operatorId) {
		this.operatorId = operatorId;
	}

	public Session withOperatorId(long operatorId) {
		this.operatorId = operatorId;
		return this;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Session withModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
		return this;
	}


	public String getDepotCode() {
		return depotCode;
	}

	public void setDepotCode(String depotCode) {
		this.depotCode = depotCode;
	}

	public Session withDepotCode(String depotCode) {
		this.depotCode = depotCode;
		return this;
	}

	public long getDepotId() {
		return depotId;
	}

	public void setDepotId(long depotId) {
		this.depotId = depotId;
	}

	public Session withDepotId(long depotId) {
		this.depotId = depotId;
		return this;
	}

	public String getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}

	public Session withCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
		return this;
	}

	public String getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public Session withCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
		return this;
	}

	public List<GateImage> getGateImages() {
		return gateImages;
	}

	public void setGateImages(List<GateImage> gateImages) {
		this.gateImages = gateImages;
	}

	public Session withGateImages(List<GateImage> gateImages) {
		this.gateImages = gateImages;
		return this;
	}

	public List<AuditItem> getAuditItems() {
		return auditItems;
	}

	public void setAuditItems(List<AuditItem> auditItems) {
		this.auditItems = auditItems;
	}

	public Session withAuditItems(List<AuditItem> auditItems) {
		this.auditItems = auditItems;
		return this;
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

	public Session withUploadStatus(int status) {
		this.uploadStatus = status;
		return this;
	}

	public Session withUploadStatus(UploadStatus status) {
		this.uploadStatus = status.value;
		return this;
	}
	//endregion

	/**
	 * Get list import images in List of Gate Images
	 *
	 * @return
	 */
	public List<GateImage> getImportImages() {
		List<GateImage> imageList = new ArrayList<GateImage>();
		for (GateImage gateImage : gateImages) {
			if (gateImage.getType() == ImageType.IMPORT.value) {
				imageList.add(gateImage);
			}
		}
		return imageList;
	}

	/**
	 * Get list export images in List of Gate Images
	 *
	 * @return
	 */
	public List<GateImage> getExportImages() {
		List<GateImage> imageList = new ArrayList<GateImage>();
		for (GateImage gateImage : gateImages) {
			if (gateImage.getType() == ImageType.EXPORT.value) {
				imageList.add(gateImage);
			}
		}

		return imageList;
	}

	/**
	 * Get list audit images in List of Audit Images
	 *
	 * @return
	 */
	public List<AuditImage> getIssueImages() {
		List<AuditImage> imageList = new ArrayList<AuditImage>();

		for (AuditItem auditItem : auditItems) {
			for (AuditImage auditImage : auditItem.getAuditImages()) {
				if (auditImage.getType() == ImageType.AUDIT.value) {
					imageList.add(auditImage);
				}
			}
		}

		return imageList;
	}

	/**
	 * Get list repaired images in List of Audit Images
	 *
	 * @return
	 */
	public List<AuditImage> getRepairedImages() {
		List<AuditImage> imageList = new ArrayList<AuditImage>();

		for (AuditItem auditItem : auditItems) {
			for (AuditImage auditImage : auditItem.getAuditImages()) {
				if (auditImage.getType() == ImageType.REPAIRED.value) {
					imageList.add(auditImage);
				}
			}
		}

		return imageList;
	}

	/**
	 * Get session in Json type to upload
	 *
	 * @return
	 */
	public JsonObject getJsonSession() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("pre_status", this.preStatus);
		jsonObject.addProperty("container_id", containerId);
		jsonObject.addProperty("operator_id", operatorId);
		jsonObject.add("gate_images", this.getGateInImageToUpLoad());
		return jsonObject;
	}

	/**
	 * Return JSONArray of gate-in image list look like [{name: '....'}, {name: '....'}, ...] for upload
	 *
	 * @return
	 * @throws JSONException
	 */
	public JsonArray getGateInImageToUpLoad() {
		JsonArray gate_image = new JsonArray();
		for (GateImage gateImage : this.gateImages) {
			if (gateImage.getType() == ImageType.IMPORT.getValue()) {
				String gateImageName = gateImage.getName();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", gateImageName);
				gate_image.add(jsonObject);
			}
		}
		return gate_image;
	}

	/**
	 * Return JSONArray of gate-out image list look like [{name: '....'}, {name: '....'}, ...] for upload
	 *
	 * @return
	 * @throws JSONException
	 */
	public JsonObject getGateOutImageToUpLoad() {
		JsonObject checkOutJson = new JsonObject();
		JsonArray gate_image = new JsonArray();
		for (GateImage gateImage : this.gateImages) {
			if (gateImage.getType() == ImageType.EXPORT.getValue()) {
				String gateImageName = gateImage.getName();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", gateImageName);
				gate_image.add(jsonObject);
			}
		}
		checkOutJson.add("gate_images", gate_image);
		return checkOutJson;
	}

	/**
	 * Return JSONArray of audit items list look like [{id: xx, audit_images: [{ name : '...', ...}]}, ...] for upload
	 *
	 * @return
	 * @throws JSONException
	 */
	public JsonObject getRepairedAuditItemToUpLoad() {
		JsonObject auditItemsPut = new JsonObject();
		JsonArray auditItems = new JsonArray();
		for (AuditItem auditItem : this.auditItems) {
			JsonObject jsonObject = new JsonObject();
			long auditId = auditItem.getId();
			jsonObject.addProperty("id", auditId);
			auditItems.add(jsonObject);
			JsonArray repairedImageName = auditItem.getRepairedImageToUpLoad();
			jsonObject.add("repair_images", repairedImageName);
		}
		auditItemsPut.add("audit_items", auditItems);
		return auditItemsPut;
	}

	/**
	 * Check if container has repair images or not
	 *
	 * @return
	 */
	public boolean hasRepairImages() {
		for (AuditItem item : auditItems) {
			for (AuditImage image : item.getAuditImages()) {
				if (image.getType() == ImageType.REPAIRED.value) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Use it to check if container session is valid or not
	 */
	public boolean isValidToUpload(Step step) {

		switch (step) {

			// Chỉ cần có ít nhất một tấm hình IMPORT là hợp lệ
			case IMPORT:

				// Required operator
				if (this.getOperatorId() == 0) {
					return false;
				}

				for (GateImage image : gateImages) {
					if (image.getType() == ImageType.IMPORT.value) return true;
				}
				return false;

			// Tất cả các item đều được gán lỗi và có hình
			case AUDIT:

				if (auditItems.size() == 0) {
					return false;
				}

				for (AuditItem item : auditItems) {
					if (item.isAudited() == false || item.getUploadStatus() == UploadStatus.NONE.value)
						return false;
				}

				return true;

			// Tất cả các item đều phải có hình sau sửa chữa
			// Loi cam sua thi field cho phep sua phai la false
			case REPAIR:

				if (auditItems.size() == 0) {
					return false;
				}

				for (AuditItem item : auditItems) {
					if (item.isRepaired() == false && item.isAllowed() == true) {
						return false;
					}
				}

				return true;

			// Chỉ cần có ít nhất một tấm hình EXPORTED là hợp lệ
			case EXPORTED:
				for (GateImage image : gateImages) {
					if (image.getType() == ImageType.EXPORT.value) return true;
				}
				return false;
		}

		return false;
	}

	/**
	 * Check image in current localStep of session, if non image is missing => true
	 * Else return false
	 *
	 * @return
	 */
	public void checkRetry() {

		if (localStep == Step.REPAIR.value) {
			for (AuditItem auditItem : auditItems) {
				for (AuditImage auditImage : auditItem.getAuditImages()) {
					if (auditImage.getType() == ImageType.REPAIRED.value) {
						File file = new File(auditImage.getUrl());
						if (!file.exists()) {
							this.retry = false;
						}
					}
				}
			}

			this.retry = true;

		} else if (localStep == Step.AUDIT.value) {
			for (AuditItem auditItem : auditItems) {
				for (AuditImage auditImage : auditItem.getAuditImages()) {
					if (auditImage.getType() == ImageType.AUDIT.value) {
						File file = new File(auditImage.getUrl());
						if (!file.exists()) {
							this.retry = false;
						}
					}
				}
			}
			this.retry = true;
		} else {
			for (GateImage gateImage : gateImages) {
				File file = new File(gateImage.getUrl());
				if (!file.exists()) {
					this.retry = false;
				}
			}
			this.retry = true;
		}
	}

	/**
	 * Count total image off session
	 *
	 * @param session
	 * @return
	 */
	public int getTotalImage() {

		int totalImage = 0;
		List<AuditItem> auditItems = this.getAuditItems();

		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {
				totalImage = totalImage + auditItem.getAuditImages().size();
			}
			totalImage = totalImage + this.getGateImages().size();
			return totalImage;

		} else {

			totalImage = this.getGateImages().size();
			return totalImage;
		}
	}

	/**
	 * Get list of uploaded images
	 *
	 * @return
	 */
	public int getUploadedImage() {
		int uploadedImage = 0;
		List<AuditItem> auditItems = this.getAuditItems();

		if (auditItems != null) {
			for (AuditItem auditItem : auditItems) {

				List<AuditImage> auditImages = auditItem.getAuditImages();
				for (AuditImage auditImage : auditImages) {
					if (auditImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
						uploadedImage = uploadedImage + 1;
					}
				}

			}
		}

		List<GateImage> gateImages = this.getGateImages();
		for (GateImage gateImage : gateImages) {
			if (gateImage.getUploadStatus() == UploadStatus.COMPLETE.value) {
				uploadedImage = uploadedImage + 1;
			}
		}
		return uploadedImage;
	}

	public List<AuditItem> getListRepairedItem() {

		List<AuditItem> list = new ArrayList<AuditItem>();

		for (AuditItem auditItem : this.getAuditItems()) {
			if (auditItem.isRepaired()) {
				list.add(auditItem);
			}
		}

		return list;
	}

	/**
	 * Get audit item that have given uuid
	 *
	 * @param itemUuid
	 * @return
	 */
	public AuditItem getAuditItem(String itemUuid) {

		if (!TextUtils.isEmpty(itemUuid)) {
			for (AuditItem item : auditItems)
				if (itemUuid.equals(item.getUuid())) {
					return item;
				}
		}

		return null;
	}

	/**
	 * @param uuid
	 */
	public boolean removeAuditItem(String uuid) {

		if (!TextUtils.isEmpty(uuid)) {
			for (AuditItem item : auditItems) {
				if (uuid.equals(item.getUuid())) {
					auditItems.remove(item);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Merge new session. Use it after upload contianer to server.
	 *
	 * @param newSession
	 * @return
	 */
	public Session mergeSession(Session newSession) {

		Logger.Log(" > Merge container " + newSession.getContainerId());
//		Logger.Log("Parse basic information");
		this.setId(newSession.getId());
		this.setStep(newSession.getStep());
		this.setCheckInTime(newSession.getCheckInTime());
		this.setCheckOutTime(newSession.getCheckOutTime());

		// local step should always greater or equal to step
//		Logger.Log("Local Step: " + Step.values()[this.getLocalStep()]);
//		Logger.Log("Server Step: " + Step.values()[newSession.getStep()]);

		if (this.getLocalStep() < newSession.getStep()) {
			this.setLocalStep(newSession.getStep());
		}

		// Merge Gate Images
		// Tìm danh sách hình giống nhau, giữ danh sách local và set new id
		// Tìm danh sách hình khác nhau
		// Difference được khởi tạo là danh sách tổng hợp của client và server
		// Difference thường là danh sách hình mới từ server
//		Logger.Log("Parse list gate images");
		List<GateImage> diffGateImages = new ArrayList<>();
		diffGateImages.addAll(gateImages);
		//Set upload status for all image get from server is uploaded
		for (GateImage gateImage : newSession.getGateImages()) {
			gateImage.setUploadStatus(UploadStatus.COMPLETE.value);
		}
		diffGateImages.addAll(newSession.getGateImages());

		gateImages.retainAll(newSession.getGateImages());
		diffGateImages.removeAll(gateImages);

//		Logger.Log("Similar gate img count: " + gateImages.size());
//		Logger.Log("Difference gate img count: " + diffGateImages.size());
		// Khởi tạo các thông tin còn thiếu của list difference
		for (GateImage image : diffGateImages) {
			if (TextUtils.isEmpty(image.getName())) {
				image.setName(Utils.getImageNameFromUrl(image.getUrl()));
			}

			if (TextUtils.isEmpty(image.getUuid())) {
				image.setUuid(UUID.randomUUID().toString());
			}
		}
		gateImages.addAll(diffGateImages);

		// Merge Audit Items
		// 2 audit items bằng nhau khi giống uuid hoặc id
		if (newSession.getAuditItems() != null && newSession.getAuditItems().size() != 0) {

//			Logger.Log("Parse list audit items");
			for (AuditItem serverItem : newSession.getAuditItems()) {
				//Set upload status for all audit item is uploaded
				serverItem.setUploadStatus(UploadStatus.COMPLETE.value);
				boolean found = false;

				for (AuditItem localItem : auditItems) {
					if (serverItem.equals(localItem)) {
						found = true;

						SimpleDateFormat format = new SimpleDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);

						if (TextUtils.isEmpty(serverItem.getModifiedAt())) {
							Logger.w("Audit item id: " + serverItem.getId());
						}

						try {
							Date server = format.parse(serverItem.getModifiedAt());
							Date local = format.parse(localItem.getModifiedAt());

							// TODO: need to debug
							if (server.after(local)) {
								serverItem.merge(localItem);
								updateAuditItem(serverItem);
							}
						} catch (ParseException e) {
							Logger.e("Cannot parse modifiedAt");
							// e.printStackTrace();
						}
					}
				}

				// Nếu không tìm thấy audit item tương ứng ở local --> audit item mới
				// --> thêm mới audit item vào session
				if (!found) {
					auditItems.add(serverItem);
					Logger.Log("Add new audit item to session");
				}
			}
		}

		return this;
	}

	/**
	 * Find and update audit item information
	 *
	 * @param auditItem
	 * @return
	 */
	public boolean updateAuditItem(AuditItem auditItem) {

		// find and replace with the new one
		Logger.Log("Update audit item");
		for (AuditItem item : auditItems) {
			if (item.equals(auditItem)) {
				auditItems.remove(item);
				auditItems.add(auditItem);
				return true;
			}
		}

		return false;
	}

	/**
	 * Change audit item upload status
	 *
	 * @param containerId
	 * @param itemUuid
	 * @param status
	 * @return
	 */
	public boolean changeAuditItemUploadStatus(String containerId, String itemUuid, UploadStatus status) {
		for (AuditItem item : auditItems) {
			if (item.getUuid().equals(itemUuid)) {
				item.setUploadStatus(status);
				return true;
			}
		}
		return false;
	}

	/**
	 * Add some field to make new session return from server look like local session
	 * - Set localstep = step
	 * - Gen UUID for each audit item, image
	 * - Set upload status of all image is uploaded
	 * - Set name for all image
	 * - Set upload status of all audit item is uploaded
	 *
	 * @param session
	 * @return
	 */
	public Session changeToLocalFormat() {
		this.setLocalStep(this.getStep());

		for (GateImage gateImage : this.getGateImages()) {
			gateImage.setName(Utils.getImageNameFromUrl(gateImage.getUrl()));
			gateImage.setUploadStatus(UploadStatus.COMPLETE.value);
			gateImage.setUuid(UUID.randomUUID().toString());
		}

		for (AuditItem auditItem : this.getAuditItems()) {
			auditItem.setUuid(UUID.randomUUID().toString());
			auditItem.setUploadStatus(UploadStatus.COMPLETE.value);
			for (AuditImage auditImage : auditItem.getAuditImages()) {
				auditImage.setName(Utils.getImageNameFromUrl(auditImage.getUrl()));
				auditImage.setUploadStatus(UploadStatus.COMPLETE.value);
				auditImage.setUuid(UUID.randomUUID().toString());
			}
		}

		return this;
	}
}