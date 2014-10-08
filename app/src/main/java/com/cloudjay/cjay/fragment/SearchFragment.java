package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.fragment.dialog.AddContainerDialog;
import com.cloudjay.cjay.fragment.dialog.AddContainerDialog_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tab search container
 */
@EFragment(R.layout.fragment_search)
public class SearchFragment extends Fragment {

	//region VIEW
	@ViewById(R.id.btn_search)
	Button btnSearch;

	@ViewById(R.id.et_search)
	EditText etSearch;

	@ViewById(R.id.lv_search_container)
	ListView lvSearch;

	@ViewById(R.id.ll_search_progress)
	LinearLayout llSearchProgress;
	//endregion

	Pattern pattern = Pattern.compile("^[a-zA-Z]{4}");

	@Bean
	DataCenter dataCenter;
	String containerID;

	private SessionAdapter mAdapter;

	public SearchFragment() {
	}

	@UiThread
	void showProgress(final boolean show) {
		llSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		lvSearch.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@Click(R.id.btn_search)
	void buttonSearchClicked() {
		showProgress(true);
		String keyword = etSearch.getText().toString();

		if (TextUtils.isEmpty(keyword)) {
			etSearch.setError(getString(R.string.dialog_container_id_required));

		} else if (isGateRole() && !Utils.simpleValid(keyword)) {

			// Note: if current user is Gate then we need to validate full container ID
			etSearch.setError(getString(R.string.dialog_container_id_invalid));

		} else {

			// Start search in background
			dataCenter.search(getActivity(), keyword);
		}
	}

	@AfterViews
	void doAfterViews() {
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		lvSearch.setAdapter(mAdapter);
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (s.length() == 0) {
					etSearch.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (isGateRole()) {
					Matcher matcher = pattern.matcher(s);
					if (s.length() < 4) {
						if (etSearch.getInputType() != InputType.TYPE_CLASS_TEXT) {
							etSearch.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
						}
					} else if (matcher.matches()) {
						if (etSearch.getInputType() != InputType.TYPE_CLASS_NUMBER) {
							etSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
						}
					}
				} else {
					etSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
				}

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	@UiThread
	public void onEvent(ContainerSearchedEvent event) {

		showProgress(false);
		List<Session> result = event.getSessions();
		if (result != null) {

			mAdapter.clear();
			mAdapter.addAll(result);
			mAdapter.notifyDataSetChanged();

			if (result.size() == 0) {
				// TODO: Show dialog alert that keyword was not found
				showAddContainerDialog(containerID);
			}
		}
	}

	@ItemClick(R.id.lv_search_container)
	void searchListViewItemClicked(int position) {
		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra("", item.getContainerId());
		startActivity(intent);
	}

	private void showAddContainerDialog(String containerID) {
		FragmentManager fragmentManager = getChildFragmentManager();
		AddContainerDialog addContainerDialog = AddContainerDialog_.builder().containerID(containerID).build();
		addContainerDialog.show(fragmentManager, "fragment_addcontainer");
	}

	// TODO: @thai cần phải refactor lại chỗ này, add Enum và tạo hàm trong Utils.java
	private boolean isGateRole() {
		if (PreferencesUtil.getPrefsValue(getActivity(), PreferencesUtil.PREF_USER_ROLE).equals("6")) {
			return true;
		} else {
			return false;
		}
	}
}
