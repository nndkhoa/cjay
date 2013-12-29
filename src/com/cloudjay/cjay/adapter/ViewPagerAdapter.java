package com.cloudjay.cjay.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cloudjay.cjay.SampleFragment_;
import com.cloudjay.cjay.fragment.GateExportListFragment_;
import com.cloudjay.cjay.fragment.GateImportListFragment_;
import com.cloudjay.cjay.fragment.UploadsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	private String[] locations;

	public ViewPagerAdapter(FragmentManager fm, String[] locations) {
		super(fm);
		this.locations = locations;
	}

	public int getCount() {
		return locations.length;
	}

	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			Fragment importFeedFragment = new GateImportListFragment_();
			return importFeedFragment;
		case 1:
			Fragment exportFeedFragment = new GateExportListFragment_();
			return exportFeedFragment;
		case 2:
		default:
			Fragment uploadFragment = new UploadsFragment();
			return uploadFragment;
		}
	}

}
