package com.cloudjay.cjay.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.image.ImageCapturedEvent;
import com.cloudjay.cjay.event.session.ContainerForUploadGotEvent;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.job.UploadImportJob;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.path.android.jobqueue.JobManager;
import com.snappydb.SnappydbException;

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
 * 1. Default sẽ hiện button giám định.
 * 2. Kiểm tra biến mSession để hiển thị
 */
@EFragment(R.layout.fragment_audit_repair)
public class AuditAndRepairFragment extends Fragment implements ActionBar.TabListener {

	//region ATTR
	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
	public final static String TAB_TYPE_EXTRA = "com.cloudjay.wizard.tabtype";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@FragmentArg(TAB_TYPE_EXTRA)
	public int tabType;

	@Bean
	DataCenter dataCenter;

	@ViewById(R.id.pager)
	ViewPager pager;

	@ViewById(R.id.btn_done)
	Button btnCompleteRepair;

	@ViewById(R.id.btn_complete_import)
	Button btnCompleteAudit;

	ActionBar actionBar;
	private ViewPagerAdapter mPagerAdapter;
	public int currentPosition = 0;

	Session mSession;
	//endregion

	public AuditAndRepairFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

    @UiThread
	public void onEvent(ContainerForUploadGotEvent event) {
		mSession = event.getTarget();

        Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");

		// Xu ly cho session da duoc Giam Dinh
		if (mSession.getLocalStep() == Step.AUDIT.value) {
			if (mSession != null) {
				if (!mSession.isValidToUpload(Step.AUDIT)) {
					Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
					return;
				}
			} else {
				Utils.showCrouton(getActivity(), "Không tìm thấy container " + containerID);
				return;
			}

			// PUT /api/cjay/containers/{pk}/complete-audit
			JobManager jobManager = App.getJobManager();
			jobManager.addJobInBackground(new UploadImportJob(mSession));

			// Hide this button
			btnCompleteAudit.setVisibility(View.GONE);

			// Check if this session has repair image or not
			if (mSession.hasRepairImages()) {
				btnCompleteRepair.setVisibility(View.VISIBLE);
			} else {
				// Navigate to HomeActivity
				getActivity().finish();
			}

		} else if (mSession.getLocalStep() == Step.REPAIR.value) {

			// Xu ly cho session da duoc sua chua
			if (mSession != null) {

				if (mSession.getLocalStep() == Step.AUDIT.value) {
					Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
					return;
				}

				if (!mSession.isValidToUpload(Step.REPAIR)) {
					Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
					return;
				}
			} else {
				Utils.showCrouton(getActivity(), "Sth goes wrong. Container Id " + containerID + " not found");
			}

			// Add containerId to upload complete repair queue
			// PUT /api/cjay/containers/{pk}/complete-repair
			//TODO add cjobject to queue @Han
			try {
				dataCenter.addCJayObject(containerID, new CJayObject());
			} catch (SnappydbException e) {
				e.printStackTrace();
			}

			// Navigate to HomeActivity
			getActivity().finish();
		}

	}

	@Click(R.id.btn_complete_import)
	void btnCompleteAuditClicked() {
		dataCenter.getSessionForUpload(getActivity(), containerID);
	}

	@Click(R.id.btn_done)
	void btnCompleteRepairClicked() {
		dataCenter.getSessionForUpload(getActivity(), containerID);
	}

	@AfterViews
	void doAfterViews() {
		configureActionBar();
		configureViewPager();
		dataCenter.getSessionInBackground(getActivity(), containerID);
	}

	@UiThread
	void checkForShowButton() {

		if (mSession.getLocalStep() == Step.REPAIR.value) {
			btnCompleteAudit.setVisibility(View.GONE);
			btnCompleteRepair.setVisibility(View.VISIBLE);
		}

		if (mSession.getLocalStep() == Step.AUDIT.value) {
			if (mSession.hasRepairImages()) {
				btnCompleteAudit.setVisibility(View.VISIBLE);
				btnCompleteRepair.setVisibility(View.VISIBLE);
			} else {
				btnCompleteAudit.setVisibility(View.VISIBLE);
				btnCompleteRepair.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Cấu hình action bar
	 */
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

	/**
	 * Cấu hình view pager
	 */
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

	public void onEvent(ContainerGotEvent event) {
		mSession = event.getSession();
		checkForShowButton();
	}

	/**
	 * Dùng để kiểm tra và xử lý hiển thị button giám định và sửa chữa
	 *
	 * @param event
	 */
	public void onEvent(ImageCapturedEvent event) {

		// requery to update button
		int imageType = event.getImageType();
		if (imageType == ImageType.AUDIT.value) {
			dataCenter.changeSessionLocalStepInBackground(getActivity(), containerID, Step.AUDIT);
		}
	}
}
