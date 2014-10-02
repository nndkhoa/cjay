package com.cloudjay.cjay.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.ImportFragment;

public class WizardActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard);

        /* Add ImportFragment to MainProcessActivity */
		ImportFragment importFragment = new ImportFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.ll_main_process, importFragment);
		transaction.commit();
	}
}
