package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.jobqueue.UploadAuditItemJob;
import com.cloudjay.cjay.task.jobqueue.UploadCompleteAuditJob;
import com.cloudjay.cjay.task.jobqueue.UploadCompleteRepairJob;
import com.cloudjay.cjay.task.jobqueue.UploadExportSessionJob;
import com.cloudjay.cjay.task.jobqueue.UploadImageJob;
import com.cloudjay.cjay.task.jobqueue.UploadSessionJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.path.android.jobqueue.JobManager;

import java.util.List;

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
		ImageButton btnUploadStatus;
		TextView tvUploadStatus;
	}

	/**
	 * 1. Inflate view into View Holder
	 * 2. Set data to View
	 *
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Session session = getItem(position);

		// Inflate UI
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
			viewHolder.btnUploadStatus = (ImageButton) convertView.findViewById(R.id.iv_upload_result);
			viewHolder.tvUploadStatus = (TextView) convertView.findViewById(R.id.tv_upload_status);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// Set data to view
		ImageLoader.getInstance().displayImage(session.getGateImages().get(0).getUrl(), viewHolder.ivContainer);
		viewHolder.tvContainerId.setText(session.getContainerId());
		viewHolder.tvTotalPhotoUpload.setText(session.getTotalImage() + "");
		viewHolder.tvCurrentPhotoUpload.setText(session.getUploadedImage() + "");

		UploadStatus status = UploadStatus.values()[session.getUploadStatus()];
		Logger.Log(session.getContainerId() + " -> Upload Status: " + status.name());
		switch (status) {

			//
			case COMPLETE:
				viewHolder.btnUploadStatus.setVisibility(View.VISIBLE);
				viewHolder.pbUpLoading.setVisibility(View.GONE);
				viewHolder.btnUploadStatus.setImageResource(R.drawable.ic_success);
				viewHolder.tvUploadStatus.setText("Hoàn tất tải lên");
				viewHolder.tvUploadStatus.setVisibility(View.VISIBLE);
				break;

			case ERROR:
				viewHolder.btnUploadStatus.setImageResource(R.drawable.ic_error);
				viewHolder.btnUploadStatus.setVisibility(View.VISIBLE);
				viewHolder.pbUpLoading.setVisibility(View.GONE);
				viewHolder.tvUploadStatus.setVisibility(View.VISIBLE);
				break;

			case UPLOADING:
			default:
				if (session.canRetry()) {

					if (NetworkHelper.isConnected(context)) {
						viewHolder.pbUpLoading.setVisibility(View.VISIBLE);
						viewHolder.pbUpLoading.setProgress(100 * session.getUploadedImage() / session.getTotalImage());
					} else {

						// Display icon Retry
						viewHolder.btnUploadStatus.setImageResource(R.drawable.ic_refresh);
						viewHolder.btnUploadStatus.setClickable(true);
						viewHolder.btnUploadStatus.setVisibility(View.VISIBLE);
						viewHolder.pbUpLoading.setVisibility(View.GONE);
						viewHolder.tvUploadStatus.setVisibility(View.VISIBLE);

						// Set event to retry button
						viewHolder.btnUploadStatus.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								uploadCurrentStep(session);
							}
						});
					}

				}
				break;
		}
		return convertView;
	}

	private void uploadCurrentStep(Session session) {
		JobManager jobManager = App.getJobManager();
		Step step = Step.values()[((int) session.getStep())];

		//Retry upload base on step
		switch (step) {
			//In step import check all image, upload all error image then upload session
			case IMPORT:
				for (GateImage image : session.getGateImages()) {
					if (image.getType() == ImageType.IMPORT.value && image.getUploadStatus() != UploadStatus.ERROR.value) {
						jobManager.addJobInBackground(new UploadImageJob(image.getUrl(), image.getName(), session.getContainerId(), ImageType.IMPORT));
					}
					;
				}
				jobManager.addJobInBackground(new UploadSessionJob(session));

				// In step audit check all image of item, upload all error image then upload error audit item => complete audit
			case AUDIT:
				for (AuditItem item : session.getAuditItems()) {
					if (item.getUploadStatus() == UploadStatus.ERROR.value) {
						for (AuditImage auditImage : item.getAuditImages()) {
							if (auditImage.getUploadStatus() != UploadStatus.ERROR.value && auditImage.getType() == ImageType.AUDIT.value) {
								jobManager.addJobInBackground(new UploadImageJob(auditImage.getUrl(), auditImage.getName(), session.getContainerId(), ImageType.AUDIT));
							}
						}
					}
					jobManager.addJobInBackground(new UploadAuditItemJob(session.getContainerId(), item));
				}
				jobManager.addJobInBackground(new UploadCompleteAuditJob(session.getContainerId()));

				// In step repaired check all image of item, upload all error image then upload error repaired item => complete repair
			case REPAIR:
				for (AuditItem item : session.getAuditItems()) {
					if (item.getUploadStatus() == UploadStatus.ERROR.value) {
						for (AuditImage auditImage : item.getAuditImages()) {
							if (auditImage.getUploadStatus() != UploadStatus.ERROR.value && auditImage.getType() == ImageType.REPAIRED.value) {
								jobManager.addJobInBackground(new UploadImageJob(auditImage.getUrl(), auditImage.getName(), session.getContainerId(), ImageType.REPAIRED));
							}
						}
					}
					jobManager.addJobInBackground(new UploadCompleteRepairJob(session.getContainerId()));
				}
				jobManager.addJobInBackground(new UploadCompleteRepairJob(session.getContainerId()));

				//In step export check all image, upload all error image then upload session
			case EXPORT:
				for (GateImage image : session.getGateImages()) {
					if (image.getType() == ImageType.EXPORT.value && image.getUploadStatus() != UploadStatus.ERROR.value) {
						jobManager.addJobInBackground(new UploadImageJob(image.getUrl(), image.getName(), session.getContainerId(), ImageType.EXPORT));
					}
					;
				}
				jobManager.addJobInBackground(new UploadExportSessionJob(session));
		}
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
