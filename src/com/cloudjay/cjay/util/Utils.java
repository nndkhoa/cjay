package com.cloudjay.cjay.util;

import com.cloudjay.cjay.service.PhotoUploadService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class Utils {

	public static Animation createScaleAnimation(View view, int parentWidth,
			int parentHeight, int toX, int toY) {
		// Difference in X and Y
		final int diffX = toX - view.getLeft();
		final int diffY = toY - view.getTop();

		// Calculate actual distance using pythagors
		float diffDistance = FloatMath.sqrt((toX * toX) + (toY * toY));
		float parentDistance = FloatMath.sqrt((parentWidth * parentWidth)
				+ (parentHeight * parentHeight));

		ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f,
				Animation.ABSOLUTE, diffX, Animation.ABSOLUTE, diffY);
		scaleAnimation.setFillAfter(true);
		scaleAnimation.setInterpolator(new DecelerateInterpolator());
		scaleAnimation.setDuration(Math.round(diffDistance / parentDistance
				* CJayConstant.SCALE_ANIMATION_DURATION_FULL_DISTANCE));

		return scaleAnimation;
	}

	public static boolean isUploadingPaused(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(CJayConstant.PREF_UPLOADS_PAUSED, false);
	}

	public static void setUploadingPaused(final Context context,
			final boolean paused) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		editor.putBoolean(CJayConstant.PREF_UPLOADS_PAUSED, paused);
		editor.commit();
	}

	public static Intent getUploadAllIntent(Context context) {
		Intent intent = new Intent(context, PhotoUploadService.class);
		intent.setAction(CJayConstant.INTENT_SERVICE_UPLOAD_ALL);
		return intent;
	}
}
