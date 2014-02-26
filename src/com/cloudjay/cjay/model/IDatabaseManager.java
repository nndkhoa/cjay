package com.cloudjay.cjay.model;

import com.cloudjay.cjay.util.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public interface IDatabaseManager {
	DatabaseHelper getHelper(Context context);

	void releaseHelper(DatabaseHelper helper);

	SQLiteDatabase getReadableDatabase(Context context);
}
