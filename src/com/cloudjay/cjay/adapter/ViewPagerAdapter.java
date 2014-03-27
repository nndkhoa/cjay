package com.cloudjay.cjay.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public abstract class ViewPagerAdapter extends FragmentStatePagerAdapter {

	private String[] locations;

	public ViewPagerAdapter(FragmentManager fm, String[] locations) {

		super(fm);
		this.locations = locations;

	}

	public int getCount() {
		return locations.length;
	}

	abstract public Fragment getItem(int position);
}
