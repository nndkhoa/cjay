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
		mImageLoader.displayImage(mCJayImage.getUri(), mImageView);
	}

	@Override
	public void setIssue(Issue issue) {
	}
	
	public void setCJayImage(CJayImage cJayImage) {
		mCJayImage = cJayImage;
	}

	@Override
	public void validateAndSaveData() {
	}

	@Override
	public void showKeyboard() {
	}
	
	@Override
	public void hideKeyboard() {
	}
}