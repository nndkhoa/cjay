package com.cloudjay.cjay.fragment;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Màn hình sửa chữa
 */
@EFragment(R.layout.fragment_repair)
public class RepairFragment extends Fragment implements ActionBar.TabListener {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@ViewById(R.id.pager)
	ViewPager pager;

	@ViewById(R.id.btn_done)
	Button btnCompleteRepair;

	@ViewById(R.id.btn_complete_import)
	Button btnCompleteAudit;

	ActionBar actionBar;
	private ViewPagerAdapter mPagerAdapter;
	public int currentPosition = 0;

	public RepairFragment() {
	}

	@Click(R.id.btn_done)
	void buttonContinueClick() {

//		// Add containerId to upload complete repair queue
//		JobManager jobManager = App.getJobManager();
//		jobManager.addJobInBackground(new UploadSessionJob(session.getSession(), step.value, true));
//
//	     /* Remove all tabs */
//		actionBar.removeAllTabs();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//
//		// Go to next fragment
//		Fragment fragment = new ExportFragment_().builder().containerId(containerId).build();
//		FragmentTransaction transaction = getFragmentManager().beginTransaction();
//		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//		transaction.replace(R.id.ll_main, fragment);
//		transaction.commit();
	}

	@Click(R.id.btn_complete_import)
	void buttonCompleteAuditClicked() {

//		// Add containerId to upload complete audit queue
//		JobManager jobManager = App.getJobManager();
//		jobManager.addJobInBackground(new UploadSessionJob(session.getSession(), step.value, true));
	}

	@AfterViews
	void doAfterViews() {
		configureActionBar();
		configureViewPager(1);
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

	private void configureViewPager(int tabType) {
		mPagerAdapter = new ViewPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), containerID, tabType);
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
}

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 *//*

class ViewPagerAdapter extends FragmentPagerAdapter {

	Context mContext;
    String mContainerID;

	public ViewPagerAdapter(Context context, FragmentManager fm, String containerId) {
		super(fm);
		mContext = context;
        mContainerID = containerId;
	}

	@Override
	public Fragment getItem(int position) {
//		return fragments.get(position);

		switch (position) {
			case 0:
				return new IssuePendingFragment_().builder().containerId(mContainerID).build();
			case 1:
				return new IssueRepairedFragment_().builder().containerId(mContainerID).build();
			default:
				return null;
		}

	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
			case 0:
				return "Danh sách lỗi";
			case 1:
				return "Đã sữa chữa";
		}
		return null;
	}
}
*/
