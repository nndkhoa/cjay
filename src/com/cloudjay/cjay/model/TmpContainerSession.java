package com.cloudjay.cjay.model;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.util.Logger;

public class TmpContainerSession {

	private static final String TAG = "TmpContainerSession";
	
	public static final int STATE_UPLOAD_COMPLETED = 5;
	public static final int STATE_UPLOAD_ERROR = 4;
	public static final int STATE_UPLOAD_IN_PROGRESS = 3;
	public static final int STATE_UPLOAD_WAITING = 2;
	
	private int id;
	private String container_id;
	private String image_id_path;
	private String operator_code;
	private String check_in_time;
	private String check_out_time;
	private String depot_code;
	private List<AuditReportItem> audit_report_items;
	private List<GateReportImage> gate_report_images;
	private int mState;
	private int mProgress;
	
	public TmpContainerSession() {
		audit_report_items = new ArrayList<AuditReportItem>();
		gate_report_images = new ArrayList<GateReportImage>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContainerId() {
		return container_id;
	}

	public void setContainerId(String container_id) {
		this.container_id = container_id;
	}

	public String getImageIdPath() {
		return image_id_path;
	}

	public void setImageIdPath(String image_id_path) {
		this.image_id_path = image_id_path;
	}

	public String getOperatorCode() {
		return operator_code;
	}

	public void setOperatorCode(String operator_code) {
		this.operator_code = operator_code;
	}

	public String getCheckInTime() {
		return check_in_time;
	}

	public void setCheckInTime(String check_in_time) {
		this.check_in_time = check_in_time;
	}

	public String getCheckOutTime() {
		return check_out_time;
	}

	public void setCheckOutTime(String check_out_time) {
		this.check_out_time = check_out_time;
	}

	public String getDepotCode() {
		return depot_code;
	}

	public void setDepotCode(String depot_code) {
		this.depot_code = depot_code;
	}

	public List<AuditReportItem> getAuditReportItems() {
		return audit_report_items;
	}

	public void setAuditReportItems(List<AuditReportItem> audit_report_items) {
		this.audit_report_items = audit_report_items;
	}

	public List<GateReportImage> getGateReportImages() {
		return gate_report_images;
	}

	public void setGateReportImages(List<GateReportImage> gate_report_images) {
		this.gate_report_images = gate_report_images;
	}
	
	public int getUploadProgress() {
		return mProgress;
	}
	
	public int getUploadState() {
		return mState;
	}

	public void printMe() {
		Logger.Log(TAG, "CId: " + getContainerId() + " - OpCode: " + getOperatorCode()
				+ " - Depot Code: " + getDepotCode() + " - Time In: " + getCheckInTime()
				+ " - Time Out: " + getCheckOutTime());
	}

}


/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package com.cloudjay.cjay.model;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Environment;
//import android.provider.MediaStore.Images.Thumbnails;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.cloudjay.cjay.CJayApplication;
//import com.cloudjay.cjay.R;
//import com.cloudjay.cjay.events.UploadStateChangedEvent;
//import com.cloudjay.cjay.events.UploadsModifiedEvent;
//import com.cloudjay.cjay.util.Flags;
//import com.cloudjay.cjay.util.Utils;
//import com.j256.ormlite.table.DatabaseTable;
//import com.lightbox.android.photoprocessing.PhotoProcessing;
//import com.lightbox.android.photoprocessing.utils.BitmapUtils;
//import com.lightbox.android.photoprocessing.utils.BitmapUtils.BitmapSize;
//
//import de.greenrobot.event.EventBus;
//
//@DatabaseTable(tableName = "photo_upload")
//public class TmpContainerSession {
//
//	private static final HashMap<Uri, TmpContainerSession> SELECTION_CACHE = new HashMap<Uri, TmpContainerSession>();
//
//	public static final int STATE_UPLOAD_COMPLETED = 5;
//	public static final int STATE_UPLOAD_ERROR = 4;
//	public static final int STATE_UPLOAD_IN_PROGRESS = 3;
//	public static final int STATE_UPLOAD_WAITING = 2;
//	public static final int STATE_SELECTED = 1;
//	public static final int STATE_NONE = 0;
//
//	public static final String FIELD_STATE = "state";
//	static final String FIELD_URI = "uri";
//	static final String FIELD_COMPLETED_DETECTION = "tag_detection";
//	static final String FIELD_USER_ROTATION = "user_rotation";
//	static final String FIELD_FILTER = "filter";
//	static final String FIELD_CROP_L = "crop_l";
//	static final String FIELD_CROP_T = "crop_t";
//	static final String FIELD_CROP_R = "crop_r";
//	static final String FIELD_CROP_B = "crop_b";
//	static final String FIELD_ACCOUNT_ID = "acc_id";
//	static final String FIELD_TARGET_ID = "target_id";
//	static final String FIELD_QUALITY = "quality";
//	static final String FIELD_RESULT_POST_ID = "r_post_id";
//	static final String FIELD_CAPTION = "caption";
//	static final String FIELD_TAGS_JSON = "tags";
//	static final String FIELD_PLACE_NAME = "place_name";
//	static final String FIELD_PLACE_ID = "place_id";
//
//	static final String LOG_TAG = "PhotoUpload";
//	static final float CROP_THRESHOLD = 0.01f; // 1%
//	static final int MINI_THUMBNAIL_SIZE = 300;
//	static final int MICRO_THUMBNAIL_SIZE = 96;
//	static final float MIN_CROP_VALUE = 0.0f;
//	static final float MAX_CROP_VALUE = 1.0f;
//
//	public static TmpContainerSession getSelection(Uri uri) {
//		// Check whether we've already got a Selection cached
//		TmpContainerSession item = SELECTION_CACHE.get(uri);
//
//		if (null == item) {
//			item = new TmpContainerSession(uri);
//			SELECTION_CACHE.put(uri, item);
//		}
//
//		return item;
//	}
//
//	public static void clearCache() {
//		SELECTION_CACHE.clear();
//	}
//
//	public static void populateCache(List<TmpContainerSession> uploads) {
//		for (TmpContainerSession upload : uploads) {
//			SELECTION_CACHE.put(upload.getOriginalPhotoUri(), upload);
//		}
//	}
//
//	public static TmpContainerSession getSelection(Uri baseUri, long id) {
//		return getSelection(Uri.withAppendedPath(baseUri, String.valueOf(id)));
//	}
//	
//	/**
//	 * Uri and Database Key
//	 */
//	private Uri mFullUri;
//	private String mFullUriString;
//
//	/**
//	 * Edit Variables
//	 */
//	private UploadQuality mQuality;
//	private int mState;
//
//	private int mProgress;
//	private Bitmap mBigPictureNotificationBmp;
//
//	private boolean mNeedsSaveFlag = false;
//
//	TmpContainerSession() {
//		// NO-Arg for Ormlite
//	}
//
//	private TmpContainerSession(Uri uri) {
//		mFullUri = uri;
//		mFullUriString = uri.toString();
//		reset();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof TmpContainerSession) {
//			return getOriginalPhotoUri().equals(
//					((TmpContainerSession) obj).getOriginalPhotoUri());
//		}
//		return false;
//	}
//
//	public boolean isValid(Context context) {
//		final String path = Utils.getPathFromContentUri(
//				context.getContentResolver(), getOriginalPhotoUri());
//		if (null != path) {
//			File file = new File(path);
//			return file.exists();
//		}
//		return false;
//	}
//
//	public Bitmap getBigPictureNotificationBmp() {
//		return mBigPictureNotificationBmp;
//	}
//
//	public Bitmap getDisplayImage(Context context) {
//		try {
//			final int size = CJayApplication.getApplication(context)
//					.getSmallestScreenDimension();
//			Bitmap bitmap = Utils.decodeImage(context.getContentResolver(),
//					getOriginalPhotoUri(), size);
//			return bitmap;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public String getDisplayImageKey() {
//		return "dsply_" + getOriginalPhotoUri();
//	}
//
//	public Uri getOriginalPhotoUri() {
//		if (null == mFullUri && !TextUtils.isEmpty(mFullUriString)) {
//			mFullUri = Uri.parse(mFullUriString);
//		}
//		return mFullUri;
//	}
//
//	public Bitmap getThumbnailImage(Context context) {
//		if (ContentResolver.SCHEME_CONTENT.equals(getOriginalPhotoUri()
//				.getScheme())) {
//			return getThumbnailImageFromMediaStore(context);
//		}
//
//		int size = MICRO_THUMBNAIL_SIZE;
//
//		try {
//			Bitmap bitmap = Utils.decodeImage(context.getContentResolver(),
//					getOriginalPhotoUri(), size);
//			return bitmap;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public String getThumbnailImageKey() {
//		return "thumb_" + getOriginalPhotoUri();
//	}
//
//	public Bitmap getUploadImage(Context context, final UploadQuality quality) {
//		return getUploadImageNative(context, quality);
//	}
//
//	public int getUploadProgress() {
//		return mProgress;
//	}
//
//	public int getUploadState() {
//		return mState;
//	}
//
//	public File getUploadSaveFile() {
//		File dir = new File(
//				Environment
//						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//				"photup");
//		if (!dir.exists()) {
//			dir.mkdirs();
//		}
//
//		return new File(dir, System.currentTimeMillis() + ".jpg");
//	}
//
//	public UploadQuality getUploadQuality() {
//		return null != mQuality ? mQuality : UploadQuality.MEDIUM;
//	}
//
//	@Override
//	public int hashCode() {
//		return getOriginalPhotoUri().hashCode();
//	}
//
//	public boolean requiresSaving() {
//		return mNeedsSaveFlag;
//	}
//
//	public void reset() {
//		mState = STATE_NONE;
//		setRequiresSaveFlag();
//	}
//
//	public void resetSaveFlag() {
//		mNeedsSaveFlag = false;
//	}
//
//	public void setBigPictureNotificationBmp(Context context,
//			Bitmap bigPictureNotificationBmp) {
//		if (null == bigPictureNotificationBmp) {
//			mBigPictureNotificationBmp = BitmapFactory.decodeResource(
//					context.getResources(), R.drawable.ic_logo);
//		} else {
//			mBigPictureNotificationBmp = bigPictureNotificationBmp;
//		}
//	}
//	
//	public void setUploadParams(UploadQuality quality) {
//		mQuality = quality;
//		setRequiresSaveFlag();
//	}
//
//	public void setUploadProgress(int progress) {
//		if (progress != mProgress) {
//			mProgress = progress;
//			notifyUploadStateListener();
//		}
//	}
//
//	public void setUploadState(final int state) {
//		if (mState != state) {
//			mState = state;
//
//			switch (state) {
//			case STATE_UPLOAD_ERROR:
//			case STATE_UPLOAD_COMPLETED:
//				mBigPictureNotificationBmp = null;
//				EventBus.getDefault().post(new UploadsModifiedEvent());
//				break;
//			case STATE_SELECTED:
//			case STATE_UPLOAD_WAITING:
//				mProgress = -1;
//				break;
//			}
//
//			notifyUploadStateListener();
//			setRequiresSaveFlag();
//		}
//	}
//
//	@Override
//	public String toString() {
//		return "";
//	}
//
//	private Bitmap getThumbnailImageFromMediaStore(Context context) {
//		final int kind = Thumbnails.MICRO_KIND;
//
//		BitmapFactory.Options opts = null;
//
//		try {
//			final long id = Long.parseLong(getOriginalPhotoUri()
//					.getLastPathSegment());
//
//			Bitmap bitmap = Thumbnails.getThumbnail(
//					context.getContentResolver(), id, kind, opts);
//			return bitmap;
//		} catch (Exception e) {
//			if (Flags.DEBUG) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//	}
//
//	private Bitmap getUploadImageNative(final Context context,
//			final UploadQuality quality) {
//		try {
//			String path = Utils.getPathFromContentUri(
//					context.getContentResolver(), getOriginalPhotoUri());
//			if (null != path) {
//				BitmapSize size = BitmapUtils.getBitmapSize(path);
//
//				if (quality.requiresResizing()) {
//					final float resizeRatio = Math.max(size.width, size.height)
//							/ (float) quality.getMaxDimension();
//					size = new BitmapSize(Math.round(size.width / resizeRatio),
//							Math.round(size.height / resizeRatio));
//				}
//
//				boolean doAndroidDecode = true;
//
//				if (Flags.USE_INTERNAL_DECODER) {
//					doAndroidDecode = PhotoProcessing.nativeLoadResizedBitmap(
//							path, size.width * size.height) != 0;
//
//					if (Flags.DEBUG) {
//						if (doAndroidDecode) {
//							Log.d("MediaStorePhotoUpload",
//									"getUploadImage. Native decode failed :(");
//						} else {
//							Log.d("MediaStorePhotoUpload",
//									"getUploadImage. Native decode complete!");
//						}
//					}
//				}
//
//				if (doAndroidDecode) {
//					if (Flags.DEBUG) {
//						Log.d("MediaStorePhotoUpload",
//								"getUploadImage. Doing Android decode");
//					}
//
//					// Just in case
//					PhotoProcessing.nativeDeleteBitmap();
//
//					// Decode in Android and send to native
//					Bitmap bitmap = Utils.decodeImage(
//							context.getContentResolver(),
//							getOriginalPhotoUri(), quality.getMaxDimension());
//
//					if (null != bitmap) {
//						PhotoProcessing.sendBitmapToNative(bitmap);
//						bitmap.recycle();
//
//						// Resize image to correct size
//						PhotoProcessing.nativeResizeBitmap(size.width,
//								size.height);
//					} else {
//						return null;
//					}
//				}
//
//				if (Flags.DEBUG) {
//					Log.d("MediaStorePhotoUpload",
//							"getUploadImage. Native worked!");
//				}
//
//				return PhotoProcessing.getBitmapFromNative(null);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			// Just in case...
//			PhotoProcessing.nativeDeleteBitmap();
//		}
//
//		return null;
//	}
//
//	private void notifyUploadStateListener() {
//		EventBus.getDefault().post(new UploadStateChangedEvent(this));
//	}
//
//	private void setRequiresSaveFlag() {
//		mNeedsSaveFlag = true;
//	}
//}
