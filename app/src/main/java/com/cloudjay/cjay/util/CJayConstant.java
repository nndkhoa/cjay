package com.cloudjay.cjay.util;

import android.os.Environment;

import java.io.File;

public class CJayConstant {

    // File path
    public static final String APP_DIRECTORY = "CJay";
    public static final String BACK_UP_DIRECTORY = ".backup";
    public static final String LOG_DIRECTORY = "log";

    // `/sdcard/DCMI/CJay/`
    public static final File APP_DIRECTORY_FILE = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            CJayConstant.APP_DIRECTORY);

}
