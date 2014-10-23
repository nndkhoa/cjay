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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.MergeIssueActivity;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.view.CheckableImageView;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nambv on 21/10/2014.
 */
public class AuditImageAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private List<AuditImage> auditImages;

    public AuditImageAdapter(Context context, List<AuditImage> auditImages) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.auditImages = auditImages;
    }

    private class ViewHolder {
        ImageView ivAuditImage;
        CheckableImageView ivCheckable;
    }

    @Override
    public int getCount() {
        if (auditImages != null)
            return auditImages.size();
        return 0;
    }

    @Override
    public AuditImage getItem(int i) {
        return auditImages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        final AuditImage auditImage = getItem(i);
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
        ImageLoader.getInstance().displayImage(auditImages.get(i).getUrl(), holder.ivAuditImage);

        return view;
    }

    public void swapData(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
        notifyDataSetChanged();
    }
}
