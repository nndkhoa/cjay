package com.cloudjay.cjay.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public abstract class ViewPagerAdapter extends FragmentPagerAdapter {

	private String[] locations;

	public ViewPagerAdapter(FragmentManager fm, String[] locations) {

		super(fm);
		this.locations = locations;

	}

	@Override
	public int getCount() {
		return locations.length;
	}

	@Override
	abstract public Fragment getItem(int position);
}
