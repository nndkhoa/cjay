package com.cloudjay.cjay.adapter;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.CheckableImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TemporaryContainerCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {

		public TextView containerIdView;
		public TextView containerOwnerView;
		public TextView importDateView;
		public ImageView itemPictureView;
		public ImageView validationImageView;
		public CheckableImageView checkableImageView;

	}

	private int mLayout;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private boolean mAvCheckable;
	private HashMap<String, Boolean> mCheckedMap;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public TemporaryContainerCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public TemporaryContainerCursorAdapter(Context context, int layout, Cursor c, int flags) {
		this(context, layout, c, flags, false);
	}

	public TemporaryContainerCursorAdapter(Context context, int layout, Cursor c, int flags, boolean avCheckable) {
		super(context, c, flags);
		this.mLayout = layout;
		mInflater = LayoutInflater.from(context);
		mCursor = c;
		mImageLoader = ImageLoader.getInstance();
		mAvCheckable = avCheckable;
		mCheckedMap = new HashMap<String, Boolean>();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (cursor == null) {
			Logger.Log("-----> BUG");
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			Logger.Log("Holder inside bindView is NULL");

			holder = new ViewHolder();
			holder.containerIdView = (TextView) view.findViewById(R.id.feed_item_container_id);
			holder.containerOwnerView = (TextView) view.findViewById(R.id.feed_item_container_owner);
			holder.importDateView = (TextView) view.findViewById(R.id.feed_item_container_import_date);
			holder.itemPictureView = (ImageView) view.findViewById(R.id.feed_item_picture);
			holder.validationImageView = (ImageView) view.findViewById(R.id.feed_item_validator);
			holder.checkableImageView = (CheckableImageView) view.findViewById(R.id.check_button);
			view.setTag(holder);
		}

		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		holder.containerIdView.setText(containerId);

		// get data from cursor and bind to holder
		String importDate = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_CHECK_IN_TIME));
		importDate = StringHelper.getRelativeDate(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, importDate);
		holder.importDateView.setText(importDate);

		// Logger.Log(containerId + " state: " + state.name());

		String operator = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_NAME));
		holder.containerOwnerView.setText(operator);

		String url = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));
		if (!TextUtils.isEmpty(url) && !url.equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {
			mImageLoader.displayImage(url, holder.itemPictureView);
		} else {
			holder.itemPictureView.setImageResource(R.drawable.ic_app);
		}

		boolean isValidForUpload = false;
		if (cursor.getColumnIndex("export_image_count") >= 0) {
			isValidForUpload = cursor.getInt(cursor.getColumnIndexOrThrow("export_image_count")) > 0;
		} else if (cursor.getColumnIndex("import_image_count") >= 0) {
			isValidForUpload = cursor.getInt(cursor.getColumnIndexOrThrow("import_image_count")) > 0;
		}
		if (isValidForUpload) {
			holder.validationImageView.setVisibility(View.VISIBLE);
		} else {
			holder.validationImageView.setVisibility(View.INVISIBLE);
		}

		if (holder.checkableImageView != null) {

			if (mAvCheckable && cursor.getColumnIndex(ContainerSession.FIELD_AV) > 0) {

				final Context ctx = context;
				final String uuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));
				boolean available;
				if (mCheckedMap.containsKey(uuid)) {
					available = mCheckedMap.get(uuid).booleanValue();
				} else {
					available = cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_AV)) == 1 ? true : false;
				}
				holder.checkableImageView.setVisibility(View.VISIBLE);
				holder.checkableImageView.setChecked(available == true);
				holder.checkableImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						CheckableImageView checkButton = (CheckableImageView) v;
						checkButton.toggle();
						QueryHelper.update(	ctx, "container_session", ContainerSession.FIELD_AV,
											Integer.toString(Utils.toInt(checkButton.isChecked())),
											ContainerSession.FIELD_UUID + " = " + Utils.sqlString(uuid));
						mCheckedMap.put(uuid, Boolean.valueOf(checkButton.isChecked()));
					}

				});

			} else {
				holder.checkableImageView.setVisibility(View.GONE);
			}
		}
	}

	// get --> new --> bind
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View v = mInflater.inflate(mLayout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.containerIdView = (TextView) v.findViewById(R.id.feed_item_container_id);
		holder.containerOwnerView = (TextView) v.findViewById(R.id.feed_item_container_owner);
		holder.importDateView = (TextView) v.findViewById(R.id.feed_item_container_import_date);
		holder.itemPictureView = (ImageView) v.findViewById(R.id.feed_item_picture);
		holder.validationImageView = (ImageView) v.findViewById(R.id.feed_item_validator);
		holder.checkableImageView = (CheckableImageView) v.findViewById(R.id.check_button);

		v.setTag(holder);

		return v;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		mCheckedMap = new HashMap<String, Boolean>();
		return super.swapCursor(newCursor);
	}

}
