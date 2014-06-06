package com.cloudjay.cjay;

import java.lang.reflect.Field;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.events.ListItemChangedEvent;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.SearchOperatorDialog;
import com.rampo.updatechecker.UpdateChecker;

/**
 * 
 * Danh s‡ch container
 * 
 * @author quocvule
 * 
 */
@EActivity(R.layout.activity_repair_home)
public class RepairHomeActivity extends CJayActivity implements OnPageChangeListener, TabListener,
													AddContainerDialog.AddContainerDialogListener,
													SearchOperatorDialog.SearchOperatorDialogListener {

	private String[] locations;
	private ViewPagerAdapter viewPagerAdapter;

	@ViewById(R.id.pager)
	ViewPager pager;

	@AfterViews
	void afterViews() {

		if (pager == null) {
			Logger.e("ViewPager is null");
		}

		locations = getResources().getStringArray(R.array.repair_home_tabs);
		configureViewPager();
		configureActionBar();
	}

	private void configureActionBar() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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

	private void configureViewPager() {

		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), locations) {

			@Override
			public Fragment getItem(int position) {
				switch (position) {
					case 0:
						Fragment pendingFragment = new RepairContainerPendingListFragment_();
						return pendingFragment;

						// case 1:
						// Fragment fixedFragment = new RepairContainerFixedListFragment_();
						// return fixedFragment;

					case 1:
					default:
						Fragment uploadFragment = new UploadsFragment_();
						return uploadFragment;
				}

			}
		};

		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	@Override
	public void OnContainerInputCompleted(Fragment parent, String containerId, String operatorName, int mode) {
		if (parent instanceof RepairContainerPendingListFragment_) {
			((RepairContainerPendingListFragment_) parent).OnContainerInputCompleted(containerId, operatorName, mode);
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {

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

		super.onCreate(arg0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater();
		return true;
	}

	public void onEventMainThread(ListItemChangedEvent event) {

		int currentTab = event.getPosition();
		getSupportActionBar().getTabAt(currentTab).setText(	locations[currentTab] + " ("
																	+ Integer.toString(event.getCount()) + ")");
	}

	@Override
	public void OnOperatorSelected(Fragment parent, String containerId, String operatorName, int mode) {
		if (parent instanceof RepairContainerPendingListFragment_) {
			((RepairContainerPendingListFragment_) parent).OnOperatorSelected(containerId, operatorName, mode);
		}
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
