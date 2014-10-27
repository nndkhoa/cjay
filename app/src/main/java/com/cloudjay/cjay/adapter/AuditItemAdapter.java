package com.cloudjay.cjay.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.MergeIssueActivity;
import com.cloudjay.cjay.activity.MergeIssueActivity_;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by nambv on 21/10/2014.
 */
public class AuditItemAdapter extends ArrayAdapter<AuditItem> {

    private LayoutInflater mInflater;
    private Context mContext;
    private int layoutResId;
    private String containerId;
    private AuditImage auditImage;

    public AuditItemAdapter(Context context, int resource, String containerId) {
        super(context, resource);
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
        this.containerId = containerId;

    }

    private class ViewHolder {
        public ImageView ivAuditImage;
        public TextView tvCodeComponent;
        public TextView tvCodeLocation;
        public TextView tvCodeIssue;
        public TextView tvCodeRepair;
        public TextView tvDimension;
        public TextView tvCount;
        public Button btnUpload;
        public Button btnRepair;
        public Button btnReport;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final AuditItem auditItem = getItem(i);

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(layoutResId, null);
            holder.ivAuditImage = (SquareImageView) view.findViewById(R.id.iv_audit_image);

            holder.tvCodeComponent = (TextView) view.findViewById(R.id.tv_code_component);
            holder.tvCodeIssue = (TextView) view.findViewById(R.id.tv_code_issue);
            holder.tvCodeLocation = (TextView) view.findViewById(R.id.tv_code_location);
            holder.tvDimension = (TextView) view.findViewById(R.id.tv_dimension);
            holder.tvCodeRepair = (TextView) view.findViewById(R.id.tv_code_repair);
            holder.tvCount = (TextView) view.findViewById(R.id.tv_count);

            holder.btnUpload = (Button) view.findViewById(R.id.btn_upload_pending);
            holder.btnReport = (Button) view.findViewById(R.id.btn_report_pending);
            holder.btnRepair = (Button) view.findViewById(R.id.btn_repair_pending);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        auditImage = auditItem.getAuditImages().get(0);
        ImageLoader.getInstance().displayImage(auditItem.getAuditImages().get(0).getUrl(),
                holder.ivAuditImage);
        holder.btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showApproveDiaglog();
            }
        });

        return view;
    }

    void showApproveDiaglog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Alert");
        builder.setMessage("Lỗi này đã được báo cáo chưa?");

        builder.setPositiveButton("Chưa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO add to database
                dialogInterface.dismiss();
            }
        });
//        builder.setNegativeButton("Vệ sinh", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                //TODO change status audit item to water wase
//                dialogInterface.dismiss();
//            }
//        });

        builder.setNegativeButton("Rồi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(mContext, MergeIssueActivity_.class);
                intent.putExtra(MergeIssueActivity.CONTAINER_ID_EXTRA, containerId);
                intent.putExtra(MergeIssueActivity.AUDIT_IMAGE_EXTRA, auditImage);
                mContext.startActivity(intent);
            }
        });

        builder.setNeutralButton("Vệ sinh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: do process here
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for BUTTON_NEGATIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(mContext.getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setBackgroundResource(R.drawable.btn_green_selector);

                // Set background and text color for BUTTON_NEUTRAL
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setTextColor(mContext.getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setBackgroundResource(R.drawable.btn_customize_selector);

                // Set background and text color for BUTTON_POSITIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(mContext.getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_dark));
            }
        });
        dialog.show();
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
