package com.cloudjay.cjay.model;

import android.content.Context;

public interface IDatabaseManager {
	DatabaseHelper getHelper(Context context);

	void releaseHelper(DatabaseHelper helper);
}
