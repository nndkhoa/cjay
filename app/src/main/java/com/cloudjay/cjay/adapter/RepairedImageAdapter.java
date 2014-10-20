package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.Session;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by thai on 21/10/2014.
 */
public class RepairedImageAdapter extends ArrayAdapter<AuditImage> {
    private LayoutInflater mInflater;
    private int layoutResId;
    Context context;

    public RepairedImageAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }
    private static class ViewHolder {
        ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AuditImage auditImage = getItem(position);

        // Apply ViewHolder pattern for better performance
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

         viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Set data to view
        ImageLoader.getInstance().displayImage(auditImage.getUrl(),viewHolder.imageView);

        return convertView;
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
