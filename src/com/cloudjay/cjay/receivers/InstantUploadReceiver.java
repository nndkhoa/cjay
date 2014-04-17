package com.cloudjay.cjay.receivers;

import org.androidannotations.annotations.EReceiver;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.service.PhotoUploadService_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@EReceiver
public class InstantUploadReceiver extends BroadcastReceiver {

	static final String KEY_LAST_UPLOADED = "last_uploaded_uri";
	static final String LOG_TAG = "InstantUploadReceiver";

	private Context mContext;

	@Override
	public void onReceive(Context ctx, Intent intent) {

		Logger.Log("onReceive");
		mContext = ctx;

		if (intent.getAction().equals(CJayConstant.INTENT_PHOTO_TAKEN)) {
			//
			if (Utils.isRunning(mContext, PhotoUploadService_.class.getName())) {

				Logger.Log("Start PhotoUpload service");
				ctx.startService(Utils.getUploadAllIntent(ctx));

			} else {

				Logger.w("Photo Upload service is already running");
			}
		}

	}

	boolean canStartUpload() {
		return NetworkHelper.isConnected(mContext);
	}
}
