package com.cloudjay.cjay.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Thai on 9/30/2014.
 */
public class TabHostPagerAdapter extends FragmentPagerAdapter {
	List<Fragment> fragments;

	/**
	 * Create View Pager Adapter for Fragment TabHost
	 * @param fm
	 * @param fragments
	 */
	public TabHostPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments =fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}
}
