package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.SessionsFetchedEvent;
import com.cloudjay.cjay.fragment.SearchFragment_;
import com.cloudjay.cjay.fragment.UploadFragment_;
import com.cloudjay.cjay.fragment.WorkingFragment_;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_home)
public class HomeActivity extends BaseActivity implements ActionBar.TabListener {

	public int currentPosition = 0;

	@ViewById(R.id.pager)
	ViewPager mViewPager;

	PagerAdapter mPagerAdapter;
	ActionBar actionBar;
	JobManager jobManager;

	@AfterViews
	void setup() {

		// Check if user was logged in
		String token = PreferencesUtil.getPrefsValue(this, PreferencesUtil.PREF_TOKEN);
		if (TextUtils.isEmpty(token)) {

			// Navigate to LoginActivity
			Intent intent = new Intent(getApplicationContext(), LoginActivity_.class);
			startActivity(intent);
			finish();

		} else {
			configureActionBar();
			configureViewPager();

			// Set Job Queue to get all sessions after login
//			jobManager = new JobManager(getApplicationContext());
//			jobManager.addJobInBackground(new GetAllSessionsJob(this));
		}
	}

	public void onEventMainThread(SessionsFetchedEvent gotAllSessionEvent) {
		Toast.makeText(this, "Got all Session", Toast.LENGTH_SHORT);
	}

	private void configureActionBar() {
		actionBar = getActionBar();
		final Method method;
		try {
			method = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
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

	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(getApplicationContext(), getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

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
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		int position = tab.getPosition();
		mViewPager.setCurrentItem(position);
		currentPosition = position;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}

class ViewPagerAdapter extends FragmentPagerAdapter {

	Context mContext;

	public ViewPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new SearchFragment_();
			case 1:
				return new WorkingFragment_();
			case 2:
				return new UploadFragment_();
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
			case 0:
				return mContext.getResources().getString(R.string.fragment_search_title).toUpperCase(l);
			case 1:
				return mContext.getResources().getString(R.string.fragment_working_title).toUpperCase(l);
			case 2:
				return mContext.getResources().getString(R.string.fragment_upload_title).toUpperCase(l);
		}
		return null;
	}
}

