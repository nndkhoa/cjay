package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Logger;

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
@OptionsMenu(R.menu.menu_repair_issue)
public class RepairContainerActivity extends CJayActivity implements OnPageChangeListener, TabListener {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";

	private ContainerSession mContainerSession;
	private ViewPagerAdapter viewPagerAdapter;
	private String[] locations;

	@ViewById
	ViewPager pager;

	@ViewById(R.id.container_id_textview)
	TextView containerIdTextView;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	ContainerSessionDaoImpl containerSessionDaoImpl;

	@AfterViews
	void afterViews() {
		try {
			containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this)
												.getContainerSessionDaoImpl();

			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				containerIdTextView.setText(mContainerSession.getContainerId());
			}

		} catch (SQLException e) {
			e.printStackTrace();
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
						pendingFragment_.setContainerSessionUUID(mContainerSessionUUID);
						return pendingFragment_;
					case 1:
					default:
						RepairIssueFixedListFragment_ fixedFragment_ = new RepairIssueFixedListFragment_();
						fixedFragment_.setContainerSessionUUID(mContainerSessionUUID);
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

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {

		Logger.Log("Validating container :" + mContainerSession.getContainerId());

		if (null != mContainerSession) {
			try {
				containerSessionDaoImpl.refresh(mContainerSession);

				if (mContainerSession.isValidForUpload(CJayImage.TYPE_REPAIRED)) {

					mContainerSession.setUploadType(ContainerSession.TYPE_REPAIR);
					CJayApplication.uploadContainerSesison(context, mContainerSession);

					finish();

				} else {
					Crouton.cancelAllCroutons();
					Crouton.makeText(this, R.string.alert_no_issue_container, Style.ALERT).show();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
