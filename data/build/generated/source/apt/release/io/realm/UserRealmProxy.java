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

public class UserRealmProxy extends User {

    @Override
    public long getId() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("User").get("id"));
    }

    @Override
    public void setId(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("User").get("id"), (long) value);
    }

    @Override
    public String getFirstName() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("firstName"));
    }

    @Override
    public void setFirstName(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("firstName"), (String) value);
    }

    @Override
    public String getLastName() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("lastName"));
    }

    @Override
    public void setLastName(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("lastName"), (String) value);
    }

    @Override
    public String getPhone() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("phone"));
    }

    @Override
    public void setPhone(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("phone"), (String) value);
    }

    @Override
    public long getDialingCode() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("User").get("dialingCode"));
    }

    @Override
    public void setDialingCode(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("User").get("dialingCode"), (long) value);
    }

    @Override
    public String getUsername() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("username"));
    }

    @Override
    public void setUsername(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("username"), (String) value);
    }

    @Override
    public String getEmail() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("email"));
    }

    @Override
    public void setEmail(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("email"), (String) value);
    }

    @Override
    public String getFullName() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("fullName"));
    }

    @Override
    public void setFullName(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("fullName"), (String) value);
    }

    @Override
    public String getAvatarUrl() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("avatarUrl"));
    }

    @Override
    public void setAvatarUrl(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("avatarUrl"), (String) value);
    }

    @Override
    public long getRole() {
        return (long) realmGetRow().getLong(Realm.columnIndices.get("User").get("role"));
    }

    @Override
    public void setRole(long value) {
        realmGetRow().setLong(Realm.columnIndices.get("User").get("role"), (long) value);
    }

    @Override
    public String getRoleName() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("roleName"));
    }

    @Override
    public void setRoleName(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("roleName"), (String) value);
    }

    @Override
    public String getDepotCode() {
        return (java.lang.String) realmGetRow().getString(Realm.columnIndices.get("User").get("depotCode"));
    }

    @Override
    public void setDepotCode(String value) {
        realmGetRow().setString(Realm.columnIndices.get("User").get("depotCode"), (String) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_User")) {
            Table table = transaction.getTable("class_User");
            table.addColumn(ColumnType.INTEGER, "id");
            table.addColumn(ColumnType.STRING, "firstName");
            table.addColumn(ColumnType.STRING, "lastName");
            table.addColumn(ColumnType.STRING, "phone");
            table.addColumn(ColumnType.INTEGER, "dialingCode");
            table.addColumn(ColumnType.STRING, "username");
            table.addColumn(ColumnType.STRING, "email");
            table.addColumn(ColumnType.STRING, "fullName");
            table.addColumn(ColumnType.STRING, "avatarUrl");
            table.addColumn(ColumnType.INTEGER, "role");
            table.addColumn(ColumnType.STRING, "roleName");
            table.addColumn(ColumnType.STRING, "depotCode");
            return table;
        }
        return transaction.getTable("class_User");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_User")) {
            Table table = transaction.getTable("class_User");
            if(table.getColumnCount() != 12) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 12; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("id")) {
                throw new IllegalStateException("Missing column 'id'");
            }
            if (columnTypes.get("id") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'id'");
            }
            if (!columnTypes.containsKey("firstName")) {
                throw new IllegalStateException("Missing column 'firstName'");
            }
            if (columnTypes.get("firstName") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'firstName'");
            }
            if (!columnTypes.containsKey("lastName")) {
                throw new IllegalStateException("Missing column 'lastName'");
            }
            if (columnTypes.get("lastName") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'lastName'");
            }
            if (!columnTypes.containsKey("phone")) {
                throw new IllegalStateException("Missing column 'phone'");
            }
            if (columnTypes.get("phone") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'phone'");
            }
            if (!columnTypes.containsKey("dialingCode")) {
                throw new IllegalStateException("Missing column 'dialingCode'");
            }
            if (columnTypes.get("dialingCode") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'dialingCode'");
            }
            if (!columnTypes.containsKey("username")) {
                throw new IllegalStateException("Missing column 'username'");
            }
            if (columnTypes.get("username") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'username'");
            }
            if (!columnTypes.containsKey("email")) {
                throw new IllegalStateException("Missing column 'email'");
            }
            if (columnTypes.get("email") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'email'");
            }
            if (!columnTypes.containsKey("fullName")) {
                throw new IllegalStateException("Missing column 'fullName'");
            }
            if (columnTypes.get("fullName") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'fullName'");
            }
            if (!columnTypes.containsKey("avatarUrl")) {
                throw new IllegalStateException("Missing column 'avatarUrl'");
            }
            if (columnTypes.get("avatarUrl") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'avatarUrl'");
            }
            if (!columnTypes.containsKey("role")) {
                throw new IllegalStateException("Missing column 'role'");
            }
            if (columnTypes.get("role") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'role'");
            }
            if (!columnTypes.containsKey("roleName")) {
                throw new IllegalStateException("Missing column 'roleName'");
            }
            if (columnTypes.get("roleName") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'roleName'");
            }
            if (!columnTypes.containsKey("depotCode")) {
                throw new IllegalStateException("Missing column 'depotCode'");
            }
            if (columnTypes.get("depotCode") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'depotCode'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("id", "firstName", "lastName", "phone", "dialingCode", "username", "email", "fullName", "avatarUrl", "role", "roleName", "depotCode");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("User = [");
        stringBuilder.append("{id:");
        stringBuilder.append(getId());
        stringBuilder.append("} ");
        stringBuilder.append("{firstName:");
        stringBuilder.append(getFirstName());
        stringBuilder.append("} ");
        stringBuilder.append("{lastName:");
        stringBuilder.append(getLastName());
        stringBuilder.append("} ");
        stringBuilder.append("{phone:");
        stringBuilder.append(getPhone());
        stringBuilder.append("} ");
        stringBuilder.append("{dialingCode:");
        stringBuilder.append(getDialingCode());
        stringBuilder.append("} ");
        stringBuilder.append("{username:");
        stringBuilder.append(getUsername());
        stringBuilder.append("} ");
        stringBuilder.append("{email:");
        stringBuilder.append(getEmail());
        stringBuilder.append("} ");
        stringBuilder.append("{fullName:");
        stringBuilder.append(getFullName());
        stringBuilder.append("} ");
        stringBuilder.append("{avatarUrl:");
        stringBuilder.append(getAvatarUrl());
        stringBuilder.append("} ");
        stringBuilder.append("{role:");
        stringBuilder.append(getRole());
        stringBuilder.append("} ");
        stringBuilder.append("{roleName:");
        stringBuilder.append(getRoleName());
        stringBuilder.append("} ");
        stringBuilder.append("{depotCode:");
        stringBuilder.append(getDepotCode());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        long aLong_0 = getId();
        result = 31 * result + (int) (aLong_0 ^ (aLong_0 >>> 32));
        String aString_1 = getFirstName();
        result = 31 * result + (aString_1 != null ? aString_1.hashCode() : 0);
        String aString_2 = getLastName();
        result = 31 * result + (aString_2 != null ? aString_2.hashCode() : 0);
        String aString_3 = getPhone();
        result = 31 * result + (aString_3 != null ? aString_3.hashCode() : 0);
        long aLong_4 = getDialingCode();
        result = 31 * result + (int) (aLong_4 ^ (aLong_4 >>> 32));
        String aString_5 = getUsername();
        result = 31 * result + (aString_5 != null ? aString_5.hashCode() : 0);
        String aString_6 = getEmail();
        result = 31 * result + (aString_6 != null ? aString_6.hashCode() : 0);
        String aString_7 = getFullName();
        result = 31 * result + (aString_7 != null ? aString_7.hashCode() : 0);
        String aString_8 = getAvatarUrl();
        result = 31 * result + (aString_8 != null ? aString_8.hashCode() : 0);
        long aLong_9 = getRole();
        result = 31 * result + (int) (aLong_9 ^ (aLong_9 >>> 32));
        String aString_10 = getRoleName();
        result = 31 * result + (aString_10 != null ? aString_10.hashCode() : 0);
        String aString_11 = getDepotCode();
        result = 31 * result + (aString_11 != null ? aString_11.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRealmProxy aUser = (UserRealmProxy)o;
        if (getId() != aUser.getId()) return false;
        if (getFirstName() != null ? !getFirstName().equals(aUser.getFirstName()) : aUser.getFirstName() != null) return false;
        if (getLastName() != null ? !getLastName().equals(aUser.getLastName()) : aUser.getLastName() != null) return false;
        if (getPhone() != null ? !getPhone().equals(aUser.getPhone()) : aUser.getPhone() != null) return false;
        if (getDialingCode() != aUser.getDialingCode()) return false;
        if (getUsername() != null ? !getUsername().equals(aUser.getUsername()) : aUser.getUsername() != null) return false;
        if (getEmail() != null ? !getEmail().equals(aUser.getEmail()) : aUser.getEmail() != null) return false;
        if (getFullName() != null ? !getFullName().equals(aUser.getFullName()) : aUser.getFullName() != null) return false;
        if (getAvatarUrl() != null ? !getAvatarUrl().equals(aUser.getAvatarUrl()) : aUser.getAvatarUrl() != null) return false;
        if (getRole() != aUser.getRole()) return false;
        if (getRoleName() != null ? !getRoleName().equals(aUser.getRoleName()) : aUser.getRoleName() != null) return false;
        if (getDepotCode() != null ? !getDepotCode().equals(aUser.getDepotCode()) : aUser.getDepotCode() != null) return false;
        return true;
    }

}
