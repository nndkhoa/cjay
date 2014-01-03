package com.cloudjay.cjay;

import java.sql.SQLException;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.fragment.RepairIssueImageListFragment_;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_repair_issue_report)
@OptionsMenu(R.menu.menu_repair_issue_report)
public class RepairIssueReportActivity extends CJayActivity implements
	OnPageChangeListener, TabListener {

	public static final String CJAY_ISSUE_EXTRA = "issue";
	private Issue mIssue;
	private String[] locations;
	
	@ViewById
	ViewPager pager;
	@ViewById(R.id.container_id_textview)
	TextView containerIdTextView;
	@ViewById(R.id.issue_textview)
	TextView issueTextView;
	
	@Extra(CJAY_ISSUE_EXTRA)
	String mIssueUUID = "";

	@AfterViews
	void afterViews() {
		try {
			IssueDaoImpl issueDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getIssueDaoImpl();
			mIssue = issueDaoImpl.queryForId(mIssueUUID);

			if (null != mIssue) {
				containerIdTextView.setText(mIssue.getContainerSession().getContainerId());
				issueTextView.setText(mIssue.getLocationCode() + " " + 
									mIssue.getDamageCodeString() + " " + 
									mIssue.getRepairCodeString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		locations = getResources().getStringArray(R.array.repair_issue_report_tabs);
		configureViewPager();
		configureActionBar();
	}
	
	@OptionsItem(R.id.menu_check)
	void checkMenuItemSelected() {
		// set fixed to true
		mIssue.setFixed(true);
		
		// save db records
		try {
			IssueDaoImpl issueDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getIssueDaoImpl();
			issueDaoImpl.createOrUpdate(mIssue);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// go back
		this.onBackPressed();
	}

	private void configureViewPager() {
		AuditorHomeTabPageAdaptor viewPagerAdapter = new AuditorHomeTabPageAdaptor(
				getSupportFragmentManager(), locations);
		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	private void configureActionBar() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (String location : locations) {
			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	public class AuditorHomeTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;

		public AuditorHomeTabPageAdaptor(FragmentManager fm, String[] locations) {
			super(fm);
			this.locations = locations;
		}

		public int getCount() {
			return locations.length;
		}

		public Fragment getItem(int position) {
			RepairIssueImageListFragment_ fragment;
			switch (position) {
			case 0:
				fragment = new RepairIssueImageListFragment_();
				fragment.setIssueUUID(mIssueUUID);
				fragment.setType(CJayImage.TYPE_REPORT);
				return fragment;
			case 1:
			default:
				fragment = new RepairIssueImageListFragment_();
				fragment.setIssueUUID(mIssueUUID);
				fragment.setType(CJayImage.TYPE_REPAIRED);
				return fragment;
			}
		}
	}
}
