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

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PhotoGridViewCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {
		public ImageView imageView;
	}

	private int layout;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public PhotoGridViewCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public PhotoGridViewCursorAdapter(Context context, int layout, Cursor c, int flags) {
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
			holder.imageView = (ImageView) view.findViewById(R.id.picture);
			view.setTag(holder);
		}

		String url = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_URI));
		if (!TextUtils.isEmpty(url)) {
			imageLoader.displayImage(url, holder.imageView);
		} else {
			holder.imageView.setImageResource(R.drawable.ic_app);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.imageView = (ImageView) v.findViewById(R.id.picture);

		v.setTag(holder);

		return v;
	}

}
