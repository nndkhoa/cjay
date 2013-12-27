package com.cloudjay.cjay;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.listener.OnReportPageCompleteListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
// slide 20
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_auditor_damage_report)
public class AuditorDamageReportActivity extends SherlockFragmentActivity
		implements OnPageChangeListener, TabListener,
		OnReportPageCompleteListener {

	private String[] locations;

	@ViewById
	ViewPager pager;

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(
				R.array.auditor_damage_report_tabs);
		configureViewPager();
		configureActionBar();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	private void configureViewPager() {
		AuditorDamageReportTabPageAdaptor viewPagerAdapter = new AuditorDamageReportTabPageAdaptor(
				getSupportFragmentManager(), locations);
		pager.setOffscreenPageLimit(5);
		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
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
	public void onReportPageCompleted(int page, String[] vals) {
		switch (page) {
		case OnReportPageCompleteListener.TAB_DAMAGE_LOCATION:

			break;
		case OnReportPageCompleteListener.TAB_DAMAGE_DAMAGE:

			break;
		case OnReportPageCompleteListener.TAB_DAMAGE_REPAIR:

			break;
		case OnReportPageCompleteListener.TAB_DAMAGE_DIMENSION:

			break;
		case OnReportPageCompleteListener.TAB_DAMAGE_QUANTITY:

			break;
		}
		int currPosition = getSupportActionBar().getSelectedNavigationIndex();
		if (currPosition < getSupportActionBar().getTabCount() - 1) {
			getSupportActionBar().selectTab(
					getSupportActionBar().getTabAt(++currPosition));
		}
	}

	public class AuditorDamageReportTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;

		public AuditorDamageReportTabPageAdaptor(FragmentManager fm,
				String[] locations) {
			super(fm);
			this.locations = locations;
		}

		public int getCount() {
			return locations.length;
		}

		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new AuditorDamageLocationFragment_();
			case 1:
				return new AuditorDamageDamageFragment_();
			case 2:
				return new AuditorDamageRepairFragment_();
			case 3:
				return new AuditorDamageDimensionFragment_();
			default:
				return new AuditorDamageQuantityFragment_();
			}
		}
	}
}
