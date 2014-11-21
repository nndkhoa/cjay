package com.cloudjay.cjay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.adapter.SessionAdapter;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.event.session.WorkingSessionCreatedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.session.get.GetListSessionsCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Danh sách các container đang thao tác.
 * <p/>
 * Tab Working sẽ update lại UI trong những trường hợp sau:
 * - on resume
 * - new working session
 * - container uploaded
 */
@EFragment(R.layout.fragment_working)
@OptionsMenu(R.menu.working)
public class WorkingFragment extends Fragment {


	@ViewById(R.id.lv_working_container)
	ListView lvWorking;

	@ViewById(R.id.tv_empty_list_working)
	TextView tvEmpty;

	@Bean
	DataCenter dataCenter;

	SessionAdapter mAdapter;

	public WorkingFragment() {
	}

	@ItemClick(R.id.lv_working_container)
	void workingItemClicked(int position) {

		// navigation to Wizard Activity
		Session item = mAdapter.getItem(position);

        if (item.getLocalStep() == Step.EXPORTED.value) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getActivity().getResources().getText(R.string.warning_exported_container),
                    Toast.LENGTH_LONG).show();
            return;
        }

        hideMenuItems();

		Intent intent = new Intent(getActivity(), WizardActivity_.class);
		intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, item.getContainerId());
		intent.putExtra(WizardActivity.STEP_EXTRA, item.getLocalStep());
		startActivity(intent);
	}

	@ItemLongClick(R.id.lv_working_container)
	void workingItemLongClicked(int position) {

		lvWorking.setItemChecked(position, true);

		//Get session from position
		Session item = mAdapter.getItem(position);
		selectedId = item.getContainerId();

		getActivity().supportInvalidateOptionsMenu();
	}

	@OptionsItem(R.id.menu_export)
	void exportMenuItemClicked() {
		dataCenter.changeLocalStepAndForceExport(getActivity(), selectedId);
	}

	private String selectedId;

	/**
	 * Initial loader and set adapter for list view
	 */
	@AfterViews
	void init() {
		mAdapter = new SessionAdapter(getActivity(), R.layout.item_container_working);
		lvWorking.setAdapter(mAdapter);
		lvWorking.setEmptyView(tvEmpty);
	}

	void refresh() {
		if (mAdapter != null) {
			dataCenter.add(new GetListSessionsCommand(getActivity(), CJayConstant.PREFIX_UPLOADING));
		}
	}

	@UiThread
	public void updatedData(List<Session> sessionList) {
		mAdapter.setData(sessionList);
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

	void hideMenuItems() {
		selectedId = "";
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(TextUtils.isEmpty(selectedId));
		MenuItem item = menu.findItem(R.id.menu_export);
		item.setVisible(isDisplayed);

		super.onPrepareOptionsMenu(menu);
	}

	//region EVENT HANDLER

	public void onEvent(WorkingSessionCreatedEvent event) {
		refresh();
	}

	public void onEvent(ContainersGotEvent event) {
		if (event.getPrefix().equals(CJayConstant.PREFIX_WORKING))
			updatedData(event.getTargets());
	}

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    //endregion
}
