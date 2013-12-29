package com.cloudjay.cjay;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.fragment.GateExportListFragment;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.SearchOperatorDialog;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

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

	@ViewById
	ViewPager pager;

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(R.array.gate_home_tabs);
		configureViewPager();
		configureActionBar();
	}

	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
				locations);
		pager.setAdapter(mPagerAdapter);
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
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:
			Toast toast = Toast.makeText(getApplicationContext(), "LOG OUT", Toast.LENGTH_SHORT);
			toast.show();
//			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
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
