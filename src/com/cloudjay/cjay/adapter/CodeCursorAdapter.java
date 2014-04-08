package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.util.Logger;

public class CodeCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {

		public TextView codeTextView;

	}

	private int layout;
	private LayoutInflater inflater;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public CodeCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public CodeCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, c, flags);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
		mCursor = c;
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
			holder.codeTextView = (TextView) view.findViewById(R.id.name);
			view.setTag(holder);
		}

		// get data from cursor and bind to holder
		String name = cursor.getString(cursor.getColumnIndexOrThrow(DamageCode.DISPLAY_NAME));

		holder.codeTextView.setText(name);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.codeTextView = (TextView) v.findViewById(R.id.name);
		v.setTag(holder);

		return v;
	}

}
