package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.EventMenuCreated;
import com.cloudjay.cjay.fragment.AuditAndRepairFragment_;
import com.cloudjay.cjay.fragment.ExportFragment_;
import com.cloudjay.cjay.fragment.ImportFragment_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Step;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_wizard)
public class WizardActivity extends BaseActivity {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
	public final static String STEP_EXTRA = "com.cloudjay.wizard.step";

	@Extra(CONTAINER_ID_EXTRA)
	String containerID;

	/**
	 * This is current local step
	 */
	@Extra(STEP_EXTRA)
	int step = 4;

	ActionBar actionBar;

	/**
	 * Dựa bước (step) hiện tại của container session, để tạo nên các màn hình tương ứng
	 */
	@AfterViews
	void configFragment() {
		this.exportSessionContainerId = containerID;
		// Get actionbar
		actionBar = getActionBar();

		// Set Providing Up Navigation
		actionBar.setDisplayHomeAsUpEnabled(true);

		Fragment fragment;
		Step currentStep = Step.values()[((int) step)];
		Logger.Log("Current step: " + currentStep.name());

		switch (currentStep) {

			// Load fragment Audit and Repair
			case AUDIT:
			case REPAIR:
				fragment = AuditAndRepairFragment_.builder().containerID(containerID)
						.tabType(1).build();
				break;

			// Load fragment Export
			case AVAILABLE:
				fragment = ExportFragment_.builder().containerID(containerID).build();
				break;

			// Load fragment Import
			case IMPORT:
			default:
				fragment = ImportFragment_.builder().containerID(containerID).build();
				break;
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.ll_main, fragment);
		transaction.commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}