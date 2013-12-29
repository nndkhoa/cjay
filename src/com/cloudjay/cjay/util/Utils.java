package com.cloudjay.cjay.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.service.UploadIntentService;
import com.lightbox.android.photoprocessing.PhotoProcessing;
import com.lightbox.android.photoprocessing.utils.MediaUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

public class Utils {

	public static void isStillRunning(Context ctx, String packageName) {

		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager
				.getRunningAppProcesses();
		for (int i = 0; i < procInfos.size(); i++) {
			if (procInfos.get(i).processName.equals(packageName)) {
				Toast.makeText(ctx, packageName + "is running",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public final static <T extends Parcelable> void parcelCollection(
			final Parcel out, final Collection<T> collection) {
		if (collection != null) {
			out.writeInt(collection.size());
			out.writeTypedList(new ArrayList<T>(collection));
		} else {
			out.writeInt(-1);
		}
	}

	public final static <T extends Parcelable> Collection<T> unparcelCollection(
			final Parcel in, final Creator<T> creator) {
		final int size = in.readInt();

		if (size >= 0) {
			final List<T> list = new ArrayList<T>(size);
			in.readTypedList(list, creator);
			return list;
		} else {
			return null;
		}
	}

	public static int getOrientationFromContentUri(ContentResolver cr,
			Uri contentUri) {
		int returnValue = 0;

		if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
			// can post image
			String[] proj = { MediaStore.Images.Media.ORIENTATION };
			Cursor cursor = cr.query(contentUri, proj, null, null, null);

			if (null != cursor) {
				if (cursor.moveToFirst()) {
					returnValue = cursor
							.getInt(cursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
				}
				cursor.close();
			}
		} else if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
			returnValue = MediaUtils.getExifOrientation(contentUri.getPath());
		}

		return returnValue;
	}

	public static Animation createScaleAnimation(View view, int parentWidth,
			int parentHeight, int toX, int toY) {
		// Difference in X and Y
		final int diffX = toX - view.getLeft();
		final int diffY = toY - view.getTop();

		// Calculate actual distance using pythagors
		float diffDistance = (float) Math.sqrt((toX * toX) + (toY * toY));
		float parentDistance = (float) Math.sqrt((parentWidth * parentWidth)
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

	public static Intent getUploadIntent(Context context) {
		Intent intent = new Intent(context, UploadIntentService.class);
		intent.setAction(CJayConstant.INTENT_SERVICE_UPLOAD_ALL);
		return intent;
	}

	public static Bitmap decodeImage(final ContentResolver resolver,
			final Uri uri, final int MAX_DIM) throws FileNotFoundException {

		// Get original dimensions
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
		} catch (SecurityException se) {
			se.printStackTrace();
			return null;
		}

		final int origWidth = o.outWidth;
		final int origHeight = o.outHeight;

		// Holds returned bitmap
		Bitmap bitmap;

		o.inJustDecodeBounds = false;
		o.inScaled = false;
		o.inPurgeable = true;
		o.inInputShareable = true;
		o.inDither = true;
		o.inPreferredConfig = Bitmap.Config.RGB_565;

		if (origWidth > MAX_DIM || origHeight > MAX_DIM) {
			int k = 1;
			int tmpHeight = origHeight, tmpWidth = origWidth;
			while ((tmpWidth / 2) >= MAX_DIM || (tmpHeight / 2) >= MAX_DIM) {
				tmpWidth /= 2;
				tmpHeight /= 2;
				k *= 2;
			}
			o.inSampleSize = k;

			bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri),
					null, o);
		} else {
			bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri),
					null, o);
		}

		if (null != bitmap) {
			if (Flags.DEBUG) {
				Log.d("Utils", "Resized bitmap to: " + bitmap.getWidth() + "x"
						+ bitmap.getHeight());
			}
		}

		return bitmap;
	}

	// And to convert the image URI to the direct file system path of the image
	// file
	public static String getPathFromContentUri(ContentResolver cr,
			Uri contentUri) {
		if (Flags.DEBUG) {
			Log.d("Utils", "Getting file path for Uri: " + contentUri);
		}

		String returnValue = null;

		if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
			// can post image
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = cr.query(contentUri, proj, null, null, null);

			if (null != cursor) {
				if (cursor.moveToFirst()) {
					returnValue = cursor
							.getString(cursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				}
				cursor.close();
			}
		} else if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
			returnValue = contentUri.getPath();
		}

		return returnValue;
	}

	public static Bitmap rotate(Bitmap original, final int angle) {
		if ((angle % 360) == 0) {
			return original;
		}

		final boolean dimensionsChanged = angle == 90 || angle == 270;
		final int oldWidth = original.getWidth();
		final int oldHeight = original.getHeight();
		final int newWidth = dimensionsChanged ? oldHeight : oldWidth;
		final int newHeight = dimensionsChanged ? oldWidth : oldHeight;

		Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight,
				original.getConfig());
		Canvas canvas = new Canvas(bitmap);

		Matrix matrix = new Matrix();
		matrix.preTranslate((newWidth - oldWidth) / 2f,
				(newHeight - oldHeight) / 2f);
		matrix.postRotate(angle, bitmap.getWidth() / 2f, bitmap.getHeight() / 2);
		canvas.drawBitmap(original, matrix, null);

		original.recycle();

		return bitmap;
	}

	public static Bitmap fineResizePhoto(final Bitmap bitmap,
			final int maxDimension) {
		Utils.checkPhotoProcessingThread();

		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int biggestDimension = Math.max(width, height);

		if (biggestDimension <= maxDimension) {
			return bitmap;
		}

		final float ratio = maxDimension / (float) biggestDimension;
		Bitmap resized = PhotoProcessing.resize(bitmap,
				Math.round(width * ratio), Math.round(height * ratio));
		if (Flags.DEBUG) {
			Log.d("PhotoUpload", "Finely resized to: " + resized.getWidth()
					+ "x" + resized.getHeight());
		}

		return resized;
	}

	public static void checkPhotoProcessingThread() {
		if (!CJayApplication.THREAD_FILTERS.equals(Thread.currentThread()
				.getName())) {
			throw new IllegalStateException(
					"PhotoProcessing should be done on corrent thread!");
		}
	}
}
