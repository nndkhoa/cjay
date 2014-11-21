package com.cloudjay.cjay.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.fragment.dialog.AddContainerDialog;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.session.update.AddSessionCommand;
import com.cloudjay.cjay.task.command.session.update.AddWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.get.SearchCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Tab search container
 */
@EFragment(R.layout.fragment_search)
@OptionsMenu(R.menu.search)
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

	//region ATTR
	@Bean
	DataCenter dataCenter;
	String containerID;
	String selectedId;

	@SystemService
	InputMethodManager inputMethodManager;

	private SessionAdapter mAdapter;
    public boolean isSearchResultDialogOpening = false;

	public SearchFragment() {
	}
	//endregion

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
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent
			) {
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

	//region VIEW INTERACTION
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
	void searchItemClicked(int position) {

		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);

        if (item.getLocalStep() == Step.EXPORTED.value) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getActivity().getResources().getText(R.string.warning_exported_container),
                    Toast.LENGTH_LONG).show();
            return;
        }

		dataCenter.add(new AddWorkingSessionCommand(getActivity(), item));
        hideMenuItems();

		// Open Wizard Activity
		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, item.getContainerId());
		intent.putExtra(WizardActivity.STEP_EXTRA, item.getLocalStep());
		startActivity(intent);

	}

	@ItemLongClick(R.id.lv_search_container)
	void searchItemLongClicked(int position) {
		lvSearch.setItemChecked(position, true);

		//Get session from position
		Session item = mAdapter.getItem(position);
		selectedId = item.getContainerId();
		getActivity().supportInvalidateOptionsMenu();
	}

	@OptionsItem(R.id.menu_export)
	void exportMenuItemClicked() {
		dataCenter.changeLocalStepAndForceExport(getActivity(), selectedId);
	}
	//endregion

	//region DIALOG
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

				// Kiểm tra nhập đủ 11 ký tự hay chưa
				if (editText.length() == 11) {
					// Kiểm tra containerId theo chuẩn ISO
					if (Utils.isContainerIdValid(containerId)) {
						// Nếu đúng chuẩn ISO, tạo mới container và mở Camera
						createContainerSession(containerId);
						openCamera(containerId);
					} else {
						showAddContainerDialog(containerId);
					}
				} else {
					showAddContainerDialog(containerId);
				}

				// Close dialog
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
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isSearchResultDialogOpening = false;
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
	//endregion

	@UiThread
	void showProgress(final boolean show) {
		llSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		llSearchResult.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	//region EVENT HANDLER

	/**
	 * Thông báo bắt đầu search từ Server
	 *
	 * @param event
	 */
	@UiThread
	public void onEvent(SearchAsyncStartedEvent event) {
		Toast.makeText(getActivity(), event.getStringEvent(), Toast.LENGTH_SHORT).show();
	}

	/**
	 * @param event
	 */
	@UiThread
	public void onEvent(ContainerSearchedEvent event) {

        boolean searchInImportFragment = event.isSearchInImportFragment();

        if (!searchInImportFragment) {
            showProgress(false);
            if (event.isFailed()) {

                llSearchResult.setVisibility(View.GONE);
                Utils.showCrouton(getActivity(), "Xảy ra sự cố với kết nối mạng \nXin thử lại sau", Style.ALERT);

            } else {
                List<Session> result = event.getSessions();
                mAdapter.clear();
                if (result.size() != 0) {
                    mAdapter.setData(result);
                    mAdapter.notifyDataSetChanged();

                } else {
                    if (!isSearchResultDialogOpening) {
                        isSearchResultDialogOpening = true;
                        showSearchResultDialog(containerID);
                    }
                }
            }
        }
	}
	//endregion

	@Override
	public void onResume() {
		super.onResume();
		llSearchResult.setVisibility(View.GONE);
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

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		boolean isDisplayed = !(TextUtils.isEmpty(selectedId));
		MenuItem item = menu.findItem(R.id.menu_export);
		item.setVisible(isDisplayed);

		super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Begin to search in background
	 */
	private void performSearch() {

		showProgress(true);

		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		String keyword = editText.getText().toString();

		// Start search in background
		containerID = keyword;
		dataCenter.add(new SearchCommand(getActivity(), keyword, false));
	}

	/**
	 * Create container session when containerId is valid ISO
	 *
	 * @param containerId
	 */
	public void createContainerSession(String containerId) {
		// Add new session to database
		String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.
				CJAY_DATETIME_FORMAT_NO_TIMEZONE);

		// Create container session
		Session session = new Session().withContainerId(containerId)
				.withLocalStep(Step.IMPORT.value)
				.withStep(Step.IMPORT.value)
				.withCheckInTime(currentTime)
				.withPreStatus(1);

		// Save normal session and working session.
		// add working session also post an event
		dataCenter.add(new AddSessionCommand(getActivity(), session));
		dataCenter.add(new AddWorkingSessionCommand(getActivity(), session));
	}

	/**
	 * Open camera and go to import step
	 */
	public void openCamera(String containerId) {

		Logger.Log("containerId: " + containerId);

		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity_.CONTAINER_ID_EXTRA, containerId);
		startActivity(intent);

		// Open camera activity
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerId);
		cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, "");
		cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
		cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.IMPORT.value);
		startActivity(cameraActivityIntent);
	}

	void hideMenuItems() {
		selectedId = "";
		getActivity().supportInvalidateOptionsMenu();
	}

}
