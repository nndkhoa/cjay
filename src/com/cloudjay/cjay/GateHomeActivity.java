package com.cloudjay.cjay;

import java.lang.reflect.Field;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.R.integer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.SearchOperatorDialog;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_gate_home)
public class GateHomeActivity extends CJayActivity implements
		OnPageChangeListener, TabListener,
		AddContainerDialog.AddContainerDialogListener,
		SearchOperatorDialog.SearchOperatorDialogListener {

	public static final int TAB_IMPORT = 0;
	public static final int TAB_EXPORT = 1;
	public static final int TAB_UPLOAD = 2;

	private String[] locations;
	private ViewPagerAdapter mPagerAdapter;
	PullToRefreshAttacher mPullToRefreshAttacher;

	@ViewById
	ViewPager pager;

	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

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
		locations = getResources().getStringArray(R.array.gate_home_tabs);
		configureViewPager();
		configureActionBar();

	}

	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
				locations) {

			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 0:
					Fragment importFeedFragment = new GateImportListFragment_();
					return importFeedFragment;
				case 1:
					Fragment exportFeedFragment = new GateExportListFragment_();
					return exportFeedFragment;
				case 2:
				default:
					Fragment uploadFragment = new UploadsFragment_();
					return uploadFragment;
				}
			}
		};
		pager.setAdapter(mPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	public void configureActionBar() {

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (String location : locations) {
			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
	}

	public void onEventMainThread(ListItemChangedEvent event) {

		int currentTab = event.getPosition();
		getSupportActionBar().getTabAt(currentTab).setText(
				locations[currentTab] + " ("
						+ Integer.toString(event.getCount()) + ")");
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void OnOperatorSelected(Fragment parent, String containerId,
			String operatorName, int mode) {
		if (parent instanceof GateImportListFragment) {
			((GateImportListFragment) parent).OnOperatorSelected(containerId,
					operatorName, mode);
		} else if (parent instanceof GateExportListFragment) {
			((GateExportListFragment) parent).OnOperatorSelected(containerId,
					operatorName, mode);
		}
	}

	@Override
	public void OnContainerInputCompleted(Fragment parent, String containerId,
			String operatorName, int mode) {
		if (parent instanceof GateImportListFragment) {

			((GateImportListFragment) parent).OnContainerInputCompleted(
					containerId, operatorName, mode);

		} else if (parent instanceof GateExportListFragment) {

			((GateExportListFragment) parent).OnContainerInputCompleted(
					containerId, operatorName, mode);

		}
	}

}
