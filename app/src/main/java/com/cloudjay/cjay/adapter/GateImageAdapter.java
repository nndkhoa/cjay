package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nambv on 07/10/2014.
 */
public class GateImageAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<GateImage> gateImages;
    private Context mContext;

    private boolean mCheckable = false;
    private ArrayList<String> mArrayCheckedImages;

    public GateImageAdapter(Context context, List<GateImage> gateImages, boolean isCheckable) {
        this.inflater = LayoutInflater.from(context);
        this.gateImages = gateImages;
        this.mContext = context;
        this.mCheckable = isCheckable;

        mArrayCheckedImages = new ArrayList<String>();
    }

    private class ViewHolder {
        public ImageView ivGateImage;
        public CheckablePhotoGridItemLayout photoLayout;
    }

    @Override
    public int getCount() {
        if (gateImages != null) {
            return gateImages.size();
        }
        return 0;
    }

    @Override
    public GateImage getItem(int position) {
        return gateImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_image_gridview, null);
            holder.ivGateImage = (ImageView) convertView.findViewById(R.id.iv_image);
            holder.photoLayout = (CheckablePhotoGridItemLayout) convertView.findViewById(R.id.photo_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CheckablePhotoGridItemLayout layout = (CheckablePhotoGridItemLayout) holder.photoLayout;
        layout.setShowCheckbox(mCheckable);
        if (mCheckable) {
                layout.setParentAdapter(this);
                layout.setCJayImageUrl(gateImages.get(i).getUrl());
                layout.setChecked(mArrayCheckedImages.contains(gateImages.get(i).getUrl()));
        }

        ImageLoader.getInstance().displayImage(gateImages.get(i).getUrl(), holder.ivGateImage);
        return convertView;
    }

    public void swapData(List<GateImage> gateImages) {
        this.gateImages = gateImages;
        notifyDataSetChanged();
    }

    public void addCheckedCJayImageUrl(String url) {
        mArrayCheckedImages.add(url);
    }

    public void removeCheckedCJayImageUrl(String url) {
        mArrayCheckedImages.remove(url);
    }

    public void setCheckedCJayImageUrls(ArrayList<String> checkedCJayImageUrls) {
        mArrayCheckedImages = checkedCJayImageUrls;
    }

    public List<String> getCheckedCJayImageUrls() {
        return mArrayCheckedImages;
    }

    public int getCheckedCJayImageUrlsCount() {
        return mArrayCheckedImages.size();
    }

    public void removeAllCheckedCJayImageUrl() {
        mArrayCheckedImages.clear();
    }
}
