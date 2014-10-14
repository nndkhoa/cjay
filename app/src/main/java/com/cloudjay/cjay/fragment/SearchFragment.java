package com.cloudjay.cjay.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.cloudjay.cjay.event.BeginSearchOnServerEvent;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.fragment.dialog.AddContainerDialog;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Role;

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
	EditText etSearch;

	@ViewById(R.id.lv_search_container)
	ListView lvSearch;

	@ViewById(R.id.ll_search_progress)
	LinearLayout llSearchProgress;

	@ViewById(R.id.ll_search_result)
	LinearLayout llSearchResult;

    @ViewById(android.R.id.empty)
    TextView tvEmptyView;
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
		llSearchResult.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@Click(R.id.btn_search)
	void buttonSearchClicked() {
        if (!TextUtils.isEmpty(etSearch.getText())) {
            performSearch();
        } else {
            etSearch.setError(getResources().getString(R.string.error_empty_search_string));
        }
	}

	@AfterViews
	void doAfterViews() {
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
        lvSearch.setEmptyView(tvEmptyView);
		lvSearch.setAdapter(mAdapter);
		//Set input type for role
		setKeyboardBasedOnRole();
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (s.length() == 0) {

				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if ((Utils.getRole(getActivity())) == Role.GATE.getValue()) {
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

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(etSearch.getText())) {
                        performSearch();
                    } else {
                        etSearch.setError(getResources().getString(R.string.error_empty_search_string));
                    }
                    return true;
                }

                return false;
            }
        });
	}

	private void setKeyboardBasedOnRole() {
		if ((Utils.getRole(getActivity())) == Role.GATE.getValue()) {
			etSearch.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		} else {
			etSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
	}

	@UiThread
	public void onEvent(ContainerSearchedEvent event) {

		Logger.Log("onEvent ContainerSearchedEvent");

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

	@ItemClick(R.id.lv_search_container)
	void searchListViewItemClicked(int position) {

		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, item.getContainerId());
		intent.putExtra(WizardActivity.STEP_EXTRA, item.getStep());
		startActivity(intent);
	}

	private void showSearchResultDialog(final String containerId) {
		Logger.Log("Result is null");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_search_container_title);
		builder.setMessage("Container ID với từ khóa " + containerId + " chưa được nhập vào hệ thống");
		builder.setPositiveButton("Bỏ qua", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
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

	private void showAddContainerDialog(String containerId) {
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog_ = com.cloudjay.cjay.fragment.dialog.AddContainerDialog_
				.builder().containerId(containerId).build();
		addContainerDialog_.show(fragmentManager, "fragment_addcontainer");
	}

    private void performSearch() {
        showProgress(true);
        String keyword = etSearch.getText().toString();

        // Start search in background
        containerID = keyword;
        dataCenter.search(getActivity(), keyword);
    }

    @UiThread
    public void onEvent(BeginSearchOnServerEvent event) {
        Toast.makeText(getActivity(), event.getStringEvent(), Toast.LENGTH_SHORT).show();
    }
}
