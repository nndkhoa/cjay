package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class GateImageAdapter extends ArrayAdapter<GateImage> {

	private LayoutInflater inflater;
	private Context mContext;
	private int resource;


	private boolean mCheckable = false;
	private ArrayList<String> mArrayCheckedImages;

	public GateImageAdapter(Context context, int resource, boolean isCheckable) {
		super(context, resource);

		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mCheckable = isCheckable;
		this.resource = resource;

		mArrayCheckedImages = new ArrayList<String>();
	}

	private class ViewHolder {
		public ImageView ivGateImage;
		public CheckablePhotoGridItemLayout photoLayout;
	}

	@Override
	public View getView(final int i, View convertView, ViewGroup viewGroup) {

        final GateImage gateImage = getItem(i);

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(resource, null);
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
			layout.setCJayImageUrl(gateImage.getUrl());
			layout.setChecked(mArrayCheckedImages.contains(gateImage.getUrl()));
		}

		ImageLoader.getInstance().displayImage(gateImage.getUrl(), holder.ivGateImage);
		return convertView;
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

    public void setData(List<GateImage> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
