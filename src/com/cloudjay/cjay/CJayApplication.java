package com.cloudjay.cjay;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.R.integer;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.cloudjay.cjay.model.DatabaseManager;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.network.HttpRequestWrapper;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

@ReportsCrashes(formKey = "", formUri = "https://cloudjay-web.appspot.com/acra/", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast)
public class CJayApplication extends Application {

	public static CJayApplication getApplication(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}

	public static void startCJayHomeActivity(Context context) {
		int userRole = ((CJayActivity) context).getCurrentUser().getRole();

		Intent intent = null;
		switch (userRole) {
		case 1: // Giám định
			intent = new Intent(context, AuditorHomeActivity_.class);
			break;

		case 4: // Sửa chữa
			intent = new Intent(context, RepairTeamHomeActivity_.class);
			break;

		case 6: // Cổng
		default:
			intent = new Intent(context, GateHomeActivity_.class);
			break;
		}
		context.startActivity(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				// .displayer(new FadeInBitmapDisplayer(300))
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.memoryCacheSize(41943040).discCacheSize(104857600)
				.memoryCache(new WeakMemoryCache()).threadPoolSize(10)
				.threadPriority(Thread.MAX_PRIORITY).build();

		ACRA.init(CJayApplication.this);
		ACRA.getErrorReporter().checkReportsOnApplicationStart();
		ImageLoader.getInstance().init(config);

		CJayClient.getInstance().init(new HttpRequestWrapper(),
				new DatabaseManager());

		DataCenter.initialize(new DatabaseManager());

		if (!CJayConstant.APP_DIRECTORY_FILE.exists())
			CJayConstant.APP_DIRECTORY_FILE.mkdir();

		if (!CJayConstant.HIDDEN_APP_DIRECTORY_FILE.exists())
			CJayConstant.HIDDEN_APP_DIRECTORY_FILE.mkdir();

		// Configure Logger
		Logger.PRODUCTION_MODE = false;
	}
}
