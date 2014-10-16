package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Utils;

import java.util.List;


/**
 * Created by thai on 09/10/2014.
 */
public class UploadSessionAdapter extends ArrayAdapter<Session> {

    private LayoutInflater mInflater;
    private int layoutResId;
    Context context;

    public UploadSessionAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }

    private static class ViewHolder {
        TextView tvContainerId;
        TextView tvCurrentPhotoUpload;
        TextView tvTotalPhotoUpload;
        ImageView ivContainer;
        ProgressBar pbUpLoading;
        ImageView ivUploadStatus;
        TextView tvUploadError;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Session session = getItem(position);
        // Apply ViewHolder pattern for better performance
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.tvContainerId = (TextView) convertView.findViewById(R.id.tv_containerId_uploading);
            viewHolder.tvCurrentPhotoUpload = (TextView) convertView.findViewById(R.id.tv_current_photo_upload);
            viewHolder.tvTotalPhotoUpload = (TextView) convertView.findViewById(R.id.tv_total_photo_upload);
            viewHolder.ivContainer = (ImageView) convertView.findViewById(R.id.iv_container_upload);
            viewHolder.pbUpLoading = (ProgressBar) convertView.findViewById(R.id.pb_upload_progress);
            viewHolder.ivUploadStatus = (ImageView) convertView.findViewById(R.id.iv_upload_result);
            viewHolder.tvUploadError = (TextView) convertView.findViewById(R.id.tv_upload_error);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //Set data to view
        viewHolder.tvContainerId.setText(session.getContainerId());
        viewHolder.tvTotalPhotoUpload.setText(String.valueOf(Utils.countTotalImage(session)));
        viewHolder.tvCurrentPhotoUpload.setText(String.valueOf(Utils.countUploadedImage(session)));
        if (NetworkHelper.isConnected(context)) {
            if (Utils.countTotalImage(session) == Utils.countUploadedImage(session)) {
                viewHolder.ivUploadStatus.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.ivUploadStatus.setImageResource(R.drawable.ic_error);
            viewHolder.pbUpLoading.setVisibility(View.GONE);
            viewHolder.tvUploadError.setVisibility(View.VISIBLE);

        }

        return convertView;
    }

    public void setData(List<Session> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
