package com.cloudjay.cjay.adapter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.PhotoViewPagerActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJayCursorLoader;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;
 
public class PhotoExpandableListAdapter extends BaseExpandableListAdapter implements LoaderCallbacks<Cursor> {
 
    private Context mContext;
    private List<String> mSectionHeaders;
    private String mContainerSessionUUID;
    private int[] mImageTypes;
    private Hashtable<Integer, GridView> mGridViews;
	private Hashtable<Integer, PhotoGridViewCursorAdapter> mCursorAdapters;
	private int mItemLayout;
 
    public PhotoExpandableListAdapter(Context context, String containerSessionUUID, int[] imageTypes) {
        mContext = context;
        mItemLayout = R.layout.grid_item_image;
        mGridViews = new Hashtable<Integer, GridView>();
        mCursorAdapters = new Hashtable<Integer, PhotoGridViewCursorAdapter>();
        mSectionHeaders = new ArrayList<String>();
        
        mImageTypes = imageTypes;
        mContainerSessionUUID = containerSessionUUID;
        
        for (int i = 0; i < mImageTypes.length; i++) {
			mSectionHeaders.add(Utils.getImageTypeDescription(mContext, mImageTypes[i]));
		}
    }
 
    public GridView getPhotoGridView(int groupPosition) {
        return mGridViews.get(Integer.valueOf(groupPosition));
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
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_item, null);
        }
        
        final int imageType = mImageTypes[groupPosition];
        final String title = mSectionHeaders.get(groupPosition);
        GridView gridView = (GridView)convertView.findViewById(R.id.gridview);
		gridView.setEmptyView(((FragmentActivity)mContext).findViewById(android.R.id.empty));
		gridView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(mContext, PhotoViewPagerActivity_.class);
				intent.putExtra(PhotoViewPagerActivity_.START_POSITION,
						position);
				intent.putExtra(
						PhotoViewPagerActivity_.CJAY_CONTAINER_SESSION_EXTRA,
						mContainerSessionUUID);
				intent.putExtra(PhotoViewPagerActivity_.CJAY_IMAGE_TYPE_EXTRA,
						imageType);
				intent.putExtra("title", title);
				mContext.startActivity(intent);
			}
		});
		
		if (groupPosition == 0) {
			((FragmentActivity) mContext).getSupportLoaderManager().initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1, null, this);			
		} else if (groupPosition == 1) {
			((FragmentActivity) mContext).getSupportLoaderManager().initLoader(CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2, null, this);				
		}
		
		mGridViews.put(Integer.valueOf(groupPosition), gridView);
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.mSectionHeaders.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.mSectionHeaders.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_section, null);
        }

        String headerTitle = (String) getGroup(groupPosition);
        TextView sectionHeaderTextView = (TextView) convertView
                .findViewById(R.id.list_section_header);
        sectionHeaderTextView.setTypeface(null, Typeface.BOLD);
        sectionHeaderTextView.setText(headerTitle);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		int imageType = -1;

		switch (id) {
		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
			imageType = mImageTypes[0];
			break;

		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
			imageType = mImageTypes[1];
			break;
		}
		
		final int cursorLoaderImageType = imageType;
		
		return new CJayCursorLoader(mContext) {
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = DataCenter.getInstance()
						.getCJayImagesCursorByContainer(getContext(),
								mContainerSessionUUID, cursorLoaderImageType);

				if (cursor != null) {
					// Ensure the cursor window is filled
					cursor.registerContentObserver(mObserver);
				}

				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int adapterId = 0;
		
		switch (loader.getId()) {
		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
			adapterId = 0;		
			break;

		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
			adapterId = 1;		
			break;
		}
		
		if (mCursorAdapters.get(Integer.valueOf(adapterId)) == null) {
			mCursorAdapters.put(Integer.valueOf(adapterId), new PhotoGridViewCursorAdapter(mContext,
					mItemLayout, cursor, 0));
			
			GridView gridView = this.getPhotoGridView(adapterId);
			gridView.setAdapter(mCursorAdapters.get(Integer.valueOf(adapterId)));
			
			if (cursor.getCount() > 0) {
				LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) gridView.getLayoutParams();
				p.height = (gridView.getMeasuredWidth()/2) * (int)((cursor.getCount()+1)/2);
				gridView.setLayoutParams(p);
			}			
			
		} else {
			mCursorAdapters.get(Integer.valueOf(adapterId)).swapCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
		switch (loader.getId()) {
		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_1:
			mCursorAdapters.get(Integer.valueOf(0)).swapCursor(null);			
			break;

		case CJayConstant.CURSOR_LOADER_ID_PHOTO_GRIDVIEW_2:
			mCursorAdapters.get(Integer.valueOf(1)).swapCursor(null);			
			break;
		}
	}
}
