package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nambv on 21/10/2014.
 */
public class AuditItemAdapter extends ArrayAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private List<AuditItem> auditItems;

    public AuditItemAdapter(Context context, int resource) {
        super(context, resource);
    }

    private class ViewHolder {
        public ImageView ivAuditImage;
        public TextView tvCodeComponent;
        public TextView tvCodeLocation;
        public TextView tvCodeIssue;
        public TextView tvCodeRepair;
        public TextView tvDimension;
        public TextView tvCount;
    }

    @Override
    public int getCount() {
        if (auditItems != null)
            return auditItems.size();
        return 0;
    }

    @Override
    public AuditItem getItem(int i) {
        return auditItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_issue_pending, null);
            holder.ivAuditImage = (ImageView) view.findViewById(R.id.iv_audit_image);
            holder.tvCodeComponent = (TextView) view.findViewById(R.id.tv_code_component);
            holder.tvCodeIssue = (TextView) view.findViewById(R.id.tv_code_issue);
            holder.tvCodeLocation = (TextView) view.findViewById(R.id.tv_code_location);
            holder.tvDimension = (TextView) view.findViewById(R.id.tv_dimension);
            holder.tvCodeRepair = (TextView) view.findViewById(R.id.tv_code_repair);
            holder.tvCount = (TextView) view.findViewById(R.id.tv_count);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ImageLoader.getInstance().displayImage(auditItems.get(i).getAuditImages().get(0).getUrl(),
                holder.ivAuditImage);

        return view;
    }
}
