package com.cloudjay.cjay.model;


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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class Session implements Serializable {

    public static final String FIELD_CONTAINER_ID = "container_id";

    //region ATTR
    @Expose
    private long id;

    @Expose
    private long step;

    public long getServerStep() {
        return serverStep;
    }

    public void setServerStep(long serverStep) {
        this.serverStep = serverStep;
    }

    private long serverStep;

    @SerializedName("pre_status")
    @Expose
    private long preStatus;

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

    public boolean isCanRetry() {
        return canRetry;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    private boolean canRetry;

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

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public Session withStep(long step) {
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
    public JsonArray getGateOutImageToUpLoad() {
        JsonArray gate_image = new JsonArray();
        for (GateImage gateImage : this.gateImages) {
            if (gateImage.getType() == ImageType.EXPORT.getValue()) {
                String gateImageName = gateImage.getName();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", gateImageName);
                gate_image.add(jsonObject);
            }
        }
        return gate_image;
    }

    /**
     * Return JSONArray of audit items list look like [{id: xx, audit_images: [{ name : '...', ...}]}, ...] for upload
     *
     * @return
     * @throws JSONException
     */
    public JsonArray getRepairedAuditItemToUpLoad() {
        JsonArray auditItems = new JsonArray();
        for (AuditItem auditItem : this.auditItems) {
            JsonObject jsonObject = new JsonObject();
            long auditId = auditItem.getId();
            jsonObject.addProperty("name", auditId);
            auditItems.add(jsonObject);
            JsonArray repairedImageName = auditItem.getRepairedImageToUpLoad();
            jsonObject.add("repair_images", repairedImageName);
        }
        return auditItems;
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
                for (GateImage image : gateImages) {
                    if (image.getType() == ImageType.IMPORT.value) return true;
                }
                return false;

            // Tất cả các item đều được gán lỗi và có hình
            case AUDIT:
                for (AuditItem item : auditItems) {
                    if (item.getAudited() == false)
                        return false;
                }

                return true;

            // Tất cả các item đều phải có hình sau sửa chữa
            case REPAIR:
                for (AuditItem item : auditItems) {
                    if (item.getRepaired() == false) {
                        return false;
                    }
                }

                return true;

            // Chỉ cần có ít nhất một tấm hình EXPORT là hợp lệ
            case EXPORT:
                for (GateImage image : gateImages) {
                    if (image.getType() == ImageType.EXPORT.value) return true;
                }
                return false;
        }

        return false;
    }

    /**
     * Check image in current step of session, if non image is missing => true
     * Else return false
     * @return
     */
    public void checkRetry() {
        if (step == Step.REPAIR.value) {
            for (AuditItem auditItem : auditItems) {
                for (AuditImage auditImage : auditItem.getAuditImages()) {
                    if (auditImage.getType() == ImageType.REPAIRED.value) {
                        File file = new File(auditImage.getUrl());
                        if (!file.exists()) {
                            this.canRetry = false;
                        }
                    }
                }
            }
            this.canRetry = true;

        } else if (step == Step.AUDIT.value) {
            for (AuditItem auditItem : auditItems) {
                for (AuditImage auditImage : auditItem.getAuditImages()) {
                    if (auditImage.getType() == ImageType.AUDIT.value) {
                        File file = new File(auditImage.getUrl());
                        if (!file.exists()) {
                            this.canRetry = false;
                        }
                    }
                }
            }
            this.canRetry = true;
        } else {
            for (GateImage gateImage : gateImages) {
                File file = new File(gateImage.getUrl());
                if (!file.exists()) {
                    this.canRetry = false;
                }
            }
            this.canRetry = true;
        }
    }
}