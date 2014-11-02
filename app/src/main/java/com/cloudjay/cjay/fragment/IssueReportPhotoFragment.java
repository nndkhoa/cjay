package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.nostra13.universalimageloader.core.ImageLoader;

@EFragment(R.layout.fragment_report_issue_photo)
public class IssueReportPhotoFragment extends IssueReportFragment {
	private AuditImage mAuditImage;
	private ImageLoader mImageLoader;

	@ViewById(R.id.item_picture)
	ImageView mImageView;

	@AfterViews
	void afterViews() {
		mImageLoader = ImageLoader.getInstance();
		try {
			mImageLoader.displayImage(mAuditImage.getUrl(), mImageView);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void hideKeyboard() {
	}

	public void setCJayImage(AuditImage auditImage) {
		mAuditImage = auditImage;
	}

	@Override
	public void setAuditItem(AuditItem auditItem) {
	}

	@Override
	public void showKeyboard() {
	}

	@Override
	public boolean validateAndSaveData() {
		return true;
	}
}