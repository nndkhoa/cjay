package com.cloudjay.cjay.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.nostra13.universalimageloader.core.ImageLoader;

@EFragment(R.layout.fragment_issue_photo)
public class IssueReportPhotoFragment extends IssueReportFragment {
	private CJayImage mCJayImage;
	private ImageLoader mImageLoader;

	@ViewById(R.id.item_picture)
	ImageView mImageView;

	@AfterViews
	void afterViews() {
		mImageLoader = ImageLoader.getInstance();
		try {
			mImageLoader.displayImage(mCJayImage.getUri(), mImageView);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void hideKeyboard() {
	}

	public void setCJayImage(CJayImage cJayImage) {
		mCJayImage = cJayImage;
	}

	@Override
	public void setIssue(Issue issue) {
	}

	@Override
	public void showKeyboard() {
	}

	@Override
	public boolean validateAndSaveData() {
		return true;
	}
}