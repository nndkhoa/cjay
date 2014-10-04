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

public class GateImageRealmProxy extends GateImage {

    @Override
    public long getId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("GateImage").get("id"));
    }

    @Override
    public void setId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("GateImage").get("id"), (long) value);
    }

    @Override
    public long getType() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("GateImage").get("type"));
    }

    @Override
    public void setType(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("GateImage").get("type"), (long) value);
    }

    @Override
    public String getUrl() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("GateImage").get("url"));
    }

    @Override
    public void setUrl(String value) {
        realmGetRow().setString(Realm.columnIndices.get("GateImage").get("url"), (String) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_GateImage")) {
            Table table = transaction.getTable("class_GateImage");
            table.addColumn(ColumnType.INTEGER, "id");
            table.addColumn(ColumnType.INTEGER, "type");
            table.addColumn(ColumnType.STRING, "url");
            return table;
        }
        return transaction.getTable("class_GateImage");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_GateImage")) {
            Table table = transaction.getTable("class_GateImage");
            if(table.getColumnCount() != 3) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 3; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("id")) {
                throw new IllegalStateException("Missing column 'id'");
            }
            if (columnTypes.get("id") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'id'");
            }
            if (!columnTypes.containsKey("type")) {
                throw new IllegalStateException("Missing column 'type'");
            }
            if (columnTypes.get("type") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'type'");
            }
            if (!columnTypes.containsKey("url")) {
                throw new IllegalStateException("Missing column 'url'");
            }
            if (columnTypes.get("url") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'url'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("id", "type", "url");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("GateImage = [");
        stringBuilder.append("{id:");
        stringBuilder.append(getId());
        stringBuilder.append("} ");
        stringBuilder.append("{type:");
        stringBuilder.append(getType());
        stringBuilder.append("} ");
        stringBuilder.append("{url:");
        stringBuilder.append(getUrl());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        long aLong_0 = getId();
        result = 31 * result + (int) (aLong_0 ^ (aLong_0 >>> 32));
        long aLong_1 = getType();
        result = 31 * result + (int) (aLong_1 ^ (aLong_1 >>> 32));
        String aString_2 = getUrl();
        result = 31 * result + (aString_2 != null ? aString_2.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GateImageRealmProxy aGateImage = (GateImageRealmProxy)o;
        if (getId() != aGateImage.getId()) return false;
        if (getType() != aGateImage.getType()) return false;
        if (getUrl() != null ? !getUrl().equals(aGateImage.getUrl()) : aGateImage.getUrl() != null) return false;
        return true;
    }

}
