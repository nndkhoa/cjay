package com.cloudjay.cjay.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class QueryHelper {

	public static void update(Context ctx, String table, String field, String value, String whereClause) {
		update(ctx, table, new String[] { field }, new String[] { value }, whereClause);
	}

	public static void update(Context ctx, String table, String[] fields, String[] values, String whereClause) {
		if (fields.length == 0 || values.length == 0 || fields.length != values.length) { return; }
		SQLiteDatabase db = DataCenter.getDatabaseHelper(ctx.getApplicationContext()).getWritableDatabase();
		ContentValues cv = new ContentValues(fields.length);
		for (int i = 0; i < fields.length; i++) {
			cv.put(fields[i], values[i]);
		}
		db.update(table, cv, whereClause, null);
	}

}
