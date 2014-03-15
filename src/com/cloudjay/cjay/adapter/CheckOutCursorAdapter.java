package com.cloudjay.cjay.adapter;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.util.Logger;
import com.j256.ormlite.android.extras.OrmliteCursorAdapter;
import com.j256.ormlite.stmt.PreparedQuery;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Sample Cursor Adapter using Ormlite Extra. Just sample.
 * 
 * @author tieubao
 * 
 */
public class CheckOutCursorAdapter extends
		OrmliteCursorAdapter<ContainerSession> implements Filterable {

	private ImageLoader imageLoader;

	public CheckOutCursorAdapter(Context context, int layout, Cursor c,
			PreparedQuery<ContainerSession> query) {
		super(context, layout, c, query);
		this.imageLoader = ImageLoader.getInstance();
	}

	@Override
	public void bindView(View itemView, Context context, ContainerSession item) {

		ViewHolder holder = (ViewHolder) itemView.getTag();
		if (holder == null) {

			Logger.Log("Holder inside bindView is NULL");
			holder = new ViewHolder();
			holder.containerIdView = (TextView) itemView
					.findViewById(R.id.feed_item_container_id);
			holder.containerOwnerView = (TextView) itemView
					.findViewById(R.id.feed_item_container_owner);
			holder.importDateView = (TextView) itemView
					.findViewById(R.id.feed_item_container_import_date);
			holder.exportDateView = (TextView) itemView
					.findViewById(R.id.feed_item_container_export_date);
			holder.itemPictureView = (ImageView) itemView
					.findViewById(R.id.feed_item_picture);
			itemView.setTag(holder);
		}

		if (isScrolling == true) {

			holder.containerIdView
					.setText(item.getContainer().getContainerId());
			holder.containerOwnerView.setText(item.getContainer().getOperator()
					.getName());
		} else {
			holder.importDateView.setText(item.getCheckInTime());
			// holder.exportDateView.setText(item.getCheckOutTime());
			holder.containerOwnerView.setText(item.getContainer().getOperator()
					.getName());
			holder.containerIdView
					.setText(item.getContainer().getContainerId());

			String url = item.getImageIdPath();
			if (!TextUtils.isEmpty(url)) {
				imageLoader.displayImage(url, holder.itemPictureView);
			} else {
				holder.itemPictureView.setImageResource(R.drawable.ic_app);
			}
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

	private static class ViewHolder {
		public TextView containerIdView;
		public TextView containerOwnerView;
		public TextView importDateView;
		public TextView exportDateView;
		public ImageView itemPictureView;
	}
}
