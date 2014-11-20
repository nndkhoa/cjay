package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
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

    public List<GateImage> mImportImages;
    public List<AuditImage> mAuditImages;
    public List<AuditImage> mRepairedImages;

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

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_photogrid_item, null);
        }

        if (groupPosition == 0) {
            GridView gridViewImport = (GridView) convertView.findViewById(R.id.gv_images_item);
            GateImageAdapter gateImageAdapter = new GateImageAdapter(mContext, R.layout.item_image_gridview,
                    false);
            gateImageAdapter.setData(mImportImages);
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) gridViewImport.getLayoutParams();
            if (gateImageAdapter.getCount() > 0) {
                int gridViewWidth = gridViewImport.getMeasuredWidth();
                // set height for gridview, 2 is a column count
                params.height = gridViewWidth / 2 * (int) (1.0 * (gateImageAdapter.getCount()) / 2 + 0.5);
            } else {
                // set height = 0 for gridview
                params.height = 0;

            }
            gridViewImport.setLayoutParams(params);
            gridViewImport.setAdapter(gateImageAdapter);
        }

        if (groupPosition == 1) {
            GridView gridViewAudit = (GridView) convertView.findViewById(R.id.gv_images_item);
            AuditImageAdapter auditItemAdapter = new AuditImageAdapter(mContext, R.layout.item_image_gridview);
            auditItemAdapter.setData(mAuditImages);
            // Do the same for gridViewAudit to set height
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) gridViewAudit.getLayoutParams();
            if (auditItemAdapter.getCount() > 0) {
                int gridViewWidth = gridViewAudit.getMeasuredWidth();
                params.height = gridViewWidth / 2 * (int) (1.0 * (auditItemAdapter.getCount()) / 2 + 0.5);
            } else {
                params.height = 0;
            }
            gridViewAudit.setLayoutParams(params);
            gridViewAudit.setAdapter(auditItemAdapter);
        }

        if (groupPosition == 2) {
            GridView gridViewRepaired = (GridView) convertView.findViewById(R.id.gv_images_item);
            AuditImageAdapter repairedItemAdapter = new AuditImageAdapter(mContext, R.layout.item_image_gridview);
            repairedItemAdapter.setData(mRepairedImages);
            // Do the same for gridViewRepaired to set height
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) gridViewRepaired.getLayoutParams();
              if (repairedItemAdapter.getCount() > 0) {
                int gridViewWidth = gridViewRepaired.getMeasuredWidth();
                params.height = gridViewWidth / 2 * (int) (1.0 * (repairedItemAdapter.getCount()) / 2 + 0.5);
            } else {
                params.height = 0;

            }
            gridViewRepaired.setLayoutParams(params);
            gridViewRepaired.setAdapter(repairedItemAdapter);
        }

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
