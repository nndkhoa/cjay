package com.cloudjay.cjay;

import java.lang.reflect.Field;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.fragment.*;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_repair_home)
public class RepairHomeActivity extends CJayActivity implements
		OnPageChangeListener, TabListener {

	private String[] locations;
	@ViewById
	ViewPager pager;

	@Override
	protected void onCreate(Bundle arg0) {

		// Below code to show `More Action` item on menu
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
			
		}
		super.onCreate(arg0);
	}

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(R.array.repair_home_tabs);
		configureViewPager();
		configureActionBar();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:

			getSession().deleteSession(getApplicationContext());
			startActivity(new Intent(this, LoginActivity_.class));

			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
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
			switch (position) {
			case 0:
				Fragment pendingFragment = new RepairContainerPendingListFragment_();
				return pendingFragment;
			case 1:
				Fragment fixedFragment = new RepairContainerFixedListFragment_();
				return fixedFragment;

			case 2:
			default:
				Fragment uploadFragment = new UploadsFragment_();
				return uploadFragment;
			}
		}
	}
}
