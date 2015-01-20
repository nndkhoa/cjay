package com.cloudjay.cjay.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class NotificationItem {

	@SerializedName("object_type")
	@Expose
	private String objectType;
	@SerializedName("object_id")
	@Expose
	private long objectId;
	@SerializedName("message_id")
	@Expose
	private String messageId;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @SerializedName("container_id")
    @Expose
    private String containerId;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public NotificationItem withObjectType(String objectType) {
		this.objectType = objectType;
		return this;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public NotificationItem withObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public NotificationItem withMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}