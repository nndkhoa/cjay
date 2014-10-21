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
import com.nostra13.universalimageloader.core.ImageLoader;

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
    }

    private class ViewHolder {
        ImageView ivAuditImage;
        Button btnReport;

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
            view = inflater.inflate(R.layout.item_issue_pending, null);
            holder.ivAuditImage = (ImageView) view.findViewById(R.id.iv_audit_image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ImageLoader.getInstance().displayImage(auditImages.get(i).getUrl(), holder.ivAuditImage);
        holder.btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertReportDialog(auditImage);
            }
        });

        return view;
    }

    private void showAlertReportDialog(AuditImage auditImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.dialog_search_container_title);
        builder.setMessage("Lỗi này đã được bao cáo chưa?");

        builder.setPositiveButton("Chưa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(mContext, MergeIssueActivity.class);
                mContext.startActivity(intent);
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Rồi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO add to database
                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton("Vệ sinh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO switch to audit fragment @Vu
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for confirm button
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(mContext.getResources().getColor(android.R.color.holo_green_dark));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundResource(mContext.getResources().getColor(android.R.color.darker_gray));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setBackgroundResource(mContext.getResources().getColor(android.R.color.holo_blue_light));
            }
        });
        dialog.show();
    }

    public void swapData(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
        notifyDataSetChanged();
    }
}
