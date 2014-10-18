package com.cloudjay.cjay.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImageAdapter;

/**
 * Created by nambv on 17/10/2014.
 */
public class CheckablePhotoGridItemLayout extends CheckableFrameLayout {

    private final ImageView mImageView;
    private final CheckableImageView mButton;

    private GateImageAdapter mParentAdapter;
    private String mCJayImageUrl;

    public CheckablePhotoGridItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.item_gridview_photo_multi_select,
                this);

        mImageView = (ImageView) findViewById(R.id.iv_image);
        mButton = (CheckableImageView) findViewById(R.id.cb_select);
//		mButton.setOnClickListener(this);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public void setShowCheckbox(boolean visible) {
        if (visible) {
            mButton.setVisibility(View.VISIBLE);
//			mButton.setOnClickListener(this);
        } else {
            mButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void toggle() {
        super.toggle();

        if (isChecked()) {
            mParentAdapter.addCheckedCJayImageUrl(mCJayImageUrl);
        } else {
            mParentAdapter.removeCheckedCJayImageUrl(mCJayImageUrl);
        }
    }

    @Override
    public void setChecked(final boolean b) {
        super.setChecked(b);
        if (View.VISIBLE == mButton.getVisibility()) {
            mButton.setChecked(b);
        }
    }

    public void setCJayImageUrl(String cJayImageUuid) {
        mCJayImageUrl = cJayImageUuid;
    }

    public String getCJayImageUrl() {
        return mCJayImageUrl;
    }

    public void setParentAdapter(GateImageAdapter parentAdapter) {
        mParentAdapter = parentAdapter;
    }

    public GateImageAdapter getParentAdapter() {
        return mParentAdapter;
    }
}
