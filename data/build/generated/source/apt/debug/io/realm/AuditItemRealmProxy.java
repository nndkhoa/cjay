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

public class AuditItemRealmProxy extends AuditItem {

    @Override
    public long getId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("AuditItem").get("id"));
    }

    @Override
    public void setId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("AuditItem").get("id"), (long) value);
    }

    @Override
    public String getDamageCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("damageCode"));
    }

    @Override
    public void setDamageCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("damageCode"), (String) value);
    }

    @Override
    public long getDamageCodeId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("AuditItem").get("damageCodeId"));
    }

    @Override
    public void setDamageCodeId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("AuditItem").get("damageCodeId"), (long) value);
    }

    @Override
    public String getRepairCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("repairCode"));
    }

    @Override
    public void setRepairCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("repairCode"), (String) value);
    }

    @Override
    public long getRepairCodeId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("AuditItem").get("repairCodeId"));
    }

    @Override
    public void setRepairCodeId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("AuditItem").get("repairCodeId"), (long) value);
    }

    @Override
    public String getComponentCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("componentCode"));
    }

    @Override
    public void setComponentCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("componentCode"), (String) value);
    }

    @Override
    public String getComponentName() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("componentName"));
    }

    @Override
    public void setComponentName(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("componentName"), (String) value);
    }

    @Override
    public long getComponentCodeId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("AuditItem").get("componentCodeId"));
    }

    @Override
    public void setComponentCodeId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("AuditItem").get("componentCodeId"), (long) value);
    }

    @Override
    public String getLocationCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("locationCode"));
    }

    @Override
    public void setLocationCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("locationCode"), (String) value);
    }

    @Override
    public double getLength() {
        return (double) realmGetRow().getDouble(Realm.columnIndices.get("AuditItem").get("length"));
    }

    @Override
    public void setLength(double value) {
        realmGetRow().setDouble(Realm.columnIndices.get("AuditItem").get("length"), (double) value);
    }

    @Override
    public double getHeight() {
        return (double) realmGetRow().getDouble(Realm.columnIndices.get("AuditItem").get("height"));
    }

    @Override
    public void setHeight(double value) {
        realmGetRow().setDouble(Realm.columnIndices.get("AuditItem").get("height"), (double) value);
    }

    @Override
    public long getQuantity() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("AuditItem").get("quantity"));
    }

    @Override
    public void setQuantity(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("AuditItem").get("quantity"), (long) value);
    }

    @Override
    public boolean isIsAllowed() {
        return (boolean) realmGetRow().getBoolean(Realm.columnIndices.get("AuditItem").get("isAllowed"));
    }

    @Override
    public void setIsAllowed(boolean value) {
        realmGetRow().setBoolean(Realm.columnIndices.get("AuditItem").get("isAllowed"), (boolean) value);
    }

    @Override
    public RealmList<com.cloudjay.cjay.data.model.AuditImage> getAuditImages() {
        return new RealmList(AuditImage.class, realmGetRow().getLinkList(Realm.columnIndices.get("AuditItem").get("auditImages")), realm);
    }

    @Override
    public void setAuditImages(RealmList<com.cloudjay.cjay.data.model.AuditImage> value) {
        LinkView links = realmGetRow().getLinkList(Realm.columnIndices.get("AuditItem").get("auditImages"));
        if (value == null) {
            return;
        }
        for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value) {
            links.add(linkedObject.realmGetRow().getIndex());
        }
    }

    @Override
    public String getCreatedAt() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("createdAt"));
    }

    @Override
    public void setCreatedAt(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("createdAt"), (String) value);
    }

    @Override
    public String getModifiedAt() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("AuditItem").get("modifiedAt"));
    }

    @Override
    public void setModifiedAt(String value) {
        realmGetRow().setString(Realm.columnIndices.get("AuditItem").get("modifiedAt"), (String) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_AuditItem")) {
            Table table = transaction.getTable("class_AuditItem");
            table.addColumn(ColumnType.INTEGER, "id");
            table.addColumn(ColumnType.STRING, "damageCode");
            table.addColumn(ColumnType.INTEGER, "damageCodeId");
            table.addColumn(ColumnType.STRING, "repairCode");
            table.addColumn(ColumnType.INTEGER, "repairCodeId");
            table.addColumn(ColumnType.STRING, "componentCode");
            table.addColumn(ColumnType.STRING, "componentName");
            table.addColumn(ColumnType.INTEGER, "componentCodeId");
            table.addColumn(ColumnType.STRING, "locationCode");
            table.addColumn(ColumnType.DOUBLE, "length");
            table.addColumn(ColumnType.DOUBLE, "height");
            table.addColumn(ColumnType.INTEGER, "quantity");
            table.addColumn(ColumnType.BOOLEAN, "isAllowed");
            if (!transaction.hasTable("class_AuditImage")) {
                AuditImageRealmProxy.initTable(transaction);
            }
            table.addColumnLink(ColumnType.LINK_LIST, "auditImages", transaction.getTable("class_AuditImage"));
            table.addColumn(ColumnType.STRING, "createdAt");
            table.addColumn(ColumnType.STRING, "modifiedAt");
            return table;
        }
        return transaction.getTable("class_AuditItem");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_AuditItem")) {
            Table table = transaction.getTable("class_AuditItem");
            if(table.getColumnCount() != 16) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 16; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("id")) {
                throw new IllegalStateException("Missing column 'id'");
            }
            if (columnTypes.get("id") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'id'");
            }
            if (!columnTypes.containsKey("damageCode")) {
                throw new IllegalStateException("Missing column 'damageCode'");
            }
            if (columnTypes.get("damageCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'damageCode'");
            }
            if (!columnTypes.containsKey("damageCodeId")) {
                throw new IllegalStateException("Missing column 'damageCodeId'");
            }
            if (columnTypes.get("damageCodeId") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'damageCodeId'");
            }
            if (!columnTypes.containsKey("repairCode")) {
                throw new IllegalStateException("Missing column 'repairCode'");
            }
            if (columnTypes.get("repairCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'repairCode'");
            }
            if (!columnTypes.containsKey("repairCodeId")) {
                throw new IllegalStateException("Missing column 'repairCodeId'");
            }
            if (columnTypes.get("repairCodeId") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'repairCodeId'");
            }
            if (!columnTypes.containsKey("componentCode")) {
                throw new IllegalStateException("Missing column 'componentCode'");
            }
            if (columnTypes.get("componentCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'componentCode'");
            }
            if (!columnTypes.containsKey("componentName")) {
                throw new IllegalStateException("Missing column 'componentName'");
            }
            if (columnTypes.get("componentName") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'componentName'");
            }
            if (!columnTypes.containsKey("componentCodeId")) {
                throw new IllegalStateException("Missing column 'componentCodeId'");
            }
            if (columnTypes.get("componentCodeId") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'componentCodeId'");
            }
            if (!columnTypes.containsKey("locationCode")) {
                throw new IllegalStateException("Missing column 'locationCode'");
            }
            if (columnTypes.get("locationCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'locationCode'");
            }
            if (!columnTypes.containsKey("length")) {
                throw new IllegalStateException("Missing column 'length'");
            }
            if (columnTypes.get("length") != ColumnType.DOUBLE) {
                throw new IllegalStateException("Invalid type 'double' for column 'length'");
            }
            if (!columnTypes.containsKey("height")) {
                throw new IllegalStateException("Missing column 'height'");
            }
            if (columnTypes.get("height") != ColumnType.DOUBLE) {
                throw new IllegalStateException("Invalid type 'double' for column 'height'");
            }
            if (!columnTypes.containsKey("quantity")) {
                throw new IllegalStateException("Missing column 'quantity'");
            }
            if (columnTypes.get("quantity") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'quantity'");
            }
            if (!columnTypes.containsKey("isAllowed")) {
                throw new IllegalStateException("Missing column 'isAllowed'");
            }
            if (columnTypes.get("isAllowed") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'isAllowed'");
            }
            if(!columnTypes.containsKey("auditImages")) {
                throw new IllegalStateException("Missing column 'auditImages'");
            }
            if(columnTypes.get("auditImages") != ColumnType.LINK_LIST) {
                throw new IllegalStateException("Invalid type 'AuditImage' for column 'auditImages'");
            }
            if (!transaction.hasTable("class_AuditImage")) {
                throw new IllegalStateException("Missing table 'class_AuditImage' for column 'auditImages'");
            }
            if (!columnTypes.containsKey("createdAt")) {
                throw new IllegalStateException("Missing column 'createdAt'");
            }
            if (columnTypes.get("createdAt") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'createdAt'");
            }
            if (!columnTypes.containsKey("modifiedAt")) {
                throw new IllegalStateException("Missing column 'modifiedAt'");
            }
            if (columnTypes.get("modifiedAt") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'modifiedAt'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("id", "damageCode", "damageCodeId", "repairCode", "repairCodeId", "componentCode", "componentName", "componentCodeId", "locationCode", "length", "height", "quantity", "isAllowed", "auditImages", "createdAt", "modifiedAt");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("AuditItem = [");
        stringBuilder.append("{id:");
        stringBuilder.append(getId());
        stringBuilder.append("} ");
        stringBuilder.append("{damageCode:");
        stringBuilder.append(getDamageCode());
        stringBuilder.append("} ");
        stringBuilder.append("{damageCodeId:");
        stringBuilder.append(getDamageCodeId());
        stringBuilder.append("} ");
        stringBuilder.append("{repairCode:");
        stringBuilder.append(getRepairCode());
        stringBuilder.append("} ");
        stringBuilder.append("{repairCodeId:");
        stringBuilder.append(getRepairCodeId());
        stringBuilder.append("} ");
        stringBuilder.append("{componentCode:");
        stringBuilder.append(getComponentCode());
        stringBuilder.append("} ");
        stringBuilder.append("{componentName:");
        stringBuilder.append(getComponentName());
        stringBuilder.append("} ");
        stringBuilder.append("{componentCodeId:");
        stringBuilder.append(getComponentCodeId());
        stringBuilder.append("} ");
        stringBuilder.append("{locationCode:");
        stringBuilder.append(getLocationCode());
        stringBuilder.append("} ");
        stringBuilder.append("{length:");
        stringBuilder.append(getLength());
        stringBuilder.append("} ");
        stringBuilder.append("{height:");
        stringBuilder.append(getHeight());
        stringBuilder.append("} ");
        stringBuilder.append("{quantity:");
        stringBuilder.append(getQuantity());
        stringBuilder.append("} ");
        stringBuilder.append("{isAllowed:");
        stringBuilder.append(isIsAllowed());
        stringBuilder.append("} ");
        stringBuilder.append("{auditImages:");
        stringBuilder.append(getAuditImages());
        stringBuilder.append("} ");
        stringBuilder.append("{createdAt:");
        stringBuilder.append(getCreatedAt());
        stringBuilder.append("} ");
        stringBuilder.append("{modifiedAt:");
        stringBuilder.append(getModifiedAt());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        long aLong_0 = getId();
        result = 31 * result + (int) (aLong_0 ^ (aLong_0 >>> 32));
        String aString_1 = getDamageCode();
        result = 31 * result + (aString_1 != null ? aString_1.hashCode() : 0);
        long aLong_2 = getDamageCodeId();
        result = 31 * result + (int) (aLong_2 ^ (aLong_2 >>> 32));
        String aString_3 = getRepairCode();
        result = 31 * result + (aString_3 != null ? aString_3.hashCode() : 0);
        long aLong_4 = getRepairCodeId();
        result = 31 * result + (int) (aLong_4 ^ (aLong_4 >>> 32));
        String aString_5 = getComponentCode();
        result = 31 * result + (aString_5 != null ? aString_5.hashCode() : 0);
        String aString_6 = getComponentName();
        result = 31 * result + (aString_6 != null ? aString_6.hashCode() : 0);
        long aLong_7 = getComponentCodeId();
        result = 31 * result + (int) (aLong_7 ^ (aLong_7 >>> 32));
        String aString_8 = getLocationCode();
        result = 31 * result + (aString_8 != null ? aString_8.hashCode() : 0);
        long temp_9 = Double.doubleToLongBits(getLength());
        result = 31 * result + (int) (temp_9 ^ (temp_9 >>> 32));
        long temp_10 = Double.doubleToLongBits(getHeight());
        result = 31 * result + (int) (temp_10 ^ (temp_10 >>> 32));
        long aLong_11 = getQuantity();
        result = 31 * result + (int) (aLong_11 ^ (aLong_11 >>> 32));
        result = 31 * result + (isIsAllowed() ? 1 : 0);
        io.realm.RealmList<com.cloudjay.cjay.data.model.AuditImage> temp_13 = getAuditImages();
        result = 31 * result + (temp_13 != null ? temp_13.hashCode() : 0);
        String aString_14 = getCreatedAt();
        result = 31 * result + (aString_14 != null ? aString_14.hashCode() : 0);
        String aString_15 = getModifiedAt();
        result = 31 * result + (aString_15 != null ? aString_15.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditItemRealmProxy aAuditItem = (AuditItemRealmProxy)o;
        if (getId() != aAuditItem.getId()) return false;
        if (getDamageCode() != null ? !getDamageCode().equals(aAuditItem.getDamageCode()) : aAuditItem.getDamageCode() != null) return false;
        if (getDamageCodeId() != aAuditItem.getDamageCodeId()) return false;
        if (getRepairCode() != null ? !getRepairCode().equals(aAuditItem.getRepairCode()) : aAuditItem.getRepairCode() != null) return false;
        if (getRepairCodeId() != aAuditItem.getRepairCodeId()) return false;
        if (getComponentCode() != null ? !getComponentCode().equals(aAuditItem.getComponentCode()) : aAuditItem.getComponentCode() != null) return false;
        if (getComponentName() != null ? !getComponentName().equals(aAuditItem.getComponentName()) : aAuditItem.getComponentName() != null) return false;
        if (getComponentCodeId() != aAuditItem.getComponentCodeId()) return false;
        if (getLocationCode() != null ? !getLocationCode().equals(aAuditItem.getLocationCode()) : aAuditItem.getLocationCode() != null) return false;
        if (Double.compare(getLength(), aAuditItem.getLength()) != 0) return false;
        if (Double.compare(getHeight(), aAuditItem.getHeight()) != 0) return false;
        if (getQuantity() != aAuditItem.getQuantity()) return false;
        if (isIsAllowed() != aAuditItem.isIsAllowed()) return false;
        if (getAuditImages() != null ? !getAuditImages().equals(aAuditItem.getAuditImages()) : aAuditItem.getAuditImages() != null) return false;
        if (getCreatedAt() != null ? !getCreatedAt().equals(aAuditItem.getCreatedAt()) : aAuditItem.getCreatedAt() != null) return false;
        if (getModifiedAt() != null ? !getModifiedAt().equals(aAuditItem.getModifiedAt()) : aAuditItem.getModifiedAt() != null) return false;
        return true;
    }

}
