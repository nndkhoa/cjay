package com.cloudjay.cjay.model;

import org.coocood.vcontentprovider.VContentProvider;
import org.coocood.vcontentprovider.VDatabaseVersion;
import org.coocood.vcontentprovider.VTableCreation;
import org.coocood.vcontentprovider.VViewCreation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nambv on 12/09/2014.
 */
public class CJayProvider extends VContentProvider {
    @Override
    protected String addDatabaseVersionsViewsAndGetName(ArrayList<VDatabaseVersion> vDatabaseVersions, HashMap<String, VViewCreation> stringVViewCreationHashMap) {


        VTableCreation userTable = new VTableCreation(User.TABLE, "id")
            .addTextColumn(User.EMAIL, "", false)
            .addTextColumn(User.ACCESS_TOKEN, "", false);

        VDatabaseVersion version = new VDatabaseVersion(1)
                .newTable(userTable);

        vDatabaseVersions.add(version);

        VDatabaseVersion version2 = new VDatabaseVersion(5)
                .alterTableAddTextColumn(User.TABLE, User.ROLE, "", false);
        vDatabaseVersions.add(version2);

        VDatabaseVersion version3 = new VDatabaseVersion(10)
                .alterTableAddTextColumn(User.TABLE, User.MOTHER_NAME, "", false);
        vDatabaseVersions.add(version3);

        VDatabaseVersion version4 = new VDatabaseVersion(15)
                .alterTableAddTextColumn(User.TABLE, User.FATHER_NAME, "", false);
        vDatabaseVersions.add(version4);

        return "database";
    }
}
