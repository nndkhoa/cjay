package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by nambv on 01/11/2014.
 */
public class RepairedItemAdapter extends ArrayAdapter<AuditItem> {
	private LayoutInflater mInflater;
	private Context mContext;
	private int layoutResId;
	private AuditImage auditImage;

	public RepairedItemAdapter(Context context, int resource) {
		super(context, resource);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mContext = context;
		this.layoutResId = resource;
	}


	private class ViewHolder {
		public ImageView ivRepairedImage;
		public TextView tvCodeComponent;
		public TextView tvCodeLocation;
		public TextView tvCodeIssue;
		public TextView tvCodeRepair;
		public TextView tvDimension;
		public TextView tvCount;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		final AuditItem auditItem = getItem(position);

		final ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = mInflater.inflate(layoutResId, null);
			holder.ivRepairedImage = (SquareImageView) view.findViewById(R.id.iv_repaired_image);

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

		if (auditItem.getRepaired() == true) {
			//Set detail textviews
			holder.tvCodeComponent.setText(auditItem.getComponentCode());
			holder.tvCodeIssue.setText(auditItem.getDamageCode());
			holder.tvCodeLocation.setText(auditItem.getLocationCode());
			holder.tvDimension.setText("Dài " + auditItem.getLength() + "," + " Rộng " + auditItem.getHeight());
			holder.tvCodeRepair.setText(auditItem.getRepairCode());
			holder.tvCount.setText(auditItem.getQuantity() + "");

			if (auditItem.getAuditImages() != null && auditItem.getAuditImages().size() != 0) {
				if (auditItem.getAuditImages().get(0) != null) {
					auditImage = auditItem.getAuditImages().get(0);
					ImageAware imageAware = new ImageViewAware(holder.ivRepairedImage, false);
					ImageLoader.getInstance().displayImage(auditImage.getUrl(),
							imageAware);
				}
			}
		}

		return view;
	}
}
