package com.cloudjay.cjay.activity;

import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.LogAdapter;
import com.cloudjay.cjay.model.LogItem;
import com.cloudjay.cjay.util.CJayConstant;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_log)
public class LogActivity extends BaseActivity {

	@ViewById(R.id.lv_log)
	ListView lvLog;

	@ViewById(R.id.btn_search_log)
	ImageButton btnSearch;

	@ViewById(R.id.et_search_log)
	EditText etSearch;

	@ViewById(R.id.tv_empty_list_working_log)
	TextView tvEmpty;

	@Bean
	DataCenter dataCenter;

	@SystemService
	InputMethodManager inputMethodManager;

	LogAdapter mAdapter;

	@AfterViews
	void init() {
		mAdapter = new LogAdapter(this, R.layout.item_upload_log);
		lvLog.setAdapter(mAdapter);
		lvLog.setEmptyView(tvEmpty);
		refresh();
	}

	@Click(R.id.btn_search_log)
	void btnSearchClicked() {
		performSearch();
	}

	private void performSearch() {
		inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		if (!TextUtils.isEmpty(etSearch.getText())) {
			searchLog(etSearch.getText().toString());
			etSearch.setText("");
		} else {
			etSearch.setError(getResources().getString(R.string.error_empty_search_string));
		}
	}

	private void searchLog(String key) {
		List<LogItem> list = dataCenter.searchLog(this.getApplicationContext(), key);
		updatedData(list);
	}

	@Background
	void refresh() {
		List<LogItem> list = dataCenter.getListLogItems(this.getApplicationContext(),
				CJayConstant.PREFIX_LOG);
		updatedData(list);
	}

	@UiThread
	public void updatedData(List<LogItem> logUploadList) {
		mAdapter.clear();
		if (logUploadList != null) {
			for (LogItem object : logUploadList) {
				mAdapter.insert(object, mAdapter.getCount());
			}
		}
		mAdapter.notifyDataSetChanged();
	}
}
