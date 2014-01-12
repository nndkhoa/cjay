package com.cloudjay.cjay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.ConnectionUtils;
import com.cloudjay.cjay.util.Flags;
import com.cloudjay.cjay.util.Utils;

public class InstantUploadReceiver extends BroadcastReceiver {

	static final String KEY_LAST_UPLOADED = "last_uploaded_uri";
	static final String LOG_TAG = "InstantUploadReceiver";

	private Context mContext;
	private SharedPreferences mPreferences;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		
		mContext = ctx;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

		if (!mPreferences.getBoolean(CJayConstant.PREF_INSTANT_UPLOAD_ENABLED,
				false)) {
			// Instant Upload is disabled, fail fast
			return;
		}

		boolean intentHandled;
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			intentHandled = handleConnectivityIntent(intent);
		} else {
			intentHandled = handlePhotoIntent(intent);
		}

		if (intentHandled && canStartUpload()) {
			if (Flags.DEBUG) {
				Log.d(LOG_TAG, "Starting Service for Instant Upload.");
			}
			ctx.startService(Utils.getUploadIntent(ctx));
		}
	}

	boolean handleConnectivityIntent(Intent intent) {
		return false;
		// PhotoUploadController controller = PhotoUploadController
		// .getFromContext(mContext);
		// return controller.hasWaitingUploads();
	}

	boolean handlePhotoIntent(Intent intent) {

		// TODO: handlePhotoIntent
		// Uri uri = intent.getData();
		// Account account = Account.getAccountFromSession(mContext);
		//
		// if (null != account && null != uri) {
		// if (Flags.DEBUG) {
		// Log.d(LOG_TAG, "Got Photo with URI: " + uri.toString());
		// }
		//
		// final String albumId = mPreferences.getString(
		// PreferenceConstants.PREF_INSTANT_UPLOAD_ALBUM_ID, null);
		// if (TextUtils.isEmpty(albumId)) {
		// if (Flags.DEBUG) {
		// Log.d(LOG_TAG, "No album set!!!");
		// }
		// return false;
		// }
		//
		// final PhotoUpload upload = PhotoUpload.getSelection(uri);
		// final String qualityId = mPreferences.getString(
		// PreferenceConstants.PREF_INSTANT_UPLOAD_QUALITY, null);
		// final String filterId = mPreferences.getString(
		// PreferenceConstants.PREF_INSTANT_UPLOAD_FILTER, "1");
		//
		// upload.setUploadParams(account, albumId,
		// UploadQuality.mapFromPreference(qualityId));
		// upload.setFilterUsed(Filter.mapFromPref(filterId));
		//
		// PhotoUploadController controller = PhotoUploadController
		// .getFromContext(mContext);
		//
		// if (Flags.DEBUG) {
		// Log.d(LOG_TAG, "Adding Upload for URI: " + uri.toString());
		// }
		// return controller.addUpload(upload);
		// }

		return false;
	}

	boolean canStartUpload() {
		ConnectivityManager mgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo info = mgr.getActiveNetworkInfo();

		// If we're not connected, fail fast
		if (!info.isConnectedOrConnecting()) {
			return false;
		}

		final boolean uploadWhileRoaming = mPreferences.getBoolean(
				CJayConstant.PREF_INSTANT_UPLOAD_IF_ROAMING, false);
		if (!uploadWhileRoaming && ConnectionUtils.isRoaming(mContext)) {
			return false;
		}

		final boolean uploadOnWifiOnly = mPreferences.getBoolean(
				CJayConstant.PREF_INSTANT_UPLOAD_WIFI_ONLY, false);
		if (uploadOnWifiOnly && info.getType() != ConnectivityManager.TYPE_WIFI) {
			return false;
		}

		return true;
	}
}
