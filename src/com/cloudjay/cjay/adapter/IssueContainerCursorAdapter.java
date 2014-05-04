package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.ContainerState;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.CheckableImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

public class IssueContainerCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {
		public TextView containerIdView;
		public TextView containerOwnerView;
		public TextView importDateView;
		public TextView containerIssuesView;
		public ImageView itemPictureView;
		public ImageView validationImageView;
		public CheckableImageView checkableImageView;
		public ImageView warningImageView;
	}

	private int mLayout;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private boolean mAvCheckable;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public IssueContainerCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public IssueContainerCursorAdapter(Context context, int layout, Cursor c, int flags) {
		this(context, layout, c, flags, false);
	}

	public IssueContainerCursorAdapter(Context context, int layout, Cursor c, int flags, boolean avCheckable) {
		super(context, c, flags);
		this.mLayout = layout;
		mInflater = LayoutInflater.from(context);
		mCursor = c;
		mImageLoader = ImageLoader.getInstance();
		mAvCheckable = avCheckable;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (cursor == null) {
			Logger.e("-----> BUG");
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			Logger.Log("Holder inside bindView is NULL");

			holder = new ViewHolder();
			holder.containerIdView = (TextView) view.findViewById(R.id.feed_item_container_id);
			holder.containerOwnerView = (TextView) view.findViewById(R.id.feed_item_container_owner);
			holder.importDateView = (TextView) view.findViewById(R.id.feed_item_container_import_date);
			holder.containerIssuesView = (TextView) view.findViewById(R.id.feed_item_container_issues);
			holder.itemPictureView = (ImageView) view.findViewById(R.id.feed_item_picture);
			holder.validationImageView = (ImageView) view.findViewById(R.id.feed_item_validator);
			holder.checkableImageView = (CheckableImageView) view.findViewById(R.id.check_button);
			holder.warningImageView = (ImageView) view.findViewById(R.id.feed_item_warning);
			view.setTag(holder);
		}

		// get data from cursor and bind to holder
		String importDate = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_CHECK_IN_TIME));
		importDate = StringHelper.getRelativeDate(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, importDate);
		holder.importDateView.setText(importDate);

		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		holder.containerIdView.setText(containerId);

		String operator = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_NAME));
		holder.containerOwnerView.setText(operator);

		String issueCount = cursor.getString(cursor.getColumnIndexOrThrow("issue_count"));
		holder.containerIssuesView.setText(issueCount);

		String url = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));

		// if (!TextUtils.isEmpty(url) && !url.equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {

		if (!TextUtils.isEmpty(url)
				&& !url.matches("^https://storage\\.googleapis\\.com/storage-cjay\\.cloudjay\\.com/\\s+$")) {
			mImageLoader.displayImage(url, holder.itemPictureView);
		} else {
			holder.itemPictureView.setImageResource(R.drawable.ic_app);
		}

		if (holder.validationImageView != null) {

			boolean isValidForUpload = false;
			if (cursor.getColumnIndex("auditor_image_no_issue_count") >= 0
					&& cursor.getColumnIndex("invalid_issue_count") >= 0 && cursor.getColumnIndex("issue_count") >= 0) {
				int imageWithoutIssueCount = cursor.getInt(cursor.getColumnIndexOrThrow("auditor_image_no_issue_count"));
				int invalidIssueCount = cursor.getInt(cursor.getColumnIndexOrThrow("invalid_issue_count"));
				int validIssueCount = cursor.getInt(cursor.getColumnIndexOrThrow("issue_count"));
				if (imageWithoutIssueCount > 1 || validIssueCount == 0 || invalidIssueCount > 0) {
					isValidForUpload = false;
				} else {
					isValidForUpload = true;
				}
			}

			if (cursor.getColumnIndex("fixed_issue_count") >= 0 && cursor.getColumnIndex("issue_count") >= 0) {
				int fixedIssueCount = cursor.getInt(cursor.getColumnIndexOrThrow("fixed_issue_count"));
				int validIssueCount = cursor.getInt(cursor.getColumnIndexOrThrow("issue_count"));
				if (fixedIssueCount < validIssueCount || validIssueCount == 0) {
					isValidForUpload = false;
				} else {
					isValidForUpload = true;
				}

				ContainerState state = ContainerState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_SERVER_STATE))];
				if (state == ContainerState.REPAIRING) {
					holder.warningImageView.setVisibility(View.GONE);
					view.setEnabled(false);
				} else {
					holder.warningImageView.setVisibility(View.VISIBLE);
					view.setEnabled(true);
				}

			}

			if (!isValidForUpload) {
				holder.validationImageView.setVisibility(View.INVISIBLE);
			} else {
				holder.validationImageView.setVisibility(View.VISIBLE);
			}
		}

		if (holder.checkableImageView != null) {

			if (mAvCheckable && cursor.getColumnIndex(ContainerSession.FIELD_AVAILABLE) > 0) {
				final Context ctx = context;
				final String uuid = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_UUID));

				boolean available = Boolean.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_AVAILABLE)));
				holder.checkableImageView.setVisibility(View.VISIBLE);
				holder.checkableImageView.setChecked(available == true);
				holder.checkableImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						CheckableImageView checkButton = (CheckableImageView) v;
						checkButton.toggle();
						QueryHelper.update(	ctx, "container_session", ContainerSession.FIELD_AVAILABLE,
											String.valueOf(checkButton.isChecked()), ContainerSession.FIELD_UUID
													+ " = " + Utils.sqlString(uuid));
						EventBus.getDefault().post(new ContainerSessionChangedEvent());

					}

				});
			} else {
				holder.checkableImageView.setVisibility(View.GONE);
			}
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mInflater.inflate(mLayout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.containerIdView = (TextView) v.findViewById(R.id.feed_item_container_id);
		holder.containerOwnerView = (TextView) v.findViewById(R.id.feed_item_container_owner);
		holder.importDateView = (TextView) v.findViewById(R.id.feed_item_container_import_date);
		holder.containerIssuesView = (TextView) v.findViewById(R.id.feed_item_container_issues);
		holder.itemPictureView = (ImageView) v.findViewById(R.id.feed_item_picture);
		holder.validationImageView = (ImageView) v.findViewById(R.id.feed_item_validator);
		holder.checkableImageView = (CheckableImageView) v.findViewById(R.id.check_button);
		holder.warningImageView = (ImageView) v.findViewById(R.id.feed_item_warning);
		v.setTag(holder);

		return v;
	}

}
