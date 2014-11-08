package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;

import java.util.List;

/**
 * Adapter for list container sessions
 */
public class SessionAdapter extends ArrayAdapter<Session> {

	private LayoutInflater mInflater;
	private int layoutResId;
	Context context;

	public SessionAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutResId = resource;
	}

	private static class ViewHolder {
		TextView tvContainerId;
		TextView tvOperator;
		TextView tvDateIn;
		TextView tvDateOut;
		TextView tvPreStatus;
		TextView tvCurrentStatus;
		TextView tvStep;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Session session = getItem(position);

		// Apply ViewHolder pattern for better performance
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(layoutResId, parent, false);
			viewHolder = new ViewHolder();

			viewHolder.tvContainerId = (TextView) convertView.findViewById(R.id.tv_container_id);
			viewHolder.tvOperator = (TextView) convertView.findViewById(R.id.tv_operator);
			viewHolder.tvDateIn = (TextView) convertView.findViewById(R.id.tv_date_in);
			viewHolder.tvDateOut = (TextView) convertView.findViewById(R.id.tv_date_out);
			viewHolder.tvPreStatus = (TextView) convertView.findViewById(R.id.tv_pre_status);
			viewHolder.tvCurrentStatus = (TextView) convertView.findViewById(R.id.tv_current_status);
			viewHolder.tvStep = (TextView) convertView.findViewById(R.id.tv_step);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		//Set data to view
		viewHolder.tvContainerId.setText(session.getContainerId());
		viewHolder.tvOperator.setText(session.getOperatorCode());

		// Set datetime
		String checkInDate = StringUtils.getTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE,
				CJayConstant.DAY_FORMAT, session.getCheckInTime());
		viewHolder.tvDateIn.setText(checkInDate);

		String checkOutDate = StringUtils.getTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE,
				CJayConstant.DAY_FORMAT, session.getCheckOutTime());
		if (TextUtils.isEmpty(checkOutDate)) {
			viewHolder.tvDateOut.setText("");
		} else {
			viewHolder.tvDateOut.setText(checkOutDate);
		}

		viewHolder.tvStep.setText((Step.values()[session.getLocalStep()]).toString());
		viewHolder.tvPreStatus.setText((Status.values()[(int) session.getPreStatus()]).toString());

		try {
			viewHolder.tvCurrentStatus.setText((Status.values()[(int) session.getStatus()]).toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public void setData(List<Session> data) {
		this.clear();
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				// add(data.get(i));
				this.insert(data.get(i), this.getCount());
			}
		}
		this.notifyDataSetChanged();
	}
}
