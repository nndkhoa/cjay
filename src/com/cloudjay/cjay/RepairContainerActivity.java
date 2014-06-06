package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.QueryHelper;
import com.cloudjay.cjay.util.UploadType;
import com.cloudjay.cjay.util.Utils;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * 
 * Danh sách lỗi của container
 * 
 * @author quocvule
 * 
 */
@EActivity(R.layout.activity_repair_container)
@OptionsMenu(R.menu.menu_repair_container)
public class RepairContainerActivity extends CJayActivity implements OnPageChangeListener, TabListener {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";

	private ViewPagerAdapter viewPagerAdapter;
	private String[] locations;

	@ViewById
	ViewPager pager;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUuid = "";

	String containerId;

	@AfterViews
	void afterViews() {

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		SQLiteDatabase db = DataCenter.getDatabaseHelper(getApplicationContext()).getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from csiview where _id = ?", new String[] { mContainerSessionUuid });
		if (cursor.moveToFirst()) {
			containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
			setTitle(containerId);
		}

		locations = getResources().getStringArray(R.array.repair_container_tabs);
		configureViewPager();
		configureActionBar();
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

	private void configureViewPager() {

		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), locations) {

			@Override
			public Fragment getItem(int position) {
				switch (position) {
					case 0:
						RepairIssuePendingListFragment_ pendingFragment_ = new RepairIssuePendingListFragment_();
						pendingFragment_.setContainerSessionUUID(mContainerSessionUuid);
						return pendingFragment_;
					case 1:
					default:
						RepairIssueFixedListFragment_ fixedFragment_ = new RepairIssueFixedListFragment_();
						fixedFragment_.setContainerSessionUUID(mContainerSessionUuid);
						return fixedFragment_;
				}

			}
		};

		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// menu.findItem(R.id.menu_upload).setVisible(mContainerSession != null);
		menu.findItem(R.id.menu_upload).setVisible(	Utils.isValidForUpload(	getApplicationContext(),
																			mContainerSessionUuid,
																			CJayImage.TYPE_REPAIRED));
		return super.onPrepareOptionsMenu(menu);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		Logger.Log("Validating container: " + containerId);
		if (Utils.isValidForUpload(getApplicationContext(), mContainerSessionUuid, CJayImage.TYPE_REPAIRED)) {

			QueryHelper.update(	this, "container_session", ContainerSession.FIELD_UPLOAD_TYPE,
								Integer.toString(UploadType.REPAIR.getValue()), ContainerSession.FIELD_UUID + " = "
										+ Utils.sqlString(mContainerSessionUuid));
			CJayApplication.uploadContainer(context, mContainerSessionUuid, containerId);
			finish();

		} else {
			Crouton.cancelAllCroutons();
			Crouton.makeText(this, R.string.alert_invalid_container, Style.ALERT).show();
		}
	}
}
