package com.cloudjay.cjay;

//import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.fragment.*;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_auditor_home)
public class AuditorHomeActivity extends SherlockFragmentActivity implements
		OnPageChangeListener, TabListener {

	private String[] locations;
	@ViewById
	ViewPager pager;

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(R.array.auditor_home_tabs);
		configureViewPager();
		configureActionBar();
	}

	private void configureViewPager() {
		AuditorHomeTabPageAdaptor viewPagerAdapter = new AuditorHomeTabPageAdaptor(getSupportFragmentManager(), locations);
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
    		Fragment fragment = new SampleFragment_();
    	    Bundle bundle = new Bundle();
	    	switch (position) {
	    	case 0:
	    		Fragment reportingListFragment_ = new AuditorReportingListFragment_(); 
	    	    return reportingListFragment_;
	    	case 1:
	    		Fragment reportedListFragment_ = new AuditorReportedListFragment_(); 
	    	    return reportedListFragment_;
	    	default:
	    	    bundle.putString("label", locations[position]);
	    	    fragment.setArguments(bundle);
	    	    return fragment;
	    	}
	    }
	}
}
