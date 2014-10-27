package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.LogUploadAdapter;
import com.cloudjay.cjay.model.LogUpload;
import com.cloudjay.cjay.util.CJayConstant;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by thai on 23/10/2014.
 */
@EActivity(R.layout.activity_logupload)
public class UploadLogActivity extends Activity {

    @ViewById(R.id.lv_logupload)
    ListView lvLogUpload;

    @ViewById(R.id.btn_search_log)
    Button btnSearch;

    @ViewById(R.id.et_search_log)
    EditText etSearch;

    @ViewById(R.id.tv_empty_list_working_log)
    TextView tvEmpty;

    @Bean
    DataCenter dataCenter;

    LogUploadAdapter mAdapter;

    @AfterViews
    void init(){
        mAdapter = new LogUploadAdapter(this,R.layout.item_upload_log);
        lvLogUpload.setAdapter(mAdapter);
        lvLogUpload.setEmptyView(tvEmpty);
        refresh();
    }

    @Click(R.id.btn_search_log)
    void btnSearchClicked(){
        if (!TextUtils.isEmpty(etSearch.getText())) {
            searchLog(etSearch.getText().toString());
        } else {
            etSearch.setError(getResources().getString(R.string.error_empty_search_string));
        }
    }

    private void searchLog(String key) {
        List<LogUpload> list = dataCenter.searchLogUpload(this.getApplicationContext(),key);
        updatedData(list);
    }

    @Background
    void refresh() {
        List<LogUpload> list = dataCenter.getListLogUpload(this.getApplicationContext(),
                CJayConstant.PREFIX_LOGUPLOAD);
        updatedData(list);
    }

    @UiThread
    public void updatedData(List<LogUpload> logUploadList) {
        mAdapter.clear();
        if (logUploadList != null) {
            for (LogUpload object : logUploadList) {
                mAdapter.insert(object, mAdapter.getCount());
            }
        }
        mAdapter.notifyDataSetChanged();
    }
}
