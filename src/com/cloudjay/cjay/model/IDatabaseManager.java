package com.cloudjay.cjay.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.util.DatabaseHelper;

public interface IDatabaseManager {
	DatabaseHelper getHelper(Context context);

	SQLiteDatabase getReadableDatabase(Context context);

	void releaseHelper(DatabaseHelper helper);
}
