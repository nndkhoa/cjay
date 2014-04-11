package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

public class UploadCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {

		public ProgressBar progressBar;
		public TextView containerIdView;
		public ImageView itemPictureView;
		public ImageView uploadResultImageView;

	}

	private int layout;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public UploadCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public UploadCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, c, flags);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
		mCursor = c;
		imageLoader = ImageLoader.getInstance();
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
			holder.containerIdView = (TextView) view.findViewById(R.id.tv_photo_caption);
			holder.itemPictureView = (ImageView) view.findViewById(R.id.iv_photo);
			holder.uploadResultImageView = (ImageView) view.findViewById(R.id.iv_upload_result);
			holder.progressBar = (ProgressBar) view.findViewById(R.id.pb_upload_progress);
			view.setTag(holder);
		}

		// get data from cursor and bind to holder
		// Container caption
		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		// String operator = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_NAME));

		holder.containerIdView.setText("Container: " + containerId);
		// if (TextUtils.isEmpty(operator)) {
		// holder.containerIdView.setText("Container: " + containerId);
		// } else {
		// holder.containerIdView.setText("Container: " + containerId + " | Operator: " + operator);
		// }

		// Image Id Path
		String url = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));
		if (!TextUtils.isEmpty(url)) {
			imageLoader.displayImage(url, holder.itemPictureView);
		} else {
			holder.itemPictureView.setImageResource(R.drawable.ic_app);
		}

		// Progress Bar
		int uploadState = cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_STATE));
		switch (uploadState) {
			case ContainerSession.STATE_UPLOAD_COMPLETED:
				holder.progressBar.setVisibility(View.GONE);
				holder.uploadResultImageView.setImageResource(R.drawable.ic_success);
				holder.uploadResultImageView.setVisibility(View.VISIBLE);
				break;

			case ContainerSession.STATE_UPLOAD_ERROR:
				holder.progressBar.setVisibility(View.GONE);
				holder.uploadResultImageView.setImageResource(R.drawable.ic_error);
				holder.uploadResultImageView.setVisibility(View.VISIBLE);
				break;

			case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			case ContainerSession.STATE_UPLOAD_WAITING:
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.uploadResultImageView.setVisibility(View.GONE);
				holder.progressBar.setIndeterminate(true);
				break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(layout, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.progressBar = (ProgressBar) v.findViewById(R.id.pb_upload_progress);
		holder.containerIdView = (TextView) v.findViewById(R.id.tv_photo_caption);
		holder.itemPictureView = (ImageView) v.findViewById(R.id.iv_photo);
		holder.uploadResultImageView = (ImageView) v.findViewById(R.id.iv_upload_result);
		v.setTag(holder);
		return v;
	}

}
