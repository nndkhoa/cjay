package com.cloudjay.cjay.adapter;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ContainerCursorAdapter extends CursorAdapter implements Filterable {

	private int layout;
	private Cursor mCursor;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	@SuppressWarnings("deprecation")
	public ContainerCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public ContainerCursorAdapter(Context context, int layout, Cursor c,
			int flags) {

		super(context, c, flags);

		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
		this.mCursor = c;
		this.imageLoader = ImageLoader.getInstance();
	}

	private static class ViewHolder {

		public TextView containerIdView;
		public TextView containerOwnerView;
		public TextView importDateView;
		public TextView exportDateView;
		public ImageView itemPictureView;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
			throw new IllegalStateException(
					"this should only be called when the cursor is valid");
		}

		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}

		View v;
		if (convertView == null) {
			v = newView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}

		bindView(v, mContext, mCursor);
		return v;

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			Logger.Log("Holder inside bindView is NULL");

			holder = new ViewHolder();
			holder.containerIdView = (TextView) view
					.findViewById(R.id.feed_item_container_id);
			holder.containerOwnerView = (TextView) view
					.findViewById(R.id.feed_item_container_owner);
			holder.importDateView = (TextView) view
					.findViewById(R.id.feed_item_container_import_date);
			holder.exportDateView = (TextView) view
					.findViewById(R.id.feed_item_container_export_date);
			holder.itemPictureView = (ImageView) view
					.findViewById(R.id.feed_item_picture);
			view.setTag(holder);
		}

		// TODO: get data from cursor and bind to holder
		String importDate = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_CHECK_IN_TIME));
		holder.importDateView.setText(importDate);

		String exportDate = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_CHECK_OUT_TIME));
		holder.exportDateView.setText(exportDate);

		String url = cursor.getString(cursor
				.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));

		if (!TextUtils.isEmpty(url)) {
			imageLoader.displayImage(url, holder.itemPictureView);
		} else {
			holder.itemPictureView.setImageResource(R.drawable.ic_app);
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.containerIdView = (TextView) v
				.findViewById(R.id.feed_item_container_id);
		holder.containerOwnerView = (TextView) v
				.findViewById(R.id.feed_item_container_owner);
		holder.importDateView = (TextView) v
				.findViewById(R.id.feed_item_container_import_date);
		holder.exportDateView = (TextView) v
				.findViewById(R.id.feed_item_container_export_date);
		holder.itemPictureView = (ImageView) v
				.findViewById(R.id.feed_item_picture);

		v.setTag(holder);

		return v;
	}

}
