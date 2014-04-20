package com.cloudjay.cjay.adapter;

import java.util.ArrayList;
import java.util.List;

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
import com.cloudjay.cjay.view.CheckablePhotoItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PhotoGridViewCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {
		public ImageView imageView;
		public CheckablePhotoItemLayout photoLayout;
	}

	private int mLayout;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	
	private boolean mCheckable = false;
	private ArrayList<String> mCheckedCJayImageUuids;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public PhotoGridViewCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public PhotoGridViewCursorAdapter(Context context, int layout, Cursor c, int flags) {
		this(context, layout, c, flags, false);
	}

	public PhotoGridViewCursorAdapter(Context context, int layout, Cursor c, int flags, boolean itemCheckable) {
		super(context, c, flags);
		this.mLayout = layout;
		mInflater = LayoutInflater.from(context);
		mCursor = c;
		mImageLoader = ImageLoader.getInstance();
		
		mCheckable = itemCheckable;
		mCheckedCJayImageUuids = new ArrayList<String>();
	}
	
	public boolean isCheckable() {
		return mCheckable;
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
			holder.photoLayout = (CheckablePhotoItemLayout) view.findViewById(R.id.photo_layout);
			holder.imageView = (ImageView) holder.photoLayout.findViewById(R.id.picture);
			
			view.setTag(holder);
		}

		String cJayImageUuid = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
		String url = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_URI));
		if (!TextUtils.isEmpty(url)) {
			mImageLoader.displayImage(url, holder.imageView);
		} else {
			holder.imageView.setImageResource(R.drawable.ic_app);
		}
		
		CheckablePhotoItemLayout layout = (CheckablePhotoItemLayout) holder.photoLayout;
		layout.setShowCheckbox(mCheckable);
		layout.setParentAdapter(this);
		layout.setCJayImageUuid(cJayImageUuid);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mInflater.inflate(mLayout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.photoLayout = (CheckablePhotoItemLayout) v.findViewById(R.id.photo_layout);
		holder.imageView = (ImageView) holder.photoLayout.findViewById(R.id.picture);

		v.setTag(holder);

		return v;
	}

	public void addCheckedCJayImage(String cJayImageUuid) {
		mCheckedCJayImageUuids.add(cJayImageUuid);
	}

	public void removeCheckedCJayImage(String cJayImageUuid) {
		mCheckedCJayImageUuids.remove(cJayImageUuid);
	}
	
	public List<String> getCheckedCJayImageUuids() {
		return mCheckedCJayImageUuids;
	}
}