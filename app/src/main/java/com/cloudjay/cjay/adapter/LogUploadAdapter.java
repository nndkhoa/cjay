package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.LogItem;

import java.util.List;

/**
 * Created by thai on 27/10/2014.
 */
public class LogUploadAdapter extends ArrayAdapter<LogItem> {

    private LayoutInflater mInflater;
    private int layoutResId;
    Context context;

    public LogUploadAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }

    private static class ViewHolder {
        TextView tvContainerId;
        TextView tvMessage;
        TextView tvTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogItem logUpload = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.tvContainerId = (TextView) convertView.findViewById(R.id.tv_containerid_log);
            viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tv_message_log);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time_log);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvContainerId.setText(logUpload.getContainerId());
        viewHolder.tvMessage.setText(logUpload.getMessage());
        viewHolder.tvTime.setText(logUpload.getTime());
        return convertView;
    }

    public void setData(List<LogItem> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
