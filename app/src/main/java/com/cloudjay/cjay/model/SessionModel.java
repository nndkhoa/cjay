package com.cloudjay.cjay.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by nambv on 2015/03/03.
 */
@Table(value = "session")
@ContainerAdapter
public class SessionModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    int id;

    @Column(name = "session_id")
    String sessionId;

    @Column(name = "session_primary_key")
    long sessionPrimaryKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionPrimaryKey() {
        return sessionPrimaryKey;
    }

    public void setSessionPrimaryKey(long sessionPrimaryKey) {
        this.sessionPrimaryKey = sessionPrimaryKey;
    }
}
