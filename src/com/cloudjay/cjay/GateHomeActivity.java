package com.cloudjay.cjay;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.fragment.GateExportListFragment_;
import com.cloudjay.cjay.fragment.GateImportListFragment_;
import com.cloudjay.cjay.fragment.TemporaryContainerFragment_;
import com.cloudjay.cjay.fragment.UploadsFragment_;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.SearchOperatorDialog;
import com.rampo.updatechecker.UpdateChecker;

@EActivity(R.layout.activity_gate_home)
public class GateHomeActivity extends CJayActivity implements OnPageChangeListener, TabListener,
													AddContainerDialog.AddContainerDialogListener,
													SearchOperatorDialog.SearchOperatorDialogListener {

	public static final int TAB_IMPORT = 0;
	public static final int TAB_EXPORT = 1;
	public static final int TAB_UPLOAD = 2;
	public static final int TAB_TEMP = 3;

	List<String> locations;
	private ViewPagerAdapter mPagerAdapter;
	PullToRefreshAttacher mPullToRefreshAttacher;
	private int currentPosition = 0;
	boolean rainyMode = false;

	@ViewById
	ViewPager pager;

	@AfterViews
	void afterViews() {
		locations = new ArrayList<String>();

		String[] tmp = getResources().getStringArray(R.array.gate_home_tabs);
		for (String string : tmp) {
			locations.add(string);
		}

		configureActionBar();
		configureViewPager();

	}

	public void configureActionBar() {

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Logger.e("Rainy Mode " + rainyMode);
		if (rainyMode) {
			locations.add("Tạm");
		}

		for (String location : locations) {
			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}

		if (!TextUtils.isEmpty(getIntent().getAction())) {
			if (getIntent().getAction().equals(CJayConstant.INTENT_OPEN_TAB_UPLOAD)) {
				getSupportActionBar().selectTab(getSupportActionBar().getTabAt(2));
			}
		}
	}

	List<Fragment> fragments;

	private void configureViewPager() {

		fragments = new ArrayList<Fragment>() {
			private static final long serialVersionUID = 1L;
		};

		fragments.add(new GateImportListFragment_());
		fragments.add(new GateExportListFragment_());
		fragments.add(new UploadsFragment_());

		Logger.e("Rainy Mode " + rainyMode);
		if (rainyMode) {

			Logger.e("Add temporary tabs");
			fragments.add(new TemporaryContainerFragment_());

		}

		mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), locations, fragments) {

			@Override
			public Fragment getItem(int position) {

				// switch (position) {
				// case 0:
				//
				// // Fragment importFeedFragment = new GateImportListFragment_();
				// // return importFeedFragment;
				// case 1:
				//
				// // Fragment exportFeedFragment = new GateExportListFragment_();
				// // return exportFeedFragment;
				//
				// case 2:
				// default:
				//
				// // Fragment uploadFragment = new UploadsFragment_();
				// // return uploadFragment;
				// }

				return getFragments().get(position);

			}
		};

		pager.setAdapter(mPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	@Override
	protected void onResume() {

		rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
										.getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
													true);

		// Remove last tab
		if (!rainyMode && fragments.size() > TAB_TEMP) {

			// Select first tab
			// onPageSelected(0);

			Logger.e("Remove last tab");

			locations.remove(TAB_TEMP);
			fragments.remove(TAB_TEMP);
			getSupportActionBar().removeTabAt(TAB_TEMP);
			configureViewPager();

			// mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), locations, fragments) {
			// @Override
			// public Fragment getItem(int position) {
			// return getFragments().get(position);
			// }
			// };
			//
			// pager.setAdapter(mPagerAdapter);
			// pager.setOnPageChangeListener(this);

		} else if (rainyMode && fragments.size() <= TAB_TEMP) {

			Logger.e("Add temporary tab");
			locations.add("Tạm");

			Tab tab = getSupportActionBar().newTab();
			tab.setText("Tạm");
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);

			configureViewPager();
		} else {
			Logger.e("Something goes wrong");
		}

		super.onResume();
	}

	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	@Override
	public void OnContainerInputCompleted(Fragment parent, String containerId, String operatorName, int mode) {
		if (parent instanceof GateImportListFragment_) {

			((GateImportListFragment_) parent).OnContainerInputCompleted(containerId, operatorName, mode);

		} else if (parent instanceof GateExportListFragment_) {

			((GateExportListFragment_) parent).OnContainerInputCompleted(containerId, operatorName, mode);

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
										.getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
													true);

		Logger.Log("Rainy Mode " + rainyMode);
		// Below code to show `More Action` item on menu
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
		}

		if (Utils.enableAutoCheckForUpdate(this)) {
			UpdateChecker checker = new UpdateChecker(this);
			checker.start();
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater();
		return super.onCreateOptionsMenu(menu);
	}

	public void onEventMainThread(ListItemChangedEvent event) {

		int currentTab = event.getPosition();
		getSupportActionBar().getTabAt(currentTab).setText(	locations.get(currentTab) + " ("
																	+ Integer.toString(event.getCount()) + ")");
	}

	@Override
	public void OnOperatorSelected(Fragment parent, String containerId, String operatorName, int mode) {
		if (parent instanceof GateImportListFragment_) {
			((GateImportListFragment_) parent).OnOperatorSelected(containerId, operatorName, mode);
		} else if (parent instanceof GateExportListFragment_) {
			((GateExportListFragment_) parent).OnOperatorSelected(containerId, operatorName, mode);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageSelected(int position) {

		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		int position = tab.getPosition();
		pager.setCurrentItem(position);
		currentPosition = position;
	}

	@Override
	public void onBackPressed() {

		if (currentPosition == 1) {
			GateExportListFragment_ fragment = (GateExportListFragment_) mPagerAdapter.getItem(currentPosition);
			if (fragment.isSearching()) {
				fragment.clearSearchEditText();
				return;
			}
		}

		super.onBackPressed();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (rainyMode && currentPosition == 3) {
			
			TemporaryContainerFragment_ fragment = (TemporaryContainerFragment_) mPagerAdapter.getItem(currentPosition);
			fragment.onKeyDown(keyCode);
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
