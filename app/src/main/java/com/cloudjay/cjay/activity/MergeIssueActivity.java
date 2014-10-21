package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.widget.ListView;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by thai on 21/10/2014.
 */
@EActivity(R.layout.activity_mergeissue)
public class MergeIssueActivity extends Activity {

    @ViewById(R.id.lv_merge_issue)
    ListView lvIssues;

    @AfterViews
    void setup(){

    }
}
