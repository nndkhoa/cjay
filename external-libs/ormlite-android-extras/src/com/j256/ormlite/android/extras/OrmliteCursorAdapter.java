package com.j256.ormlite.android.extras;

import java.sql.SQLException;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CursorAdapter;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.stmt.PreparedQuery;

public abstract class OrmliteCursorAdapter<T> extends CursorAdapter {

	private PreparedQuery<T> mQuery;
	protected int layout;
	protected LayoutInflater inflater;

	public OrmliteCursorAdapter(Context context, Cursor c,
			PreparedQuery<T> query) {
		super(context, c, false);
		setQuery(query);
	}

	public OrmliteCursorAdapter(Context context, int layout, Cursor c,
			PreparedQuery<T> query) {
		super(context, c, false);

		setQuery(query);
		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View itemView, Context context, Cursor cursor) {
		try {
			T item = getQuery()
					.mapRow(new AndroidDatabaseResults(cursor, null));
			bindView(itemView, context, item);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setQuery(PreparedQuery<T> query) {
		mQuery = query;
	}

	abstract public void bindView(View itemView, Context context, T item);

	public PreparedQuery<T> getQuery() {
		return mQuery;
	}

}
