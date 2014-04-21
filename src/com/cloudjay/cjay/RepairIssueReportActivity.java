package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
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
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.fragment.RepairIssueImageListFragment;
import com.cloudjay.cjay.fragment.RepairIssueImageListFragment_;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;

/**
 * 
 * Danh sách hình của một issue
 * 
 * @author quocvule
 * 
 */
@EActivity(R.layout.activity_repair_issue_report)
@OptionsMenu(R.menu.menu_repair_issue_report)
public class RepairIssueReportActivity extends CJayActivity implements OnPageChangeListener, TabListener {

	public class AuditorHomeTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;

		public AuditorHomeTabPageAdaptor(FragmentManager fm, String[] locations) {
			super(fm);
			this.locations = locations;
		}

		@Override
		public int getCount() {
			return locations.length;
		}

		@Override
		public Fragment getItem(int position) {
			RepairIssueImageListFragment_ fragment;
			Bundle bundle = new Bundle();
			
			switch (position) {				
				case 0:
					bundle.putString(RepairIssueImageListFragment.CJAY_ISSUE_UUID, mIssueUUID);
					bundle.putInt(RepairIssueImageListFragment.CJAY_IMAGE_TYPE, CJayImage.TYPE_REPORT);
					fragment = new RepairIssueImageListFragment_();
					fragment.setArguments(bundle);
					return fragment;
				case 1:
				default:
					bundle.putString(RepairIssueImageListFragment.CJAY_ISSUE_UUID, mIssueUUID);
					bundle.putInt(RepairIssueImageListFragment.CJAY_IMAGE_TYPE, CJayImage.TYPE_REPAIRED);
					fragment = new RepairIssueImageListFragment_();
					fragment.setArguments(bundle);
					return fragment;
			}
		}
	}

	public static final String CJAY_ISSUE_EXTRA = "issue";
	private Issue mIssue;
	private IssueDaoImpl mIssueDaoImpl;
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
		long startTime = System.currentTimeMillis();
		
		try {
			mIssueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			mIssue = mIssueDaoImpl.queryForId(mIssueUUID);

			if (null != mIssue) {
				setTitle(mIssue.getContainerSession().getContainerId());
				containerIdTextView.setText(mIssue.getContainerSession().getContainerId());
				issueTextView.setText(mIssue.getLocationCode() + " " + mIssue.getDamageCodeString() + " "
						+ mIssue.getRepairCodeString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		long difference = System.currentTimeMillis() - startTime;
		Logger.w("---> Total time: " + Long.toString(difference));

		locations = getResources().getStringArray(R.array.repair_issue_report_tabs);
		configureViewPager();
		configureActionBar();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@OptionsItem(R.id.menu_check)
	void checkMenuItemSelected() {
		// set fixed to true
		mIssue.setFixed(true);

		// save db records
		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			issueDaoImpl.createOrUpdate(mIssue);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// go back
		onBackPressed();
	}

	// @Override
	// public void onResume() {
	// invalidateOptionsMenu();
	// super.onResume();
	// }

	private void configureActionBar() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (String location : locations) {
			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
	}

	private void configureViewPager() {
		AuditorHomeTabPageAdaptor viewPagerAdapter = new AuditorHomeTabPageAdaptor(getSupportFragmentManager(),
																					locations);
		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// only show check menu item if has TYPE_REPAIRED images
		boolean hasRepaired = false;

		if (null != mIssue) {
			try {
				mIssueDaoImpl.refresh(mIssue);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			for (CJayImage cJayImage : mIssue.getCJayImages()) {
				if (cJayImage.getType() == CJayImage.TYPE_REPAIRED) {
					hasRepaired = true;
					break;
				}
			}
		}
		menu.findItem(R.id.menu_check).setVisible(hasRepaired);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
