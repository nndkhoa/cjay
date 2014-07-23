package com.cloudjay.cjay.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public abstract class ViewPagerAdapter extends FragmentPagerAdapter {

	private String[] locations;
	private List<Fragment> fragments;

	public ViewPagerAdapter(FragmentManager fm, String[] locations) {

		super(fm);
		this.locations = locations;
	}

	public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	public ViewPagerAdapter(FragmentManager fm, String[] locations, List<Fragment> fragments) {
		super(fm);
		this.locations = locations;
		this.fragments = fragments;
	}

	@Override
	public int getCount() {
		return locations.length;
	}

	@Override
	abstract public Fragment getItem(int position);

	public List<Fragment> getFragments() {
		return fragments;
	}

	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}
}
