package com.cloudjay.cjay;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by nambv on 2015/03/03.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, foreignKeysSupported = true)
public class AppDatabase {

    public static final String NAME = "cjay";

    public static final int VERSION = 1;

}
