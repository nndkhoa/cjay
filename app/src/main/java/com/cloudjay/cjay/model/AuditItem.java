package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class AuditItem {

    @Expose
    private long id;
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
    private boolean isAllowed;
    @SerializedName("audit_images")
    @Expose
    private List<AuditImage> auditImages;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("modified_at")
    @Expose
    private String modifiedAt;

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

    /**
     * Get audit image name for post audit item
     * Return JSONArray of audit image list look like [{name: '....'}, {name: '....'}, ...] for upload
     * @return
     * @throws JSONException
     */
    public JSONArray getAuditImageToUpLoad() throws JSONException {
        JSONArray audit_image = new JSONArray();
        Logger.e(String.valueOf(auditImages.size()));
        for (AuditImage auditImage : this.auditImages) {
            if (auditImage.getType() == ImageType.AUDIT.getValue()) {
                String auditImageName = auditImage.getName();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", auditImageName);
                audit_image.put(jsonObject);
            }
        }
        return audit_image;
    }

    /**
     * Get repaired image name for post complete repair
     * Return JSONArray of repaired image list look like [{name: '....'}, {name: '....'}, ...] for upload
     * @return
     * @throws JSONException
     */
    public JSONArray getRepairedImageToUpLoad() throws JSONException {
        JSONArray repaired_image = new JSONArray();
        Logger.e(String.valueOf(auditImages.size()));
        for (AuditImage repairedtImage : this.auditImages) {
            if (repairedtImage.getType() == ImageType.REPAIRED.getValue()) {
                String repairedImageName = repairedtImage.getName();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", repairedImageName);
                repaired_image.put(jsonObject);
            }
        }
        return repaired_image;
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

    public boolean isIsAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }

    public AuditItem withIsAllowed(boolean isAllowed) {
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

}