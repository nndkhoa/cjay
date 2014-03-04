package com.cloudjay.cjay;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.network.CJayClient;
import com.nostra13.universalimageloader.core.ImageLoader;

// slide 20

@EActivity(R.layout.activity_auditor_issue_report)
@OptionsMenu(R.menu.menu_audit_issue_report)
public class AuditorIssueReportActivity extends CJayActivity implements
		OnPageChangeListener, TabListener, AuditorIssueReportListener {

	public static final String CJAY_IMAGE_EXTRA = "cjay_image";

	private AuditorIssueReportTabPageAdaptor mViewPagerAdapter;
	private String[] locations;
	private CJayImage mCJayImage;
	private Issue mIssue;
	private ImageLoader imageLoader;

	@Extra(CJAY_IMAGE_EXTRA)
	String mCJayImageUUID = "";

	@ViewById(R.id.pager)
	ViewPager pager;
	@ViewById(R.id.item_picture)
	ImageView imageView;

	@AfterViews
	void afterViews() {
		try {
			imageLoader = ImageLoader.getInstance();

			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			mCJayImage = cJayImageDaoImpl.findByUuid(mCJayImageUUID);
			if (mCJayImage.getIssue() == null) {

				mIssue = new Issue();
				mIssue.setContainerSession(mCJayImage.getContainerSession());
				mCJayImage.setIssue(mIssue);
			} else {
				mIssue = mCJayImage.getIssue();
			}

			imageLoader.displayImage(mCJayImage.getUri(), imageView);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		locations = getResources().getStringArray(
				R.array.auditor_issue_report_tabs);
		configureViewPager();
		configureActionBar();
	}

	@OptionsItem(R.id.menu_check)
	void checkMenuItemClicked() {
		// save data
		for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
			IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter
					.getRegisteredFragment(i);
			if (fragment != null) {
				fragment.validateAndSaveData();
			}
		}

		// save db records
		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(this).getIssueDaoImpl();

			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance()
					.getDatabaseManager().getHelper(this).getCJayImageDaoImpl();

			// issueDaoImpl.createOrUpdate(mCJayImage.getIssue());
			issueDaoImpl.createOrUpdate(mIssue);
			cJayImageDaoImpl.createOrUpdate(mCJayImage);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// go back
		this.onBackPressed();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);

		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) imageView
				.getLayoutParams();

		// show keyboard for specific tabs
		switch (position) {
		case TAB_ISSUE_PHOTO:
			// hide the small image because we are displaying a larger version
			if (p.weight > 0) {
				p.weight = 0;
				imageView.setLayoutParams(p);
			}
			break;

		case TAB_ISSUE_DIMENSION:
		case TAB_ISSUE_QUANTITY:
			IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter
					.getRegisteredFragment(position);
			if (fragment != null) {
				fragment.showKeyboard();
			}

			// show the small image
			if (p.weight == 0) {
				p.weight = 3;
				imageView.setLayoutParams(p);
			}
			break;

		default:
			// show the small image
			if (p.weight == 0) {
				p.weight = 3;
				imageView.setLayoutParams(p);
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
			IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter
					.getRegisteredFragment(position);
			if (fragment != null) {
				fragment.hideKeyboard();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	private void configureViewPager() {
		mViewPagerAdapter = new AuditorIssueReportTabPageAdaptor(
				getSupportFragmentManager(), locations);
		pager.setOffscreenPageLimit(5);
		pager.setAdapter(mViewPagerAdapter);
		pager.setOnPageChangeListener(this);
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

	@Override
	public void onReportPageCompleted(int page) {

		// go to next tab
		int currPosition = getSupportActionBar().getSelectedNavigationIndex();
		if (currPosition < getSupportActionBar().getTabCount() - 1) {
			getSupportActionBar().selectTab(
					getSupportActionBar().getTabAt(++currPosition));

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
					DamageCodeDaoImpl damageCodeDaoImpl = CJayClient
							.getInstance().getDatabaseManager().getHelper(this)
							.getDamageCodeDaoImpl();
					damageCode = damageCodeDaoImpl.findDamageCode(val);
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
					RepairCodeDaoImpl repairCodeDaoImpl = CJayClient
							.getInstance().getDatabaseManager().getHelper(this)
							.getRepairCodeDaoImpl();
					repairCode = repairCodeDaoImpl.findRepairCode(val);
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
					ComponentCodeDaoImpl componentCodeDaoImpl = CJayClient
							.getInstance().getDatabaseManager().getHelper(this)
							.getComponentCodeDaoImpl();
					componentCode = componentCodeDaoImpl.findComponentCode(val);
				}
				mCJayImage.getIssue().setComponentCode(componentCode);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			break;
		}

	}

	public class AuditorIssueReportTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

		public AuditorIssueReportTabPageAdaptor(FragmentManager fm,
				String[] locations) {
			super(fm);
			this.locations = locations;
		}

		public int getCount() {
			return locations.length;
		}

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

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container,
					position);
			registeredFragments.put(position, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		public Fragment getRegisteredFragment(int position) {
			return registeredFragments.get(position);
		}
	}
}