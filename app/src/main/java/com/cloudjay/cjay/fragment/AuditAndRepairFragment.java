package com.cloudjay.cjay.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.HomeActivity_;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.jq.JobManager;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadSessionJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.greenrobot.event.EventBus;

/**
 * Fragment giám định và sửa chữa.
 * <p/>
 * 1. Khi lần đầu vào, chỉ hiển thị nút `Hoàn tất giám định` và theo luồng xử lý thông thường
 * 2. Nếu user chụp hình sau sửa chữa thì có nghĩa user cần sửa chữa ngay sau khi giám định.
 * 2.1 Lúc này bấm nút `Hoàn tất giám định` sẽ không đóng Activity
 * 2.2
 */
@EFragment(R.layout.fragment_audit_repair)
public class AuditAndRepairFragment extends Fragment implements ActionBar.TabListener {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
	public final static String TAB_TYPE_EXTRA = "com.cloudjay.wizard.tabtype";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@FragmentArg(TAB_TYPE_EXTRA)
	public int tabType;

	@Bean
	DataCenter dataCenter;

	@ViewById(R.id.pager)
	ViewPager pager;

	@ViewById(R.id.btn_complete_repair)
	Button btnCompleteRepair;

	@ViewById(R.id.btn_complete_audit)
	Button btnCompleteAudit;

	ActionBar actionBar;
	private ViewPagerAdapter mPagerAdapter;
	public int currentPosition = 0;

	public AuditAndRepairFragment() {
		// Required empty public constructor
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


	private void checkForShowButton() {
		Session session = DataCenter_.getInstance_(getActivity()).getSession(getActivity(), containerID);
		if (session.getLocalStep() == Step.REPAIR.value) {
			btnCompleteAudit.setVisibility(View.GONE);
			btnCompleteRepair.setVisibility(View.VISIBLE);
		}
		if (session.getLocalStep() == Step.AUDIT.value) {
			if (session.hasRepairImages()) {
				btnCompleteAudit.setVisibility(View.VISIBLE);
				btnCompleteRepair.setVisibility(View.VISIBLE);
			} else {
				btnCompleteAudit.setVisibility(View.VISIBLE);
				btnCompleteRepair.setVisibility(View.GONE);
			}
		}
	}

	@Click(R.id.btn_complete_audit)
	void btnCompleteAuditClicked() {
		Session session = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		if (session != null) {
			if (!session.isValidToUpload(Step.AUDIT)) {
				for (AuditItem auditItem : session.getAuditItems()) {
					Logger.e("UPLOAD AUDIT ITEM: " + auditItem.getUploadStatus());
				}
				Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
				return;
			}
		} else {
			Utils.showCrouton(getActivity(), "Sth goes wrong. Container Id " + containerID + " not found");
		}

		// PUT /api/cjay/containers/{pk}/complete-audit
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(session.getContainerId(), session.getLocalStep(), true));

		// Hide this button
		btnCompleteAudit.setVisibility(View.GONE);

		// Check if this session has repair image or not
		if (session.hasRepairImages()) {
			btnCompleteRepair.setVisibility(View.VISIBLE);
		} else {
			// Navigate to HomeActivity
			getActivity().finish();
		}
	}

	@Click(R.id.btn_complete_repair)
	void btnCompleteRepairClicked() {

		Session session = dataCenter.getSession(getActivity().getApplicationContext(), containerID);
		if (session != null) {
			if (!session.isValidToUpload(Step.REPAIR)) {
				Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
				return;
			}
		} else {
			Utils.showCrouton(getActivity(), "Sth goes wrong. Container Id " + containerID + " not found");
		}

		// Add containerId to upload complete repair queue
		// PUT /api/cjay/containers/{pk}/complete-repair
		JobManager jobManager = App.getJobManager();
		jobManager.addJobInBackground(new UploadSessionJob(session.getContainerId(), session.getStep(), true));

		// Navigate to HomeActivity
		Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity_.class);
		startActivity(intent);
		getActivity().finish();

//	     /* Remove all tabs */
//		actionBar.removeAllTabs();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//
//		// Go to next fragment
//		android.support.v4.app.Fragment fragment =
//				new ExportFragment_().builder().containerID(containerID).build();
//
//		FragmentTransaction transaction = getFragmentManager().beginTransaction();
//		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//		transaction.replace(R.id.ll_main, fragment);
//		transaction.commit();
	}

	@AfterViews
	void doAfterViews() {
		configureActionBar();
		configureViewPager();
		checkForShowButton();
	}

	private void configureActionBar() {

		// Get actionbar
		actionBar = getActivity().getActionBar();

		// Set ActionBar Title
		actionBar.setTitle(R.string.fragment_repair_title);

		// Fix tab layout
		final Method method;
		try {
			method = actionBar.getClass()
					.getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
			method.setAccessible(true);
			method.invoke(actionBar, false);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// Create Actionbar Tabs
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(getActivity(),
				getActivity().getSupportFragmentManager(), containerID, tabType);
		pager.setAdapter(mPagerAdapter);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				ActionBar.Tab tab = actionBar.getTabAt(position);
				actionBar.selectTab(tab);
			}

		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			actionBar.addTab(
					actionBar.newTab()
							.setText(mPagerAdapter.getPageTitle(i))
							.setTabListener(this)
			);
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
		currentPosition = position;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

	}

	@UiThread
	public void onEvent(ImageCapturedEvent event) {
		checkForShowButton();
	}
}
