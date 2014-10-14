package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.model.Session;

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

		Button btnSubmit;
		Button btnContinue;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Session session = getItem(position);

		boolean isSessionProcessing;
		isSessionProcessing = session.isProcessing();

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

			viewHolder.btnSubmit = (Button) convertView.findViewById(R.id.btn_submit);
			viewHolder.btnContinue = (Button) convertView.findViewById(R.id.btn_continue);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		//Set data to view
		viewHolder.tvContainerId.setText(session.getContainerId());
		viewHolder.tvOperator.setText(session.getOperatorCode());
		viewHolder.tvDateIn.setText(String.valueOf(session.getCheckInTime()));
        if (session.getCheckOutTime() != null) {
            viewHolder.tvDateOut.setText(String.valueOf(session.getCheckOutTime()));
        } else {
            viewHolder.tvDateOut.setText("");
        }
		viewHolder.tvStep.setText(String.valueOf(session.getStep()));
		viewHolder.tvPreStatus.setText(String.valueOf(session.getPreStatus()));
		viewHolder.tvCurrentStatus.setText(String.valueOf(session.getStatus()));

		if (!session.isProcessing()) {
			viewHolder.btnContinue.setVisibility(View.GONE);
			viewHolder.btnSubmit.setVisibility(View.GONE);
		}

		// Xử lý upload container session khi click vào button Submit
		viewHolder.btnSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				App.getJobManager(context).addJobInBackground(new UploadSessionJob(context, session));
                NetworkClient_.getInstance_(context).uploadContainerSession(context,session);
			}
		});

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
