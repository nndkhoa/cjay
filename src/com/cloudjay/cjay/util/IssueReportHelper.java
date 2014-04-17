package com.cloudjay.cjay.util;

import java.util.UUID;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.*;

@EBean(scope = Scope.Singleton)
public class IssueReportHelper {

	public static void setWWContainer(Context ctx, String imageUuid, String containerSessionUuid) {
		long startTime = System.currentTimeMillis();
		Logger.Log("*** Create water wash issue***");

		SQLiteDatabase db = DataCenter.getDatabaseHelper(ctx.getApplicationContext()).getWritableDatabase();

		Cursor damageCursor = db.rawQuery("select id as _id from damage_code where code = ?", new String[] { "DB" });

		int damageId = 0;
		if (damageCursor.moveToFirst()) {
			damageId = damageCursor.getInt(damageCursor.getColumnIndexOrThrow("_id"));
		}

		Cursor repairCursor = db.rawQuery("select id as _id from repair_code where code = ?", new String[] { "WW" });

		int repairId = 0;
		if (repairCursor.moveToFirst()) {
			repairId = repairCursor.getInt(repairCursor.getColumnIndexOrThrow("_id"));
		}

		Cursor componentCursor = db.rawQuery(	"select id as _id from component_code where code = ?",
												new String[] { "FWA" });

		int componentId = 0;
		if (componentCursor.moveToFirst()) {
			componentId = componentCursor.getInt(componentCursor.getColumnIndexOrThrow("_id"));
		}

		String issueId;
		String sql = "select _id from issue where componentCode_id = " + componentId + " and damageCode_id = "
				+ damageId + " and repairCode_id = " + repairId + " and locationCode = ? and containerSession_id = ?";
		Cursor issueCursor = db.rawQuery(sql, new String[] { "BXXX", containerSessionUuid });
		if (issueCursor.moveToFirst()) {
			// a WW issue already exists. Update quantity
			issueId = issueCursor.getString(repairCursor.getColumnIndexOrThrow("_id"));
			sql = "update issue set quantity = quantity + 1 where _id = " + Utils.sqlString(issueId);
			db.execSQL(sql);
		} else {
			// create a new WW issue
			issueId = UUID.randomUUID().toString();
			sql = "insert into issue "
					+ "(componentCode_id, containerSession_id, damageCode_id, _id, height, repairCode_id, length, locationCode, quantity, id, fixed) "
					+ " VALUES " + "(" + componentId + ", '" + containerSessionUuid + "', " + damageId + ", "
					+ Utils.sqlString(issueId) + ", NULL, " + repairId + ", NULL, 'BXXX', 1, 0, 0)";
			db.execSQL(sql);
		}

		// link issue to cjayimage
		sql = "UPDATE cjay_image SET issue_id = '" + issueId + "' WHERE uuid = '" + imageUuid + "'";
		db.execSQL(sql);

		if (ctx.getClass() == AuditorContainerActivity_.class) {
			((AuditorContainerActivity_) ctx).refresh();
		}

		// cost 50ms - Vu: updated cost: ~150-170ms
		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));
	}

	public static void showIssueAssigment(Context ctx, String imageUuid) {

		Intent intent = new Intent(ctx, AuditorIssueAssigmentActivity_.class);
		intent.putExtra(AuditorIssueAssigmentActivity_.CJAY_IMAGE_EXTRA, imageUuid);
		ctx.startActivity(intent);

	}

	public static void showIssueReport(Context ctx, String imageUuid) {

		Intent intent = new Intent(ctx, AuditorIssueReportActivity_.class);
		intent.putExtra(AuditorIssueReportActivity_.CJAY_IMAGE_EXTRA, imageUuid);
		ctx.startActivity(intent);

	}

	public static void
			showReportDialog(final Context ctx, final String cJayImageUuid, final String containerSessionUUID) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx).setMessage(R.string.dialog_report_message)
																	.setTitle(R.string.dialog_report_title)
																	.setPositiveButton(	R.string.dialog_report_no,
																						new DialogInterface.OnClickListener() {
																							@Override
																							public
																									void
																									onClick(DialogInterface dialog,
																											int id) {

																								// Issue not reported,
																								// report issue
																								showIssueReport(ctx,
																												cJayImageUuid);
																							}
																						})
																	.setNegativeButton(	R.string.dialog_report_yes,
																						new DialogInterface.OnClickListener() {
																							@Override
																							public
																									void
																									onClick(DialogInterface dialog,
																											int id) {

																								// The issue already
																								// reported, assign this
																								// image to that issue
																								showIssueAssigment(	ctx,
																													cJayImageUuid);
																							}
																						})
																	.setNeutralButton(R.string.dialog_report_neutral,
																						new OnClickListener() {
																							@Override
																							public
																									void
																									onClick(DialogInterface dialog,
																											int which) {

																								setWWContainer(	ctx,
																												cJayImageUuid,
																												containerSessionUUID);
																							}
																						});
		builder.show();
	}
}
