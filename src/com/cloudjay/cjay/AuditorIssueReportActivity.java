package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.fragment.IssueReportComponentFragment_;
import com.cloudjay.cjay.fragment.IssueReportDamageFragment_;
import com.cloudjay.cjay.fragment.IssueReportDimensionFragment_;
import com.cloudjay.cjay.fragment.IssueReportFragment;
import com.cloudjay.cjay.fragment.IssueReportLocationFragment_;
import com.cloudjay.cjay.fragment.IssueReportPhotoFragment_;
import com.cloudjay.cjay.fragment.IssueReportQuantityFragment_;
import com.cloudjay.cjay.fragment.IssueReportRepairFragment_;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;
import com.koushikdutta.async.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

// slide 20

@EActivity(R.layout.activity_auditor_issue_report)
@OptionsMenu(R.menu.menu_audit_issue_report)
public class AuditorIssueReportActivity extends CJayActivity implements OnPageChangeListener, TabListener,
															AuditorIssueReportListener {

	public class AuditorIssueReportTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

		public AuditorIssueReportTabPageAdaptor(FragmentManager fm, String[] locations) {
			super(fm);
			this.locations = locations;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			return locations.length;
		}

		@Override
		public Fragment getItem(int position) {
			IssueReportFragment fragment;

			switch (position) {
				case TAB_ISSUE_PHOTO:
					fragment = new IssueReportPhotoFragment_();
					((IssueReportPhotoFragment_) fragment).setCJayImage(mCJayImage);
					break;
				case TAB_ISSUE_LOCATION:
					fragment = new IssueReportLocationFragment_();
					break;
				case TAB_ISSUE_DAMAGE:
					fragment = new IssueReportDamageFragment_();
					break;
				case TAB_ISSUE_REPAIR:
					fragment = new IssueReportRepairFragment_();
					break;
				case TAB_ISSUE_COMPONENT:
					fragment = new IssueReportComponentFragment_();
					break;
				case TAB_ISSUE_DIMENSION:
					fragment = new IssueReportDimensionFragment_();
					break;
				case TAB_ISSUE_QUANTITY:
				default:
					fragment = new IssueReportQuantityFragment_();
					break;
			}

			fragment.setIssue(mCJayImage.getIssue());
			return fragment;
		}

		public Fragment getRegisteredFragment(int position) {
			return registeredFragments.get(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragments.put(position, fragment);
			return fragment;
		}
	}

	public static final String CJAY_IMAGE_EXTRA = "cjay_image";
	private AuditorIssueReportTabPageAdaptor mViewPagerAdapter;

	private String[] mLocations;
	private CJayImage mCJayImage;
	private Issue mIssue;

	private ImageLoader mImageLoader;
	CJayImageDaoImpl mCJayImageDaoImpl = null;

	IssueDaoImpl mIssueDaoImpl = null;

	@Extra(CJAY_IMAGE_EXTRA)
	String mCJayImageUUID = "";

	@ViewById(R.id.pager)
	ViewPager mPager;

	@ViewById(R.id.item_picture)
	ImageView mImageView;

	@AfterViews
	void afterViews() {
		try {

			mImageLoader = ImageLoader.getInstance();

			if (null == mCJayImageDaoImpl) {
				mCJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			}

			if (null == mIssueDaoImpl) {
				mIssueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			}

			mCJayImage = mCJayImageDaoImpl.findByUuid(mCJayImageUUID);
			if (mCJayImage.getIssue() == null) {
				mIssue = new Issue();
				mIssue.setContainerSession(mCJayImage.getContainerSession());
				mCJayImage.setIssue(mIssue);
			} else {
				mIssue = mCJayImage.getIssue();
			}

			mImageLoader.displayImage(mCJayImage.getUri(), mImageView);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Set Activity Title
		setTitle(mCJayImage.getContainerSession().getContainerId());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mLocations = getResources().getStringArray(R.array.auditor_issue_report_tabs);

		// load tabs
		configureViewPager();
		configureActionBar();

		// go to the 2nd tab
		getSupportActionBar().selectTab(getSupportActionBar().getTabAt(TAB_ISSUE_COMPONENT));
	}

	@OptionsItem(R.id.menu_check)
	void checkMenuItemClicked() {

		// validate and save data
		boolean isValidated = true;
		for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
			IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter.getRegisteredFragment(i);
			if (fragment != null) {
				if (!fragment.validateAndSaveData()) {
					isValidated = false;
				}
			}
		}
		if (!isValidated) {
			Toast.makeText(this, getString(R.string.issue_details_missing_warning), Toast.LENGTH_LONG).show();
			return;
		}

		// save db records
		try {
			mIssueDaoImpl.createOrUpdate(mIssue);
			// mCJayImageDaoImpl.refresh(mCJayImage);
			// mCJayImage.setIssue(mIssue);
			// mCJayImageDaoImpl.createOrUpdate(mCJayImage);
			mCJayImageDaoImpl.updateRaw("UPDATE cjay_image SET issue_id = " + Utils.sqlString(mIssue.getUuid())
					+ " WHERE uuid LIKE " + Utils.sqlString(mCJayImageUUID));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// go back
		onBackPressed();
	}

	private void configureActionBar() {

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (String location : mLocations) {

			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
	}

	private void configureViewPager() {
		mViewPagerAdapter = new AuditorIssueReportTabPageAdaptor(getSupportFragmentManager(), mLocations);
		mPager.setOffscreenPageLimit(5);
		mPager.setAdapter(mViewPagerAdapter);
		mPager.setOnPageChangeListener(this);
	}

	@OptionsItem(android.R.id.home)
	void homeIconClicked() {
		finish();
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	@Override
	public void onReportPageCompleted(int page) {

		// go to next tab
		int currPosition = getSupportActionBar().getSelectedNavigationIndex();
		if (currPosition < getSupportActionBar().getTabCount() - 1) {

			getSupportActionBar().selectTab(getSupportActionBar().getTabAt(++currPosition));

		} else {
			// if the last tab is complete, then save issue and exit
			checkMenuItemClicked();
		}
	}

	@Override
	public void onReportValueChanged(int type, String val) {
		// save value
		switch (type) {
			case TYPE_LOCATION_CODE:
				mCJayImage.getIssue().setLocationCode(val);

				break;
			case TYPE_LENGTH:
				mCJayImage.getIssue().setLength(val);

				break;
			case TYPE_HEIGHT:
				mCJayImage.getIssue().setHeight(val);

				break;
			case TYPE_QUANTITY:
				mCJayImage.getIssue().setQuantity(val);

				break;
			case TYPE_DAMAGE_CODE:
				try {
					DamageCode damageCode = null;
					if (val != null && !TextUtils.isEmpty(val)) {
						DamageCodeDaoImpl damageCodeDaoImpl = CJayClient.getInstance().getDatabaseManager()
																		.getHelper(this).getDamageCodeDaoImpl();
						damageCode = damageCodeDaoImpl.findByCode(val);
					}
					mCJayImage.getIssue().setDamageCode(damageCode);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				break;
			case TYPE_REPAIR_CODE:

				try {

					RepairCode repairCode = null;
					if (val != null && !TextUtils.isEmpty(val)) {
						RepairCodeDaoImpl repairCodeDaoImpl = CJayClient.getInstance().getDatabaseManager()
																		.getHelper(this).getRepairCodeDaoImpl();
						repairCode = repairCodeDaoImpl.findByCode(val);
					}

					mCJayImage.getIssue().setRepairCode(repairCode);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				break;
			case TYPE_COMPONENT_CODE:
				try {
					ComponentCode componentCode = null;
					if (val != null && !TextUtils.isEmpty(val)) {
						ComponentCodeDaoImpl componentCodeDaoImpl = CJayClient.getInstance().getDatabaseManager()
																				.getHelper(this)
																				.getComponentCodeDaoImpl();
						componentCode = componentCodeDaoImpl.findByCode(val);
					}
					mCJayImage.getIssue().setComponentCode(componentCode);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				break;
		}

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		mPager.setCurrentItem(position);

		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) mImageView.getLayoutParams();

		// show keyboard for specific tabs
		switch (position) {
			case TAB_ISSUE_PHOTO:
				// hide the small image because we are displaying a larger version
				if (p.weight > 0) {
					p.weight = 0;
					mImageView.setLayoutParams(p);
				}
				break;

			case TAB_ISSUE_DIMENSION:
			case TAB_ISSUE_QUANTITY:
				IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter.getRegisteredFragment(position);
				if (fragment != null) {
					fragment.showKeyboard();
				}

				// show the small image
				if (p.weight == 0) {
					p.weight = 3;
					mImageView.setLayoutParams(p);
				}
				break;

			default:
				// show the small image
				if (p.weight == 0) {
					p.weight = 3;
					mImageView.setLayoutParams(p);
				}
				break;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// hide keyboard for specific tabs
		int position = tab.getPosition();
		// LinearLayout.LayoutParams p =
		// (LinearLayout.LayoutParams)imageView.getLayoutParams();

		switch (position) {
			case TAB_ISSUE_PHOTO:
			case TAB_ISSUE_COMPONENT:
			case TAB_ISSUE_DAMAGE:
			case TAB_ISSUE_REPAIR:
			case TAB_ISSUE_DIMENSION:
			case TAB_ISSUE_QUANTITY:
				// show-hide keyboard
				IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter.getRegisteredFragment(position);
				if (fragment != null) {
					fragment.hideKeyboard();
				}
				break;

			default:
				break;
		}
	}
}