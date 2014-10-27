package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by thai on 22/10/2014.
 */
public class AuditMergeIssueAdapter extends ArrayAdapter<AuditItem> {

    private LayoutInflater mInflater;
    private int layoutResId;
    Context context;

    public AuditMergeIssueAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }

    private static class ViewHolder {
        TextView tvCompCode;
        TextView tvLocaitonCode;
        TextView tvDamageCode;
        TextView tvRepairCode;
        TextView tvSize;
        TextView tvQuantity;
        ImageView ivAuditImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AuditItem auditItem = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvCompCode = (TextView) convertView.findViewById(R.id.tv_code_component_merge);
            viewHolder.tvLocaitonCode = (TextView) convertView.findViewById(R.id.tv_code_location_merge);
            viewHolder.tvDamageCode = (TextView) convertView.findViewById(R.id.tv_code_damaged_merge);
            viewHolder.tvRepairCode = (TextView) convertView.findViewById(R.id.tv_code_repair_merge);
            viewHolder.tvSize = (TextView) convertView.findViewById(R.id.tv_dimension_merge);
            viewHolder.tvQuantity = (TextView) convertView.findViewById(R.id.tv_quantity_merge);
            viewHolder.ivAuditImage = (ImageView) convertView.findViewById(R.id.iv_audit_image_merge);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Set data to view
        viewHolder.tvCompCode.setText(auditItem.getComponentCode());
        viewHolder.tvLocaitonCode.setText(auditItem.getLocationCode());
        viewHolder.tvDamageCode.setText(auditItem.getDamageCode());
        viewHolder.tvRepairCode.setText(auditItem.getRepairCode());
        viewHolder.tvSize.setText("Dài " + auditItem.getHeight() + "," + " Rộng " + auditItem.getLength());
        viewHolder.tvQuantity.setText(String.valueOf(auditItem.getQuantity()));
        ImageLoader.getInstance().displayImage(auditItem.getAuditImages().get(0).getUrl(), viewHolder.ivAuditImage);
        return convertView;
    }

    public void setData(List<AuditItem> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
