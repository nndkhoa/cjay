package com.cloudjay.cjay.util;

import com.cloudjay.cjay.BuildConfig;

public final class Flags {

	public static final boolean DEBUG = BuildConfig.DEBUG;

	public static final boolean ENABLE_BUG_TRACKING = !DEBUG;

	public static final boolean ENABLE_DB_PERSISTENCE = true;

	public static final boolean USE_INTERNAL_DECODER = false;

}
