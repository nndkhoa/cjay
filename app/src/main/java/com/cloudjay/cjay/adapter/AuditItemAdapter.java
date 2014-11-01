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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.MergeIssueActivity_;
import com.cloudjay.cjay.fragment.CameraFragment;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.task.jobqueue.UploadAuditItemJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.path.android.jobqueue.JobManager;
import com.snappydb.SnappydbException;

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
	private String operatorCode;
	private String mAuditItemUUID;
	private String mComponentCode;

	public AuditItemAdapter(Context context, int resource, String containerId, String operatorCode) {
		super(context, resource);
		this.mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutResId = resource;
		this.containerId = containerId;
		this.operatorCode = operatorCode;
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
		public Button btnEdit;
		public TextView tvIssueStatus;
		public LinearLayout llIssueImageView;
		public LinearLayout llIssueDetails;
		public ImageView ivUploading;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {

		final AuditItem auditItem = getItem(i);

		final ViewHolder holder;
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
			holder.tvIssueStatus = (TextView) view.findViewById(R.id.tv_issue_status);

			holder.btnUpload = (Button) view.findViewById(R.id.btn_upload_pending);
			holder.btnReport = (Button) view.findViewById(R.id.btn_report_pending);
			holder.btnRepair = (Button) view.findViewById(R.id.btn_repair_pending);
			holder.btnEdit = (Button) view.findViewById(R.id.btn_edit_pending);
			holder.ivUploading = (ImageView) view.findViewById(R.id.iv_uploading);

			holder.llIssueDetails = (LinearLayout) view.findViewById(R.id.ll_issue_details);
			holder.llIssueImageView = (LinearLayout) view.findViewById(R.id.ll_issue_imageview);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// Lỗi nào chưa giám dịnh thì hiện hinh`, lỗi nào đã giám định roi thì hiện chi tiết lỗi
		if (auditItem.getAudited() == true) {

            UploadStatus status = UploadStatus.values()[((int) auditItem.getUploadStatus())];

			holder.llIssueImageView.setVisibility(View.GONE);
			holder.llIssueDetails.setVisibility(View.VISIBLE);

            switch (status) {
                case UPLOADING:
                    holder.btnUpload.setVisibility(View.GONE);
                    holder.btnEdit.setVisibility(View.GONE);
                    holder.ivUploading.setVisibility(View.VISIBLE);
                    break;

                case COMPLETE:
                    holder.btnUpload.setVisibility(View.GONE);
                    holder.btnEdit.setVisibility(View.GONE);
                    holder.ivUploading.setVisibility(View.GONE);
                    holder.btnRepair.setVisibility(View.VISIBLE);
                    break;

                case ERROR:
                    // TODO: show retry button

                default:
                    holder.btnUpload.setVisibility(View.VISIBLE);
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.ivUploading.setVisibility(View.GONE);
                    holder.btnRepair.setVisibility(View.GONE);
            }

			//Set detail textviews
			holder.tvCodeComponent.setText(auditItem.getComponentCode());
			holder.tvCodeIssue.setText(auditItem.getDamageCode());
			holder.tvCodeLocation.setText(auditItem.getLocationCode());
			holder.tvDimension.setText("Dài " + auditItem.getHeight() + "," + " Rộng " + auditItem.getLength());
			holder.tvCodeRepair.setText(auditItem.getRepairCode());
			holder.tvCount.setText(auditItem.getQuantity() + "");

			if (!auditItem.isIsAllowed()) {
				holder.tvIssueStatus.setText("Cấm sửa");
			} else {
				if (!auditItem.getApproved()) {
					holder.tvIssueStatus.setText(mContext.getResources().getString(R.string.issue_unapproved));
				} else {
					holder.tvIssueStatus.setText(mContext.getResources().getString(R.string.issue_approved));
				}
			}

			holder.btnRepair.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mAuditItemUUID = auditItem.getAuditItemUUID();
					mComponentCode = auditItem.getComponentCode();
					if (!auditItem.isIsAllowed()) {
						// Nếu lỗi này cấm sửa, hiện dialog cấm sửa
						showPreventRepairDialog();
					} else {
						if (!auditItem.getApproved()) {
							// Show repair dialog
							showRepairDiaglog();
						} else {
							// Open camera activity to take repair image
							openCamera();
						}
					}
				}
			});

			holder.btnUpload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//1. Update upload status
                    auditItem.setUploadStatus(UploadStatus.UPLOADING.value);
                    DataCenter_.getInstance_(mContext).changeUploadState(mContext,
                            containerId, auditItem);
                    notifyDataSetChanged();

					//2. Add container session to upload queue
					JobManager jobManager = App.getJobManager();
					jobManager.addJob(new UploadAuditItemJob(containerId, auditItem.getAuditItemUUID()));
				}
			});

		} else {
			holder.llIssueImageView.setVisibility(View.VISIBLE);
			holder.llIssueDetails.setVisibility(View.GONE);

			if (auditItem.getAuditImages() != null && auditItem.getAuditImages().size() != 0) {
				if (auditItem.getAuditImages().get(0) != null) {
					auditImage = auditItem.getAuditImages().get(0);
					ImageAware imageAware = new ImageViewAware(holder.ivAuditImage, false);
					ImageLoader.getInstance().displayImage(auditImage.getUrl(),
							imageAware);
				}
			}

			holder.btnReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showApproveDiaglog(auditItem);
                }
            });
		}

		return view;
	}

	private void showRepairDiaglog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Alert");
		builder.setMessage("Lỗi này chưa được duyệt. Sửa luôn?");

		builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});

		builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// Open camera activity to take repair image
				Logger.Log("mComponentCode: " + mComponentCode);
				openCamera();
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

				// Set background and text color for BUTTON_POSITIVE
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
						.setTextColor(mContext.getResources().getColor(android.R.color.white));
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
						.setBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));
			}
		});
		dialog.show();

	}

	void showApproveDiaglog(final AuditItem item) {

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Alert");
		builder.setMessage("Lỗi này đã được báo cáo chưa?");

		builder.setPositiveButton("Chưa", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();

				// TODO: @vule: open ReportIssueActivity
			}
		});

		builder.setNegativeButton("Rồi", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

				Intent intent = new Intent(mContext, MergeIssueActivity_.class);
				intent.putExtra(MergeIssueActivity_.CONTAINER_ID_EXTRA, containerId);
				intent.putExtra(MergeIssueActivity_.AUDIT_IMAGE_EXTRA, auditImage.getAuditImageUUID());
				intent.putExtra(MergeIssueActivity_.AUDIT_ITEM_REMOVE_UUID, item.getAuditItemUUID());

				mContext.startActivity(intent);
			}
		});

		builder.setNeutralButton("Vệ sinh", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				try {

					// change status audit item to water wash
					DataCenter_.getInstance_(mContext).setWaterWashType(mContext, item, containerId);
					dialogInterface.dismiss();

				} catch (SnappydbException e) {
					// TODO: Handle exception
					e.printStackTrace();
				}
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
						.setBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));
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

	void openCamera() {
		Intent cameraActivityIntent = new Intent(mContext, CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerId);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.REPAIRED.value);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.REPAIR.value);
		cameraActivityIntent.putExtra(CameraFragment.AUDIT_ITEM_UUID_EXTRA, mAuditItemUUID);
		mContext.startActivity(cameraActivityIntent);
	}

	void showPreventRepairDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Alert");
		builder.setMessage("Lỗi này không được sửa");

		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
