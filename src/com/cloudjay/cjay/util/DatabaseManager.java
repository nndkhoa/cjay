package com.cloudjay.cjay.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.IDatabaseManager;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DatabaseManager implements IDatabaseManager {
	private DatabaseHelper databaseHelper = null;

	@Override
	public DatabaseHelper getHelper(Context context) {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	public SQLiteDatabase getReadableDatabase(Context context) {
		return getHelper(context).getReadableDatabase();
	}

	@Override
	public void releaseHelper(DatabaseHelper helper) {
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}
}
