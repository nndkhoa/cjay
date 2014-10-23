package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.util.Logger;
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

    List<GateImage> mImportImages;
    List<AuditImage> mAuditImages;
    List<AuditImage> mRepairedImages;

    public PhotoExpandableListAdapter(Context context, int[] imageTypes,
                                      List<GateImage> importImages,
                                      List<AuditImage> auditImages,
                                      List<AuditImage> repairedImages) {

        mContext = context;
        mGridViews = new Hashtable<Integer, GridView>();
        mSectionHeaders = new ArrayList<String>();
        mImageTypes = imageTypes;

        for (int i = 0; i < mImageTypes.length; i++) {
            mSectionHeaders.add(Utils.getImageTypeDescription(mContext, mImageTypes[i]));
        }

        mImportImages = importImages;
        mAuditImages = auditImages;
        mRepairedImages = repairedImages;
    }

    @Override
    public int getGroupCount() {
        return mSectionHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mSectionHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_section, null);
        }

        String headerTitle = (String) getGroup(groupPosition);
        TextView sectionHeaderTextView = (TextView) convertView.findViewById(R.id.list_section_header);
        sectionHeaderTextView.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        // TODO: Inflate layout for child group view based on ```imageType```
        // Import/Export --> GridView (R.layout.expandable_list_photogrid_item)
        // Auditor --> ListView (R.layout.fragment_auditor_reporting)
        // Consider to apply ViewHolder Pattern

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_item, null);
        }

        GridView gridView = (GridView) convertView.findViewById(R.id.gv_images_item);
        if (groupPosition == 0) {
            gridView.setAdapter(new GateImageAdapter(mContext, R.layout.item_image_gridview,
                    mImportImages, false));

        }

        if (groupPosition == 1) {
            gridView.setAdapter(new AuditImageAdapter(mContext, mAuditImages));
        }

        if (groupPosition == 2) {
            gridView.setAdapter(new AuditImageAdapter(mContext, mRepairedImages));
        }

        mGridViews.put(Integer.valueOf(groupPosition), gridView);

        return convertView;
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public GridView getPhotoGridView(int groupPosition) {
        return mGridViews.get(Integer.valueOf(groupPosition));
    }

}
