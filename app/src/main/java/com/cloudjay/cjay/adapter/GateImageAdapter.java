package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.util.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by nambv on 07/10/2014.
 */
public class GateImageAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<GateImage> gateImages;

    public GateImageAdapter(Context context, List<GateImage> gateImages) {
        this.inflater = LayoutInflater.from(context);
        this.gateImages = gateImages;

    }

    private class ViewHolder {
        ImageView ivGateImage;
    }

    @Override
    public int getCount() {
        if (gateImages != null) {
            return gateImages.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return gateImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.gate_image_item, null);
            holder.ivGateImage = (ImageView) convertView.findViewById(R.id.iv_gate_image_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Logger.Log("image: " + gateImages.get(i).getUrl());
        ImageLoader.getInstance().displayImage(gateImages.get(i).getUrl(), holder.ivGateImage);
        return convertView;
    }

    public void swapData(List<GateImage> gateImages) {
        this.gateImages = gateImages;
        notifyDataSetChanged();
    }
}
