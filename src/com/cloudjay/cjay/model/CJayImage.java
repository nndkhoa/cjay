package com.cloudjay.cjay.model;

import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.events.CJayImageUploadStateChangedEvent;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.UploadState;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@DatabaseTable(tableName = "cjay_image", daoClass = CJayImageDaoImpl.class)
public class CJayImage {

	public static final int STATE_UPLOAD_COMPLETED = 4;
	public static final int STATE_UPLOAD_ERROR = 3;
	public static final int STATE_UPLOAD_IN_PROGRESS = 2;
	public static final int STATE_UPLOAD_WAITING = 1;
	public static final int STATE_NONE = 0;

	public static final int TYPE_IMPORT = 0;
	public static final int TYPE_EXPORT = 1;
	public static final int TYPE_REPORT = 2;
	public static final int TYPE_REPAIRED = 3;

	public static final String ID = "id";
	public static final String FIELD_IMAGE_NAME = "image_name";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_TIME_POSTED = "time_posted";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_URI = "_id";
	public static final String FIELD_UUID = "uuid";

	@DatabaseField(columnName = ID, defaultValue = "0")
	private int id;

	@DatabaseField(columnName = FIELD_IMAGE_NAME)
	String image_name;

	@DatabaseField(columnName = FIELD_TIME_POSTED)
	String time_posted;

	@DatabaseField(columnName = FIELD_UUID)
	String uuid;

	/**
	 * TYPE include: in | out | issue | repaired
	 */
	@DatabaseField(columnName = FIELD_TYPE)
	int type;

	@DatabaseField(columnName = FIELD_STATE, index = true, defaultValue = "4")
	int mState;

	@DatabaseField(columnName = FIELD_URI, id = true)
	String mUri;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	ContainerSession containerSession;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
	Issue issue;

	public CJayImage() {

	}

	public CJayImage(int id, int type, String image_name) {
		this.id = id;
		this.type = type;
		this.image_name = image_name;
		uuid = UUID.randomUUID().toString();
		time_posted = "";
		mUri = image_name;
	}

	public CJayImage(int id, int type, String created_at, String image_name) {
		this.id = id;
		this.type = type;
		this.image_name = image_name;
		time_posted = created_at;
		uuid = UUID.randomUUID().toString();
		mUri = image_name;
	}

	public ContainerSession getContainerSession() {
		return containerSession;
	}

	public int getId() {
		return id;
	}

	public String getImageName() {
		return image_name;
	}

	public Issue getIssue() {
		return issue;
	}

	public String getIssueComponentCode() {
		if (issue != null) return issue.getComponentCodeString();
		return null;
	}

	public String getIssueDamageCode() {
		if (issue != null) return issue.getDamageCodeString();
		return null;
	}

	public String getIssueHeight() {
		if (issue != null) return String.valueOf(issue.getHeight());
		return null;
	}

	public String getIssueLength() {
		if (issue != null) return String.valueOf(issue.getLength());
		return null;
	}

	public String getIssueLocationCode() {
		if (issue != null) return issue.getLocationCode();
		return null;
	}

	public String getIssueQuantity() {
		if (issue != null) return String.valueOf(issue.getQuantity());
		return null;
	}

	public String getIssueRepairCode() {
		if (issue != null) return issue.getRepairCodeString();
		return null;
	}

	public String getTimePosted() {
		return time_posted;
	}

	public int getType() {
		return type;
	}

	public int getUploadState() {
		return mState;
	}

	public String getUri() {
		return mUri;
	}

	public String getUuid() {
		return uuid;
	}

	public void setContainerSession(ContainerSession containerSession) {
		this.containerSession = containerSession;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public void setTimePosted(String time_posted) {
		this.time_posted = time_posted;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setUploadState(int state) {

		if (mState != state) {

			Logger.Log("Set CJayImage upload state from " + UploadState.values()[mState] + " to "
					+ UploadState.values()[state]);
			mState = state;

			switch (state) {
				case STATE_UPLOAD_ERROR:
				case STATE_UPLOAD_COMPLETED:
					mBigPictureNotificationBmp = null;
					break;

				case STATE_UPLOAD_WAITING:
					mProgress = -1;
					break;
			}

			notifyUploadStateListener();
		}
	}

	private void notifyUploadStateListener() {
		EventBus.getDefault().post(new CJayImageUploadStateChangedEvent(this));
	}

	public void setUri(String uri) {
		mUri = uri;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Bitmap getBigPictureNotificationBmp() {
		return mBigPictureNotificationBmp;
	}

	public void setBigPictureNotificationBmp(Context context, Bitmap bigPictureNotificationBmp) {
		if (null == bigPictureNotificationBmp) {
			mBigPictureNotificationBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo);
		} else {
			mBigPictureNotificationBmp = bigPictureNotificationBmp;
		}
	}

	private Bitmap mBigPictureNotificationBmp;

	public Bitmap getThumbnailImage(Context context) {

		return ImageLoader.getInstance().loadImageSync(mUri);

		// if (ContentResolver.SCHEME_CONTENT.equals(getOriginalPhotoUri().getScheme())) { return
		// getThumbnailImageFromMediaStore(context); }
		//
		// final Resources res = context.getResources();
		// int size = res.getBoolean(R.bool.load_mini_thumbnails) ? MINI_THUMBNAIL_SIZE : MICRO_THUMBNAIL_SIZE;
		// if (size == MINI_THUMBNAIL_SIZE && res.getBoolean(R.bool.sample_mini_thumbnails)) {
		// size /= 2;
		// }
		//
		// try {
		// Bitmap bitmap = Utils.decodeImage(context.getContentResolver(), getOriginalPhotoUri(), size);
		// bitmap = Utils.rotate(bitmap, getExifRotation(context));
		// return bitmap;
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// return null;
		// }
	}

	public Bitmap processBitmap(Bitmap bitmap, final boolean fullSize, final boolean modifyOriginal) {
		return bitmap;
		// if (requiresProcessing(fullSize)) {
		// return processBitmapUsingFilter(bitmap, mFilter, fullSize, modifyOriginal);
		// } else {
		// return bitmap;
		// }
	}

	private int mProgress;

	public int getUploadProgress() {
		return mProgress;
	}

	public void setUploadProgress(int progress) {
		if (progress != mProgress) {
			mProgress = progress;
			notifyUploadStateListener();
		}
	}
}
