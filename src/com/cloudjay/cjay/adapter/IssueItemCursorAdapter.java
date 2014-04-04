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
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

public class IssueItemCursorAdapter extends CursorAdapter implements Filterable {

	private int layout;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public IssueItemCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public IssueItemCursorAdapter(Context context, int layout, Cursor c,
			int flags) {
		super(context, c, flags);
		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
		this.mCursor = c;
		this.imageLoader = ImageLoader.getInstance();
	}

	private static class ViewHolder {

		public TextView locationTextView;
		public TextView damageTextView;
		public TextView componenTextView;
		public TextView repairTextView;
		public TextView quantityTextView;
		public TextView lengthTextView;
		public TextView heightTextView;
		public ImageView itemPictureView;

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
			holder.locationTextView = (TextView) view
					.findViewById(R.id.issue_location_code);

			holder.damageTextView = (TextView) view
					.findViewById(R.id.issue_damage_code);
			holder.repairTextView = (TextView) view
					.findViewById(R.id.issue_repair_code);
			holder.componenTextView = (TextView) view
					.findViewById(R.id.issue_component_code);

			holder.quantityTextView = (TextView) view
					.findViewById(R.id.issue_quantity);
			holder.lengthTextView = (TextView) view
					.findViewById(R.id.issue_length);
			holder.heightTextView = (TextView) view
					.findViewById(R.id.issue_height);

			holder.itemPictureView = (ImageView) view
					.findViewById(R.id.issue_picture);

			view.setTag(holder);
		}

		// get data from cursor and bind to holder
		String location = cursor.getString(cursor
				.getColumnIndexOrThrow("location_code"));
		String damage = cursor.getString(cursor
				.getColumnIndexOrThrow("damage_code"));
		String repair = cursor.getString(cursor
				.getColumnIndexOrThrow("repair_code"));
		String component = cursor.getString(cursor
				.getColumnIndexOrThrow("component_code"));
		String quantity = cursor.getString(cursor
				.getColumnIndexOrThrow("quantity"));
		String length = cursor
				.getString(cursor.getColumnIndexOrThrow("length"));
		String height = cursor
				.getString(cursor.getColumnIndexOrThrow("height"));
		String url = cursor
				.getString(cursor.getColumnIndexOrThrow("image_url"));

		holder.locationTextView.setText(location);

		holder.damageTextView.setText(damage);
		holder.repairTextView.setText(repair);
		holder.componenTextView.setText(component);

		holder.quantityTextView.setText(quantity);
		holder.lengthTextView.setText(length);
		holder.heightTextView.setText(height);

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
		holder.locationTextView = (TextView) v
				.findViewById(R.id.issue_location_code);
		holder.damageTextView = (TextView) v
				.findViewById(R.id.issue_damage_code);
		holder.repairTextView = (TextView) v
				.findViewById(R.id.issue_repair_code);
		holder.componenTextView = (TextView) v
				.findViewById(R.id.issue_component_code);
		holder.quantityTextView = (TextView) v
				.findViewById(R.id.issue_quantity);
		holder.lengthTextView = (TextView) v.findViewById(R.id.issue_length);
		holder.heightTextView = (TextView) v.findViewById(R.id.issue_height);
		holder.itemPictureView = (ImageView) v.findViewById(R.id.issue_picture);

		v.setTag(holder);

		return v;
	}

}
