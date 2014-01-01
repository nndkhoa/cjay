package com.cloudjay.cjay;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
// slide 20
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_auditor_issue_report)
@OptionsMenu(R.menu.menu_audit_issue_report)
public class AuditorIssueReportActivity extends SherlockFragmentActivity
		implements OnPageChangeListener, TabListener,
		AuditorIssueReportListener {

	public static final String CJAY_IMAGE_EXTRA = "cjay_image";
	
	private String[] locations;
	private CJayImage mCJayImage;

	@Extra(CJAY_IMAGE_EXTRA)
	String mCJayImageUUID = "";
	
	@ViewById(R.id.pager)			ViewPager pager;
	@ViewById(R.id.item_picture)	ImageView imageView;

	@AfterViews
	void afterViews() {
		try {
			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			mCJayImage = cJayImageDaoImpl.findByUuid(mCJayImageUUID);
			if (mCJayImage.getIssue() == null) {
				mCJayImage.setIssue(new Issue());
			}

			imageView.setImageBitmap(Utils.decodeImage(getContentResolver(), mCJayImage.getOriginalPhotoUri(), Utils.MINI_THUMBNAIL_SIZE));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
		locations = getResources().getStringArray(R.array.auditor_damage_report_tabs);
		configureViewPager();
		configureActionBar();
	}
	
	@OptionsItem(R.id.menu_check)
	void checkMenuItemClicked() {
		// save data
		try {
			IssueDaoImpl issueDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getIssueDaoImpl();
			issueDaoImpl.createOrUpdate(mCJayImage.getIssue());
			issueDaoImpl.update(mCJayImage.getIssue());
			
			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			cJayImageDaoImpl.createOrUpdate(mCJayImage);
			cJayImageDaoImpl.update(mCJayImage);
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
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
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
		AuditorDamageReportTabPageAdaptor viewPagerAdapter = new AuditorDamageReportTabPageAdaptor(getSupportFragmentManager(), locations);
		pager.setOffscreenPageLimit(5);
		pager.setAdapter(viewPagerAdapter);
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
		switch (page) {
		case AuditorIssueReportListener.TAB_DAMAGE_LOCATION:
			break;
			
		case AuditorIssueReportListener.TAB_DAMAGE_DAMAGE:
			break;
			
		case AuditorIssueReportListener.TAB_DAMAGE_REPAIR:
			break;
			
		case AuditorIssueReportListener.TAB_DAMAGE_DIMENSION:
			break;
			
		case AuditorIssueReportListener.TAB_DAMAGE_QUANTITY:
			break;
		}
		
		// go to next tab
		int currPosition = getSupportActionBar().getSelectedNavigationIndex();
		if (currPosition < getSupportActionBar().getTabCount() - 1) {
			getSupportActionBar().selectTab(getSupportActionBar().getTabAt(++currPosition));
		}
	}

	@Override
	public void onReportValueChanged(int type, String val) {
		// save value
		switch (type) {
		case AuditorIssueReportListener.TYPE_LOCATION_CODE:
			mCJayImage.getIssue().setLocationCode(val);
			
			break;
		case AuditorIssueReportListener.TYPE_LENGTH:
			mCJayImage.getIssue().setLength(Double.parseDouble(val));
			
			break;
		case AuditorIssueReportListener.TYPE_HEIGHT:
			mCJayImage.getIssue().setHeight(Double.parseDouble(val));
			
			break;
		case AuditorIssueReportListener.TYPE_QUANTITY:
			mCJayImage.getIssue().setQuantity(Integer.parseInt(val));
			
			break;
		case AuditorIssueReportListener.TYPE_DAMAGE_CODE:
			try {
				DamageCodeDaoImpl damageCodeDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getDamageCodeDaoImpl();
				mCJayImage.getIssue().setDamageCode(damageCodeDaoImpl.findDamageCode(val));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			break;
		case AuditorIssueReportListener.TYPE_REPAIR_CODE:
			try {
				RepairCodeDaoImpl repairCodeDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getRepairCodeDaoImpl();
				mCJayImage.getIssue().setRepairCode(repairCodeDaoImpl.findRepairCode(val));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			break;
		}
		
	}

	public class AuditorDamageReportTabPageAdaptor extends FragmentPagerAdapter {
		private String[] locations;

		public AuditorDamageReportTabPageAdaptor(FragmentManager fm,
				String[] locations) {
			super(fm);
			this.locations = locations;
		}

		public int getCount() {
			return locations.length;
		}

		public Fragment getItem(int position) {
			Fragment fragment;
			
			switch (position) {
			case 0:
				fragment = new AuditorDamageLocationFragment_();
				((AuditorDamageLocationFragment_)fragment).setIssue(mCJayImage.getIssue());
			case 1:
				fragment = new AuditorDamageDamageFragment_();
				((AuditorDamageDamageFragment_)fragment).setIssue(mCJayImage.getIssue());
			case 2:
				fragment = new AuditorDamageRepairFragment_();
				((AuditorDamageRepairFragment_)fragment).setIssue(mCJayImage.getIssue());
			case 3:
				fragment = new AuditorDamageDimensionFragment_();
				((AuditorDamageDimensionFragment_)fragment).setIssue(mCJayImage.getIssue());
			default:
				fragment = new AuditorDamageQuantityFragment_();
				((AuditorDamageQuantityFragment_)fragment).setIssue(mCJayImage.getIssue());
			}
			
			return fragment;
		}
	}
}
