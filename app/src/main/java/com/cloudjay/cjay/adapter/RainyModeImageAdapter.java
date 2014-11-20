package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

/**
 * Created by nambv on 2014/11/17.
 */
public class RainyModeImageAdapter extends ArrayAdapter<String> {

    private LayoutInflater inflater;
    private int resource;
    private boolean mCheckable = false;
    private ArrayList<String> mArrayCheckedImages;

    public RainyModeImageAdapter(Context context, int resource, boolean isCheckable) {
        super(context, resource);

        this.inflater = LayoutInflater.from(context);
        this.mCheckable = isCheckable;
        this.resource = resource;

        mArrayCheckedImages = new ArrayList<>();
    }

    private class ViewHolder {
        public ImageView ivGateImage;
        public CheckablePhotoGridItemLayout photoLayout;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        final String rainyImageUrl = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(resource, null);
            holder.ivGateImage = (ImageView) convertView.findViewById(R.id.iv_image);
            holder.photoLayout = (CheckablePhotoGridItemLayout) convertView.findViewById(R.id.photo_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CheckablePhotoGridItemLayout layout = holder.photoLayout;
        layout.setShowCheckbox(mCheckable);

        if (mCheckable) {
            layout.setRainyModeAdapter(this);
            layout.setRainyImage(rainyImageUrl);
            layout.setChecked(mArrayCheckedImages.contains(rainyImageUrl));
        }

        ImageAware imageAware = new ImageViewAware(holder.ivGateImage, false);
        ImageLoader.getInstance().displayImage(rainyImageUrl, imageAware);
        return convertView;
    }

    public void addCheckedImageUrl(String imageUrl) {
        mArrayCheckedImages.add(imageUrl);
    }

    public void removeCheckedImageUrl(String imageUrl) {
        mArrayCheckedImages.remove(imageUrl);
    }

    public void setCheckedImageUrls(ArrayList<String> checkedImageUrls) {
        mArrayCheckedImages = checkedImageUrls;
    }

    public ArrayList<String> getCheckedImageUrls() {
        return mArrayCheckedImages;
    }

    public int getCheckedImageUrlsCount() {
        return mArrayCheckedImages.size();
    }

    public void removeAllCheckedImageUrl() {
        mArrayCheckedImages.clear();
    }
}
