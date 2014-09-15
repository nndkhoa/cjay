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
                .addIntegerColumn(User.ID, null)
                .addTextColumn(User.FIRST_NAME, "", false)
                .addTextColumn(User.LAST_NAME, "", false)
                .addTextColumn(User.FULL_NAME, "", false)
                .addTextColumn(User.PHONE, "", false)
                .addTextColumn(User.EMAIL, "", false)
                .addTextColumn(User.ACCESS_TOKEN, "", false)
                .addTextColumn(User.AVATAR_URL, "", false)
                .addIntegerColumn(User.ROLE, null)
                .addTextColumn(User.ROLE_NAME, "", false)
                .addIntegerColumn(User.DEPOT_CODE, null)
                .addIntegerColumn(User.DIALING_CODE, null);

        VTableCreation auditReportItemTable = new VTableCreation(AuditReportItem.TABLE, "id")
                .addIntegerColumn(AuditReportItem.ID, null)
                .addTextColumn(AuditReportItem.TIME_POSTED, "", false)
                .addIntegerColumn(AuditReportItem.DAMAGE_ID, null)
                .addIntegerColumn(AuditReportItem.DAMAGE_CODE, null)
                .addIntegerColumn(AuditReportItem.REPAIR_ID, null)
                .addIntegerColumn(AuditReportItem.REPAIR_CODE, null)
                .addIntegerColumn(AuditReportItem.COMPONENT_ID, null)
                .addIntegerColumn(AuditReportItem.COMPONENT_CODE, null)
                .addTextColumn(AuditReportItem.COMPONENT_NAME, "", false)
                .addTextColumn(AuditReportItem.LOCATION_CODE, "", false)
                .addIntegerColumn(AuditReportItem.LENGTH, null)
                .addIntegerColumn(AuditReportItem.HEIGHT, null)
                .addIntegerColumn(AuditReportItem.QUANTITY, null)
                .addIntegerColumn(AuditReportItem.IS_FIX_ALLOWED, null)
                .addIntegerColumn(AuditReportItem.SESSION_ID, null);

        VTableCreation auditReportImageTable = new VTableCreation(AuditReportImage.TABLE, "id")
                .addIntegerColumn(AuditReportImage.ID, null)
                .addIntegerColumn(AuditReportImage.TYPE, null)
                .addTextColumn(AuditReportImage.IMAGE_NAME, "", false)
                .addTextColumn(AuditReportImage.IMAGE_URL, "", false)
                .addTextColumn(AuditReportImage.CREATED_AT, "", false)
                .addIntegerColumn(AuditReportImage.AUDIT_REPORT_ITEM_ID, null);

        VTableCreation gateReportImage = new VTableCreation(GateReportImage.TABLE, "id")
                .addIntegerColumn(GateReportImage.ID, null)
                .addIntegerColumn(GateReportImage.TYPE, null)
                .addTextColumn(GateReportImage.IMAGE_NAME, "", false)
                .addTextColumn(GateReportImage.IMAGE_URL, "", false)
                .addTextColumn(GateReportImage.CREATED_AT, "", false)
                .addIntegerColumn(GateReportImage.CONTAINER_ID, null);

        VTableCreation isoCodeTable = new VTableCreation(IsoCode.TABLE, "id")
                .addIntegerColumn(IsoCode.ID, null)
                .addIntegerColumn(IsoCode.TYPE, null)
                .addTextColumn(IsoCode.CODE, "", false)
                .addTextColumn(IsoCode.DISPLAY_NAME, "", false);

        VTableCreation operatorTable = new VTableCreation(Operator.TABLE, "id")
                .addIntegerColumn(Operator.ID, null)
                .addTextColumn(Operator.OPERATOR_CODE, "", false)
                .addTextColumn(Operator.OPERATOR_NAME, "", false);

        VTableCreation sessionTable = new VTableCreation(Session.TABLE, "id")
                .addIntegerColumn(Session.ID, null)
                .addIntegerColumn(Session.DEPOT_CODE, null)
                .addIntegerColumn(Session.CONTAINER_ID, null)
                .addTextColumn(Session.IMAGE_ID_PATH, "", false)
                .addTextColumn(Session.CHECK_IN_TIME, "", false)
                .addTextColumn(Session.CHECK_OUT_TIME, "", false)
                .addTextColumn(Session.TIME_MODIFIED, "", false)
                .addTextColumn(Session.TYPE, "", false)
                .addTextColumn(Session.STATUS, "", false)
                .addIntegerColumn(Session.OPERATOR_ID, null)
                .addTextColumn(Session.OPERATOR_CODE, "", false)
                .addTextColumn(Session.OPERATOR_NAME, "", false)
                .addIntegerColumn(Session.UPLOAD_TYPE, null);

        VDatabaseVersion version = new VDatabaseVersion(1)
                .newTable(userTable)
                .newTable(auditReportItemTable)
                .newTable(auditReportImageTable)
                .newTable(gateReportImage)
                .newTable(isoCodeTable)
                .newTable(operatorTable)
                .newTable(sessionTable);

        vDatabaseVersions.add(version);

        /*VDatabaseVersion version2 = new VDatabaseVersion(5)
                .alterTableAddTextColumn(User.TABLE, User.ROLE, "", false);
        vDatabaseVersions.add(version2);*/



        return "cjay_db";
    }
}
