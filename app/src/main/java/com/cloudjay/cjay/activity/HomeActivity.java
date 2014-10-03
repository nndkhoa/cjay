package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;


import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.GotAllSessionEvent;
import com.cloudjay.cjay.fragment.SearchFragment;
import com.cloudjay.cjay.fragment.UploadFragment;
import com.cloudjay.cjay.fragment.WorkingFragment;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.jobqueue.GetAllSessionsJob;
import com.path.android.jobqueue.JobManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import de.greenrobot.event.EventBus;

public class HomeActivity extends BaseActivity implements ActionBar.TabListener {

	ViewPager mViewPager;
	PagerAdapter mPagerAdapter;
	ActionBar actionBar;
	public int currentPosition = 0;
	JobManager jobManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Check if user was logged in
		User user = PreferencesUtil.getObject(this, PreferencesUtil.PREF_CURRENT_USER, User.class);
		if (null == user) {
			// Navigate to LoginActivity
		}

		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);

		mViewPager = (ViewPager) findViewById(R.id.pager);

		configureActionBar();
		configureViewPager();

		//Set Job Queue to get all sessions after login
		//TODO add fetch data from job queue to database
		jobManager = new JobManager(getApplicationContext());
		jobManager.addJobInBackground(new GetAllSessionsJob(this));
	}

	public void onEventMainThread(GotAllSessionEvent gotAllSessionEvent) {
		Toast.makeText(this, "Got all Session", Toast.LENGTH_SHORT);
	}


	private void configureActionBar() {
		actionBar = getActionBar();
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
							.setTabListener(this));
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
				return SearchFragment.newInstance(0);
			case 1:
				return WorkingFragment.newInstance(1);
			case 2:
				return UploadFragment.newInstance(2);
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

