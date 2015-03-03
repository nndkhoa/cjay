package com.cloudjay.cjay.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by nambv on 2015/03/03.
 */
@Table(value = "upload")
public class UploadModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    int id;

    @Column(name = "upload_object")
    String uploadObject;

    @Column(name = "object_type")
    int objectType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUploadObject() {
        return uploadObject;
    }

    public void setUploadObject(String uploadObject) {
        this.uploadObject = uploadObject;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }
}
