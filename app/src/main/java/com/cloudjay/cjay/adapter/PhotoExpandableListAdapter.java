package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;

import com.cloudjay.cjay.util.Utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by nambv on 14/10/2014.
 */
public class PhotoExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private final List<String> mSectionHeaders;
    private final int[] mImageTypes;
    private final Hashtable<Integer, GridView> mGridViews;

    public PhotoExpandableListAdapter(Context context, String containerSessionUUID, int[] imageTypes) {

        mContext = context;
        mGridViews = new Hashtable<Integer, GridView>();
        mSectionHeaders = new ArrayList<String>();
        mImageTypes = imageTypes;

        for (int i = 0; i < mImageTypes.length; i++) {
            mSectionHeaders.add(Utils.getImageTypeDescription(mContext, mImageTypes[i]));
        }

    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i2) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i2) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return false;
    }
}
