package com.cloudjay.cjay.view;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.RainyModeImageAdapter;
import com.cloudjay.cjay.model.GateImage;

/**
 * Created by nambv on 17/10/2014.
 */
public class CheckablePhotoGridItemLayout extends CheckableFrameLayout {

    private final ImageView mImageView;
    private final CheckableImageView mButton;

    private GateImageAdapter mParentAdapter;
    private GateImage mCJayImage;

    private RainyModeImageAdapter mRainyModeAdapter;
    private String mRainyImage;

    private boolean rainyMode;

    public CheckablePhotoGridItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        rainyMode = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getBoolean(context.getString(R.string.pref_key_enable_temporary_fragment_checkbox),
                        false);

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

        if (mParentAdapter != null) {
            if (isChecked()) {
                mParentAdapter.addCheckedCJayImageUrl(mCJayImage);
            } else {
                mParentAdapter.removeCheckedCJayImageUrl(mCJayImage);
            }
        }

        if (mRainyModeAdapter != null) {
            if (isChecked()) {
                mRainyModeAdapter.addCheckedImageUrl(mRainyImage);
            } else {
                mRainyModeAdapter.removeCheckedImageUrl(mRainyImage);
            }
        }
    }

    @Override
    public void setChecked(final boolean b) {
        super.setChecked(b);
        if (View.VISIBLE == mButton.getVisibility()) {
            mButton.setChecked(b);
        }
    }

    public void setCJayImage(GateImage cJayImageUuid) {
        mCJayImage = cJayImageUuid;
    }

    public GateImage getCJayImage() {
        return mCJayImage;
    }

    public void setParentAdapter(GateImageAdapter parentAdapter) {
        mParentAdapter = parentAdapter;
    }

    public GateImageAdapter getParentAdapter() {
        return mParentAdapter;
    }

    public void setRainyImage(String rainyImage) {
        mRainyImage = rainyImage;
    }

    public void setRainyModeAdapter(RainyModeImageAdapter adapter) {
        mRainyModeAdapter = adapter;
    }
}