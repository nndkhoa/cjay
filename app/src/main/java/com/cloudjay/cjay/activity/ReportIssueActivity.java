package com.cloudjay.cjay.activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

// slide 20

@EActivity(R.layout.activity_report_issue)
@OptionsMenu(R.menu.report_issue)
public class ReportIssueActivity extends BaseActivity implements OnPageChangeListener,
        AuditorIssueReportListener, TabListener {

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
                    ((IssueReportPhotoFragment_) fragment).setCJayImage(mAuditImage);
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

            fragment.setAuditItem(mAuditItem);

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

    public static final String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";
    public static final String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItem";
    public static final String AUDIT_IMAGE_EXTRA = "com.cloudjay.wizard.auditImage";

    private AuditorIssueReportTabPageAdaptor mViewPagerAdapter;

    private String[] mLocations;
    private Session mSession;
    private AuditItem mAuditItem;
    private AuditImage mAuditImage;

    private ImageLoader mImageLoader;;

    @Extra(CONTAINER_ID_EXTRA)
    String mContainerId = "";

    @Extra(AUDIT_ITEM_EXTRA)
    String mAuditItemUuid = "";
    
    @Extra(AUDIT_IMAGE_EXTRA)
    String mAuditImageUuid = "";

    @ViewById(R.id.pv_pager)
    ViewPager mPager;

    @ViewById(R.id.iv_audit_image)
	SquareImageView mImageView;

    @Bean
    DataCenter mDataCenter;

    @AfterViews
    void afterViews() {

		Logger.Log(mAuditItemUuid);

        mSession = mDataCenter.getSession(getApplicationContext(), mContainerId);
        mAuditItem = mDataCenter.getAuditItem(getApplicationContext(), mContainerId, mAuditItemUuid);
        mAuditImage = mDataCenter.getAuditImageByUUId(getApplicationContext(), mContainerId, mAuditItemUuid, mAuditImageUuid);

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.displayImage(mAuditImage.getUrl(), mImageView);

        // Set Activity Title
        setTitle(mSession.getContainerId());
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mLocations = getResources().getStringArray(R.array.issue_report_tabs);

        // load tabs
        configureViewPager();
        configureActionBar();

        // go to the 2nd tab
        getActionBar().selectTab(getActionBar().getTabAt(TAB_ISSUE_COMPONENT));
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
            Toast.makeText(this, "Dữ liệu chưa hoàn chỉnh", Toast.LENGTH_LONG).show();
            return;
        }

		// Set audited is true before saving
		mAuditItem.setAudited(true);
		// save db records and refresh list
        mDataCenter.updateAuditItem(getApplicationContext(), mContainerId, mAuditItem);

        // go back
        onBackPressed();
    }

    private void configureActionBar() {

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for (String location : mLocations) {

            Tab tab = getActionBar().newTab();
            tab.setText(location);
            tab.setTabListener(this);
            getActionBar().addTab(tab);
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
        Tab tab = getActionBar().getTabAt(position);
        getActionBar().selectTab(tab);
    }

    @Override
    public void onReportPageCompleted(int page) {

        // go to next tab
        int currPosition = getActionBar().getSelectedNavigationIndex();
        if (currPosition < getActionBar().getTabCount() - 1) {

            getActionBar().selectTab(getActionBar().getTabAt(++currPosition));

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
                mAuditItem.setLocationCode(val);

                break;
            case TYPE_LENGTH:
                mAuditItem.setLength(Double.valueOf(val));

                break;
            case TYPE_HEIGHT:
                mAuditItem.setHeight(Double.valueOf(val));

                break;
            case TYPE_QUANTITY:
                mAuditItem.setQuantity(Long.valueOf(val));

                break;
            case TYPE_DAMAGE_CODE:
                IsoCode damageCode = null;
                if (!TextUtils.isEmpty(val)) {
                    damageCode = mDataCenter.getIsoCode(getApplicationContext(),
                            CJayConstant.PREFIX_DAMAGE_CODE,
                            val);
                }
                if (damageCode != null) {
                    mAuditItem.setDamageCodeId(damageCode.getId());
                    mAuditItem.setDamageCode(damageCode.getCode());
                }

                break;
            case TYPE_REPAIR_CODE:
                IsoCode repairCode = null;
                if (!TextUtils.isEmpty(val)) {
                    repairCode = mDataCenter.getIsoCode(getApplicationContext(),
                            CJayConstant.PREFIX_REPAIR_CODE,
                            val);
                }
                if (repairCode != null) {
                    mAuditItem.setRepairCodeId(repairCode.getId());
                    mAuditItem.setRepairCode(repairCode.getCode());
                }

                break;
            case TYPE_COMPONENT_CODE:
                IsoCode componentCode = null;
                if (!TextUtils.isEmpty(val)) {
                    componentCode = mDataCenter.getIsoCode(getApplicationContext(),
                            CJayConstant.PREFIX_COMPONENT_CODE,
                            val);
                }
                if (componentCode != null) {
                    mAuditItem.setComponentCodeId(componentCode.getId());
                    mAuditItem.setComponentCode(componentCode.getCode());
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
				mImageView.setVisibility(View.GONE);
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
                if (mImageView.getVisibility() == View.GONE) {
					mImageView.setVisibility(View.VISIBLE);
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