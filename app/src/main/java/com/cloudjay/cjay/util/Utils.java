package com.cloudjay.cjay.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.cloudjay.cjay.event.ParsedSessionEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.Role;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.realm.Realm;
import io.realm.RealmResults;

public class Utils {
    public static String getAppVersionName(Context ctx) {

        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }

    public static void showCrouton(Activity context, int textResId) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, textResId, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    public static void showCrouton(Activity context, String message) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, message, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    public static int getRole(Context context) {

        return Integer.valueOf(PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_USER_ROLE));
    }

    //Check containerID is valid or not
    public static boolean isContainerIdValid(String containerId) {

        //if (!Logger.isDebuggable()) {
            Logger.Log("isContainerIdValid");

            int crc = ContCheckDigit.getCRC(containerId);
            if (crc == 10) {
                crc = 0;
            }

            char lastChar = containerId.charAt(containerId.length() - 1);
            if (Character.getNumericValue(lastChar) == crc) {
                return true;
            } else {
                return false;
            }
        //}

        //return true;
    }

    public static boolean simpleValid(String containerID) {

        Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
        Matcher matcher = pattern.matcher(containerID);
        if (!matcher.matches()) return false;
        return true;
    }

    public static String replaceNullBySpace(String in) {
        return in == null || in.equals("") ? " " : in;
    }

    /**
     * Convert container session json to Session Object.
     * Need to check if container is existed or not. (should use insert or update concept)
     *
     * @param context
     * @param e
     * @return
     */
    public static Session parseSession(Context context, JsonObject e) {

        //Check available session
        String containerId = e.get("container_id").toString();
        Realm realm = Realm.getInstance(context);
        RealmResults<Session> sessions = realm.where(Session.class).equalTo("containerId", containerId).findAll();
        //If hasn't -> create
        if (sessions.isEmpty()) {
            Session session = parseNewSession(context, e);
            return session;
        }
        //else -> update
        else {
            realm.beginTransaction();
            sessions.clear();
            Session session = parseNewSession(context, e);
            realm.commitTransaction();
            return session;
        }


    }

    private static Session parseNewSession(Context context, JsonObject e) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Session session = realm.createObject(Session.class);
        session.setId(Long.parseLong(e.get("id").toString()));
        session.setContainerId(e.get("container_id").toString());
        session.setCheckInTime(e.get("check_in_time").toString());
        if (e.get("check_out_time").isJsonNull()) {
            session.setCheckOutTime("");
        } else {
            session.setCheckOutTime(e.get("check_out_time").toString());
        }
        session.setDepotCode(e.get("depot_code").toString());
        session.setDepotId(Long.parseLong(e.get("depot_id").toString()));
        session.setOperatorCode(e.get("operator_code").toString());
        session.setOperatorId(Long.parseLong(e.get("operator_id").toString()));
        session.setPreStatus(Long.parseLong(e.get("pre_status").toString()));
        session.setStatus(Long.parseLong(e.get("status").toString()));
        session.setStep(Long.parseLong(e.get("step").toString()));
        realm.commitTransaction();

        realm.beginTransaction();
        // Process list audit items
        JsonArray auditItems = e.getAsJsonArray("audit_items");
        for (JsonElement audit : auditItems) {
            AuditItem item = realm.createObject(AuditItem.class);

            item.setComponentCode(audit.getAsJsonObject().get("component_code").toString());
            item.setComponentCodeId(Long.parseLong(audit.getAsJsonObject().get("component_code_id").toString()));
            item.setComponentName(audit.getAsJsonObject().get("component_name").toString());
            item.setCreatedAt(audit.getAsJsonObject().get("created_at").toString());
            item.setDamageCode(audit.getAsJsonObject().get("damage_code").toString());
            item.setDamageCodeId(Long.parseLong(audit.getAsJsonObject().get("damage_code_id").toString()));

            if (audit.getAsJsonObject().get("height").isJsonNull()) {
                item.setHeight(0);
            } else {
                item.setHeight(Double.valueOf(audit.getAsJsonObject().get("height").toString()));
            }

            if (audit.getAsJsonObject().get("length").isJsonNull()) {
                item.setHeight(0);
            } else {
                item.setHeight(Double.valueOf(audit.getAsJsonObject().get("length").toString()));
            }

            item.setId(Long.parseLong(audit.getAsJsonObject().get("id").toString()));
            item.setIsAllowed(Boolean.parseBoolean(audit.getAsJsonObject().get("is_allowed").toString()));
            item.setLocationCode(audit.getAsJsonObject().get("location_code").toString());
            item.setModifiedAt(audit.getAsJsonObject().get("modified_at").toString());
            if (audit.getAsJsonObject().get("quantity").isJsonNull()) {
                item.setQuantity(0);
            } else {
                item.setQuantity(Long.valueOf(audit.getAsJsonObject().get("quantity").toString()));
            }

            item.setRepairCode(audit.getAsJsonObject().get("repair_code").toString());
            item.setRepairCodeId(Long.parseLong(audit.getAsJsonObject().get("repair_code_id").toString()));

            // // Process list audit images
            JsonArray auditImage = audit.getAsJsonObject().getAsJsonArray("audit_images");
            for (JsonElement imageAudit : auditImage) {
                AuditImage imageAuditItem = realm.createObject(AuditImage.class);

                imageAuditItem.setId(Long.parseLong(imageAudit.getAsJsonObject().get("id").toString()));
                imageAuditItem.setType(Long.parseLong(imageAudit.getAsJsonObject().get("type").toString()));
                imageAuditItem.setUrl(imageAudit.getAsJsonObject().get("url").toString());
                item.getAuditImages().add(imageAuditItem);
            }

            session.getAuditItems().add(item);
        }
        realm.commitTransaction();

        // Process list gate images
        realm.beginTransaction();
        JsonArray gateImage = e.getAsJsonArray("gate_images");
        for (JsonElement image : gateImage) {

            GateImage imageItem = realm.createObject(GateImage.class);
            imageItem.setId(Long.parseLong(image.getAsJsonObject().get("id").toString()));
            imageItem.setType(Long.parseLong(image.getAsJsonObject().get("type").toString()));
            imageItem.setUrl(image.getAsJsonObject().get("url").toString());

            // Add reference
            session.getGateImages().add(imageItem);
        }
        realm.commitTransaction();
        return session;
    }
}
