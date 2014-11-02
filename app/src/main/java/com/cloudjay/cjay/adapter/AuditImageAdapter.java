package com.cloudjay.cjay.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.MergeIssueActivity;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.view.CheckableImageView;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nambv on 21/10/2014.
 */
public class AuditImageAdapter extends ArrayAdapter<AuditImage> {

    private LayoutInflater inflater;
    private Context mContext;
    private int mResource;

    public AuditImageAdapter(Context context, int resource) {
        super(context, resource);

        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mResource = resource;
    }

    private class ViewHolder {
        ImageView ivAuditImage;
        CheckableImageView ivCheckable;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final AuditImage auditImage = getItem(i);

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_image_gridview, null);
            holder.ivAuditImage = (ImageView) view.findViewById(R.id.iv_image);
            holder.ivCheckable = (CheckableImageView) view.findViewById(R.id.cb_select);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.ivCheckable.setVisibility(View.GONE);
		ImageAware imageAware = new ImageViewAware(holder.ivAuditImage, false);
        ImageLoader.getInstance().displayImage(auditImage.getUrl(), imageAware);

        return view;
    }

    public void setData(List<AuditImage> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
