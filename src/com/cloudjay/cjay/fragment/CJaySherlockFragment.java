package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.CJayApplication;

public class CJaySherlockFragment extends SherlockFragment {

	private static final String SAVED_STATE_KEY = CJaySherlockFragment.class
			.getSimpleName();
	protected int mCurrentlyShowingFragment;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currently_showing_fragment", mCurrentlyShowingFragment);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		SavedState fragmentSavedState = ((CJayApplication) getActivity()
				.getApplication()).getFragmentSavedState(SAVED_STATE_KEY);

		if (fragmentSavedState == null) {
			if (savedInstanceState == null) {

				// getChildFragmentManager()
				// .beginTransaction()
				// .replace(R.id.nested_fragment_container,
				// NestedFragmentOne.newInstance()).commit();

				mCurrentlyShowingFragment = 0;
			} else {

				// use savedInstanceState here to restore state saved in
				// onSaveInstance

				mCurrentlyShowingFragment = savedInstanceState
						.getInt("currently_showing_fragment");
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		((CJayApplication) getActivity().getApplication())
				.setFragmentSavedState(SAVED_STATE_KEY, getFragmentManager()
						.saveFragmentInstanceState(this));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		((CJayApplication) getActivity().getApplication())
				.setFragmentSavedState(SAVED_STATE_KEY, null);
	}

}
