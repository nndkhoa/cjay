package com.cloudjay.cjay.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DatabaseManager implements IDatabaseManager {
	private DatabaseHelper databaseHelper = null;

	public DatabaseHelper getHelper(Context context) {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(context,
					DatabaseHelper.class);
		}
		return databaseHelper;
	}

	public void releaseHelper(DatabaseHelper helper) {
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	public SQLiteDatabase getReadableDatabase(Context context) {
		return getHelper(context).getReadableDatabase();
	}
}
