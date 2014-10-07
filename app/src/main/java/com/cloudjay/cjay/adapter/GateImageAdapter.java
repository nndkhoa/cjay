package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.GateImage;

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
        // ImageView ivGateImage;
        TextView tvImageUrl;
    }

    @Override
    public int getCount() {
        return gateImages.size();
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
            //holder.ivGateImage = (ImageView) convertView.findViewById(R.id.iv_gate_image_item);
            holder.tvImageUrl = (TextView) convertView.findViewById(R.id.tv_image_url);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvImageUrl.setText(gateImages.get(i).getUrl());

        /*Bitmap bitmap = BitmapFactory.decodeFile(gateImages.get(i).getUrl());
        holder.ivGateImage.setImageBitmap(bitmap);*/
        return convertView;
    }
}
