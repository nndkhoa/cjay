package io.realm;


import com.cloudjay.cjay.data.model.*;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Row;
import io.realm.internal.Table;
import java.util.*;

public class SessionRealmProxy extends Session {

    @Override
    public long getId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("id"));
    }

    @Override
    public void setId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("id"), (long) value);
    }

    @Override
    public long getStep() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("step"));
    }

    @Override
    public void setStep(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("step"), (long) value);
    }

    @Override
    public long getPreStatus() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("preStatus"));
    }

    @Override
    public void setPreStatus(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("preStatus"), (long) value);
    }

    @Override
    public long getStatus() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("status"));
    }

    @Override
    public void setStatus(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("status"), (long) value);
    }

    @Override
    public String getContainerId() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("Session").get("containerId"));
    }

    @Override
    public void setContainerId(String value) {
        realmGetRow().setString(Realm.columnIndices.get("Session").get("containerId"), (String) value);
    }

    @Override
    public String getOperatorCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("Session").get("operatorCode"));
    }

    @Override
    public void setOperatorCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("Session").get("operatorCode"), (String) value);
    }

    @Override
    public long getOperatorId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("operatorId"));
    }

    @Override
    public void setOperatorId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("operatorId"), (long) value);
    }

    @Override
    public String getDepotCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("Session").get("depotCode"));
    }

    @Override
    public void setDepotCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("Session").get("depotCode"), (String) value);
    }

    @Override
    public long getDepotId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("Session").get("depotId"));
    }

    @Override
    public void setDepotId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("Session").get("depotId"), (long) value);
    }

    @Override
    public String getCheckInTime() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("Session").get("checkInTime"));
    }

    @Override
    public void setCheckInTime(String value) {
        realmGetRow().setString(Realm.columnIndices.get("Session").get("checkInTime"), (String) value);
    }

    @Override
    public String getCheckOutTime() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("Session").get("checkOutTime"));
    }

    @Override
    public void setCheckOutTime(String value) {
        realmGetRow().setString(Realm.columnIndices.get("Session").get("checkOutTime"), (String) value);
    }

    @Override
    public RealmList<com.cloudjay.cjay.data.model.GateImage> getGateImages() {
        return new RealmList(GateImage.class, realmGetRow().getLinkList(Realm.columnIndices.get("Session").get("gateImages")), realm);
    }

    @Override
    public void setGateImages(RealmList<com.cloudjay.cjay.data.model.GateImage> value) {
        LinkView links = realmGetRow().getLinkList(Realm.columnIndices.get("Session").get("gateImages"));
        if (value == null) {
            return;
        }
        for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value) {
            links.add(linkedObject.realmGetRow().getIndex());
        }
    }

    @Override
    public RealmList<com.cloudjay.cjay.data.model.AuditItem> getAuditItems() {
        return new RealmList(AuditItem.class, realmGetRow().getLinkList(Realm.columnIndices.get("Session").get("auditItems")), realm);
    }

    @Override
    public void setAuditItems(RealmList<com.cloudjay.cjay.data.model.AuditItem> value) {
        LinkView links = realmGetRow().getLinkList(Realm.columnIndices.get("Session").get("auditItems"));
        if (value == null) {
            return;
        }
        for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value) {
            links.add(linkedObject.realmGetRow().getIndex());
        }
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_Session")) {
            Table table = transaction.getTable("class_Session");
            table.addColumn(ColumnType.INTEGER, "id");
            table.addColumn(ColumnType.INTEGER, "step");
            table.addColumn(ColumnType.INTEGER, "preStatus");
            table.addColumn(ColumnType.INTEGER, "status");
            table.addColumn(ColumnType.STRING, "containerId");
            table.addColumn(ColumnType.STRING, "operatorCode");
            table.addColumn(ColumnType.INTEGER, "operatorId");
            table.addColumn(ColumnType.STRING, "depotCode");
            table.addColumn(ColumnType.INTEGER, "depotId");
            table.addColumn(ColumnType.STRING, "checkInTime");
            table.addColumn(ColumnType.STRING, "checkOutTime");
            if (!transaction.hasTable("class_GateImage")) {
                GateImageRealmProxy.initTable(transaction);
            }
            table.addColumnLink(ColumnType.LINK_LIST, "gateImages", transaction.getTable("class_GateImage"));
            if (!transaction.hasTable("class_AuditItem")) {
                AuditItemRealmProxy.initTable(transaction);
            }
            table.addColumnLink(ColumnType.LINK_LIST, "auditItems", transaction.getTable("class_AuditItem"));
            return table;
        }
        return transaction.getTable("class_Session");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_Session")) {
            Table table = transaction.getTable("class_Session");
            if(table.getColumnCount() != 13) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 13; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("id")) {
                throw new IllegalStateException("Missing column 'id'");
            }
            if (columnTypes.get("id") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'id'");
            }
            if (!columnTypes.containsKey("step")) {
                throw new IllegalStateException("Missing column 'step'");
            }
            if (columnTypes.get("step") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'step'");
            }
            if (!columnTypes.containsKey("preStatus")) {
                throw new IllegalStateException("Missing column 'preStatus'");
            }
            if (columnTypes.get("preStatus") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'preStatus'");
            }
            if (!columnTypes.containsKey("status")) {
                throw new IllegalStateException("Missing column 'status'");
            }
            if (columnTypes.get("status") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'status'");
            }
            if (!columnTypes.containsKey("containerId")) {
                throw new IllegalStateException("Missing column 'containerId'");
            }
            if (columnTypes.get("containerId") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'containerId'");
            }
            if (!columnTypes.containsKey("operatorCode")) {
                throw new IllegalStateException("Missing column 'operatorCode'");
            }
            if (columnTypes.get("operatorCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'operatorCode'");
            }
            if (!columnTypes.containsKey("operatorId")) {
                throw new IllegalStateException("Missing column 'operatorId'");
            }
            if (columnTypes.get("operatorId") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'operatorId'");
            }
            if (!columnTypes.containsKey("depotCode")) {
                throw new IllegalStateException("Missing column 'depotCode'");
            }
            if (columnTypes.get("depotCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'depotCode'");
            }
            if (!columnTypes.containsKey("depotId")) {
                throw new IllegalStateException("Missing column 'depotId'");
            }
            if (columnTypes.get("depotId") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'depotId'");
            }
            if (!columnTypes.containsKey("checkInTime")) {
                throw new IllegalStateException("Missing column 'checkInTime'");
            }
            if (columnTypes.get("checkInTime") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'checkInTime'");
            }
            if (!columnTypes.containsKey("checkOutTime")) {
                throw new IllegalStateException("Missing column 'checkOutTime'");
            }
            if (columnTypes.get("checkOutTime") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'checkOutTime'");
            }
            if(!columnTypes.containsKey("gateImages")) {
                throw new IllegalStateException("Missing column 'gateImages'");
            }
            if(columnTypes.get("gateImages") != ColumnType.LINK_LIST) {
                throw new IllegalStateException("Invalid type 'GateImage' for column 'gateImages'");
            }
            if (!transaction.hasTable("class_GateImage")) {
                throw new IllegalStateException("Missing table 'class_GateImage' for column 'gateImages'");
            }
            if(!columnTypes.containsKey("auditItems")) {
                throw new IllegalStateException("Missing column 'auditItems'");
            }
            if(columnTypes.get("auditItems") != ColumnType.LINK_LIST) {
                throw new IllegalStateException("Invalid type 'AuditItem' for column 'auditItems'");
            }
            if (!transaction.hasTable("class_AuditItem")) {
                throw new IllegalStateException("Missing table 'class_AuditItem' for column 'auditItems'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("id", "step", "preStatus", "status", "containerId", "operatorCode", "operatorId", "depotCode", "depotId", "checkInTime", "checkOutTime", "gateImages", "auditItems");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Session = [");
        stringBuilder.append("{id:");
        stringBuilder.append(getId());
        stringBuilder.append("} ");
        stringBuilder.append("{step:");
        stringBuilder.append(getStep());
        stringBuilder.append("} ");
        stringBuilder.append("{preStatus:");
        stringBuilder.append(getPreStatus());
        stringBuilder.append("} ");
        stringBuilder.append("{status:");
        stringBuilder.append(getStatus());
        stringBuilder.append("} ");
        stringBuilder.append("{containerId:");
        stringBuilder.append(getContainerId());
        stringBuilder.append("} ");
        stringBuilder.append("{operatorCode:");
        stringBuilder.append(getOperatorCode());
        stringBuilder.append("} ");
        stringBuilder.append("{operatorId:");
        stringBuilder.append(getOperatorId());
        stringBuilder.append("} ");
        stringBuilder.append("{depotCode:");
        stringBuilder.append(getDepotCode());
        stringBuilder.append("} ");
        stringBuilder.append("{depotId:");
        stringBuilder.append(getDepotId());
        stringBuilder.append("} ");
        stringBuilder.append("{checkInTime:");
        stringBuilder.append(getCheckInTime());
        stringBuilder.append("} ");
        stringBuilder.append("{checkOutTime:");
        stringBuilder.append(getCheckOutTime());
        stringBuilder.append("} ");
        stringBuilder.append("{gateImages:");
        stringBuilder.append(getGateImages());
        stringBuilder.append("} ");
        stringBuilder.append("{auditItems:");
        stringBuilder.append(getAuditItems());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        long aLong_0 = getId();
        result = 31 * result + (int) (aLong_0 ^ (aLong_0 >>> 32));
        long aLong_1 = getStep();
        result = 31 * result + (int) (aLong_1 ^ (aLong_1 >>> 32));
        long aLong_2 = getPreStatus();
        result = 31 * result + (int) (aLong_2 ^ (aLong_2 >>> 32));
        long aLong_3 = getStatus();
        result = 31 * result + (int) (aLong_3 ^ (aLong_3 >>> 32));
        String aString_4 = getContainerId();
        result = 31 * result + (aString_4 != null ? aString_4.hashCode() : 0);
        String aString_5 = getOperatorCode();
        result = 31 * result + (aString_5 != null ? aString_5.hashCode() : 0);
        long aLong_6 = getOperatorId();
        result = 31 * result + (int) (aLong_6 ^ (aLong_6 >>> 32));
        String aString_7 = getDepotCode();
        result = 31 * result + (aString_7 != null ? aString_7.hashCode() : 0);
        long aLong_8 = getDepotId();
        result = 31 * result + (int) (aLong_8 ^ (aLong_8 >>> 32));
        String aString_9 = getCheckInTime();
        result = 31 * result + (aString_9 != null ? aString_9.hashCode() : 0);
        String aString_10 = getCheckOutTime();
        result = 31 * result + (aString_10 != null ? aString_10.hashCode() : 0);
        io.realm.RealmList<com.cloudjay.cjay.data.model.GateImage> temp_11 = getGateImages();
        result = 31 * result + (temp_11 != null ? temp_11.hashCode() : 0);
        io.realm.RealmList<com.cloudjay.cjay.data.model.AuditItem> temp_12 = getAuditItems();
        result = 31 * result + (temp_12 != null ? temp_12.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionRealmProxy aSession = (SessionRealmProxy)o;
        if (getId() != aSession.getId()) return false;
        if (getStep() != aSession.getStep()) return false;
        if (getPreStatus() != aSession.getPreStatus()) return false;
        if (getStatus() != aSession.getStatus()) return false;
        if (getContainerId() != null ? !getContainerId().equals(aSession.getContainerId()) : aSession.getContainerId() != null) return false;
        if (getOperatorCode() != null ? !getOperatorCode().equals(aSession.getOperatorCode()) : aSession.getOperatorCode() != null) return false;
        if (getOperatorId() != aSession.getOperatorId()) return false;
        if (getDepotCode() != null ? !getDepotCode().equals(aSession.getDepotCode()) : aSession.getDepotCode() != null) return false;
        if (getDepotId() != aSession.getDepotId()) return false;
        if (getCheckInTime() != null ? !getCheckInTime().equals(aSession.getCheckInTime()) : aSession.getCheckInTime() != null) return false;
        if (getCheckOutTime() != null ? !getCheckOutTime().equals(aSession.getCheckOutTime()) : aSession.getCheckOutTime() != null) return false;
        if (getGateImages() != null ? !getGateImages().equals(aSession.getGateImages()) : aSession.getGateImages() != null) return false;
        if (getAuditItems() != null ? !getAuditItems().equals(aSession.getAuditItems()) : aSession.getAuditItems() != null) return false;
        return true;
    }

}
