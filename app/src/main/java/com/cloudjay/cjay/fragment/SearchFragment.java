package com.cloudjay.cjay.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.SearchAsyncStartedEvent;
import com.cloudjay.cjay.fragment.dialog.AddContainerDialog;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Tab search container
 */
@EFragment(R.layout.fragment_search)
public class SearchFragment extends Fragment {

	//region VIEW
	@ViewById(R.id.btn_search)
	ImageButton btnSearch;

	@ViewById(R.id.et_search)
	EditText editText;

	@ViewById(R.id.lv_search_container)
	ListView lvSearch;

	@ViewById(R.id.ll_search_progress)
	LinearLayout llSearchProgress;

	@ViewById(R.id.ll_search_result)
	LinearLayout llSearchResult;

	@ViewById(android.R.id.empty)
	TextView tvEmptyView;
	//endregion

	@Bean
	DataCenter dataCenter;
	String containerID;

	private SessionAdapter mAdapter;

	public SearchFragment() {
	}

	/**
	 * 1. Setup search EditText
	 * 2. Setup Adapter and ListView result
	 */
	@AfterViews
	void doAfterViews() {

		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		lvSearch.setEmptyView(tvEmptyView);
		lvSearch.setAdapter(mAdapter);

		//Set input type for role
		Utils.setupEditText(editText);

		// Set action when click `Enter` key
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (TextUtils.isEmpty(editText.getText())) {
					editText.setError(getResources().getString(R.string.error_empty_search_string));
					return true;
				}
				if (i == EditorInfo.IME_ACTION_SEARCH) {
					performSearch();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * User tiến hành tìm kiếm container session
	 */
	@Click(R.id.btn_search)
	void buttonSearchClicked() {
		if (!TextUtils.isEmpty(editText.getText())) {
			performSearch();
		} else {
			editText.setError(getResources().getString(R.string.error_empty_search_string));
		}
	}

	/**
	 * Click vào list item, mở activity với step tương ứng của container
	 *
	 * @param position
	 */
	@ItemClick(R.id.lv_search_container)
	void searchListViewItemClicked(int position) {

		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, item.getContainerId());
		intent.putExtra(WizardActivity.STEP_EXTRA, item.getStep());
		startActivity(intent);
	}

	/**
	 * Hiển thị kết quả không tìm thấy container
	 *
	 * @param containerId
	 */
	private void showSearchResultDialog(final String containerId) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_search_container_title);
		builder.setMessage("Container ID với từ khóa " + containerId + " chưa được nhập vào hệ thống");

		builder.setPositiveButton("Bỏ qua", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});

		// Hiển thị dialog tạo mới container
		builder.setNegativeButton("Tạo mới", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				showAddContainerDialog(containerId);
				dialogInterface.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {

				// Set background and text color for confirm button
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
						.setTextColor(getResources().getColor(android.R.color.white));
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
						.setBackgroundResource(R.drawable.btn_green_selector);
			}
		});
		dialog.show();
	}

	/**
	 * Hiển thị dialog tạo mới container
	 *
	 * @param containerId
	 */
	private void showAddContainerDialog(String containerId) {

		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog_ = com.cloudjay.cjay.fragment.dialog.AddContainerDialog_
				.builder().containerId(containerId).build();
		addContainerDialog_.show(fragmentManager, "fragment_addcontainer");
	}


	/**
	 * Begin to search in background
	 */
	private void performSearch() {
		showProgress(true);
		String keyword = editText.getText().toString();

		// Start search in background
		containerID = keyword;
		dataCenter.search(getActivity(), keyword);
	}

	@UiThread
	void showProgress(final boolean show) {
		llSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		llSearchResult.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	/**
	 * @param event
	 */
	@UiThread
	public void onEvent(ContainerSearchedEvent event) {

		showProgress(false);
		List<Session> result = event.getSessions();
		mAdapter.clear();

		if (result.size() != 0) {
			mAdapter.addAll(result);
			mAdapter.notifyDataSetChanged();
		} else {
			showSearchResultDialog(containerID);
		}
	}

	/**
	 * Bắt đầu search từ Server
	 *
	 * @param event
	 */
	@UiThread
	public void onEvent(SearchAsyncStartedEvent event) {
		Toast.makeText(getActivity(), event.getStringEvent(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
