package com.cloudjay.cjay.adapter;

import com.cloudjay.cjay.R;

import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.UserLog;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
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

public class UserLogCursorAdapter extends CursorAdapter implements Filterable {

	private int layout;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public UserLogCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public UserLogCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, c, flags);
		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
		this.mCursor = c;
		this.imageLoader = ImageLoader.getInstance();
	}

	private static class ViewHolder {

		public TextView logContentTextView;
		public ImageView logTypeImageView;

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (cursor == null) {
			Logger.e("-----> BUG");
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			Logger.Log("Holder inside bindView is NULL");

			holder = new ViewHolder();
			holder.logContentTextView = (TextView) view
					.findViewById(R.id.tv_log_content);
			holder.logTypeImageView = (ImageView) view
					.findViewById(R.id.iv_log_type);
			view.setTag(holder);
		}

		// get data from cursor and bind to holder
		String time = cursor.getString(cursor
				.getColumnIndexOrThrow(UserLog.FIELD_TIME));

		String content = cursor.getString(cursor
				.getColumnIndexOrThrow(UserLog.FIELD_CONTENT));

		holder.logContentTextView.setText(time + " - " + content);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.logContentTextView = (TextView) v
				.findViewById(R.id.tv_log_content);
		holder.logTypeImageView = (ImageView) v.findViewById(R.id.iv_log_type);

		v.setTag(holder);

		return v;
	}

}
