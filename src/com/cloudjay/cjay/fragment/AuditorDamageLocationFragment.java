package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.cloudjay.cjay.R;
import com.googlecode.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_damage_location_code)
public class AuditorDamageLocationFragment extends SherlockDialogFragment implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_damage_location_code, container, false);
		return view;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

}