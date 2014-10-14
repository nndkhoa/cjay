package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.widget.ExpandableListView;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_photo_expandablelistview)
public class PhotoExpandableListViewActivity extends Activity {

    @ViewById(R.id.lv_images_expandable)
    ExpandableListView lvImagesExpandable;

    @AfterViews
    void setUp() {

    }

}
