package com.cloudjay.cjay.adapter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.cloudjay.cjay.PhotoExpandableListViewActivity;
import com.cloudjay.cjay.PhotoViewPagerActivity;
import com.cloudjay.cjay.PhotoViewPagerActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Utils;

/**
 * 
 * Expandable Item detail (GridView)
 * 
 * @author quocvule
 * 
 */
public class PhotoExpandableListAdapter extends BaseExpandableListAdapter {

	private final Context mContext;
	private final List<String> mSectionHeaders;
	private final String mContainerSessionUUID;
	private final int[] mImageTypes;
	private final Hashtable<Integer, GridView> mGridViews;

	public PhotoExpandableListAdapter(Context context, String containerSessionUUID, int[] imageTypes) {

		mContext = context;
		mGridViews = new Hashtable<Integer, GridView>();
		mSectionHeaders = new ArrayList<String>();

		mImageTypes = imageTypes;
		mContainerSessionUUID = containerSessionUUID;

		for (int i = 0; i < mImageTypes.length; i++) {
			mSectionHeaders.add(Utils.getImageTypeDescription(mContext, mImageTypes[i]));
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
								ViewGroup parent) {

		// TODO: Inflate layout for child group view based on ```imageType```
		// Import/Export --> GridView (R.layout.expandable_list_photogrid_item)
		// Auditor --> ListView (R.layout.fragment_auditor_reporting)
		// Consider to apply ViewHolder Pattern

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_item, null);
		}

		final int imageType = mImageTypes[groupPosition];
		final String title = mSectionHeaders.get(groupPosition);

		TextView emptyTextView = (TextView) convertView.findViewById(android.R.id.empty);
		GridView gridView = (GridView) convertView.findViewById(R.id.gridview);
		// gridView.setEmptyView(((FragmentActivity) mContext).findViewById(android.R.id.empty));
		gridView.setEmptyView(emptyTextView);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				Intent intent = new Intent(mContext, PhotoViewPagerActivity_.class);
				intent.putExtra(PhotoViewPagerActivity.START_POSITION, position);
				intent.putExtra(PhotoViewPagerActivity.CJAY_CONTAINER_SESSION_EXTRA, mContainerSessionUUID);
				intent.putExtra(PhotoViewPagerActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);
				intent.putExtra("title", title);

				mContext.startActivity(intent);
			}
		});

		if (groupPosition == 0) {
			((FragmentActivity) mContext).getSupportLoaderManager()
											.initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1, null,
														(PhotoExpandableListViewActivity) mContext);
		} else if (groupPosition == 1) {
			((FragmentActivity) mContext).getSupportLoaderManager()
											.initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2, null,
														(PhotoExpandableListViewActivity) mContext);
		}

		mGridViews.put(Integer.valueOf(groupPosition), gridView);

		return convertView;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mSectionHeaders.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mSectionHeaders.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_section, null);
		}

		String headerTitle = (String) getGroup(groupPosition);
		TextView sectionHeaderTextView = (TextView) convertView.findViewById(R.id.list_section_header);
		sectionHeaderTextView.setTypeface(null, Typeface.BOLD);
		sectionHeaderTextView.setText(headerTitle);

		return convertView;
	}

	public GridView getPhotoGridView(int groupPosition) {
		return mGridViews.get(Integer.valueOf(groupPosition));
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}
