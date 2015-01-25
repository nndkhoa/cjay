package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.IssueReportComponentFragment_;
import com.cloudjay.cjay.fragment.IssueReportDamageFragment_;
import com.cloudjay.cjay.fragment.IssueReportDimensionFragment_;
import com.cloudjay.cjay.fragment.IssueReportFragment;
import com.cloudjay.cjay.fragment.IssueReportLocationFragment_;
import com.cloudjay.cjay.fragment.IssueReportPhotoFragment_;
import com.cloudjay.cjay.fragment.IssueReportRepairFragment_;
import com.cloudjay.cjay.listener.AuditorIssueReportListener;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.task.command.issue.UpdateAuditItemCommand;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// slide 20

@EActivity(R.layout.activity_report_issue)
@OptionsMenu(R.menu.report_issue)
public class ReportIssueActivity extends BaseActivity implements OnPageChangeListener,
        AuditorIssueReportListener, TabListener {

    public static final String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
    public static final String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItem";
    public static final String AUDIT_IMAGE_EXTRA = "com.cloudjay.wizard.auditImage";

    private AuditorIssueReportTabPageAdaptor mViewPagerAdapter;

    ActionBar actionBar;
    private String[] mLocations;
    private AuditImage mAuditImage;
    private AuditItem mAuditItem;

    @Extra(CONTAINER_ID_EXTRA)
    String mContainerId;

    @Extra(AUDIT_ITEM_EXTRA)
    String strAuditItem;

    @Extra(AUDIT_IMAGE_EXTRA)
    String mAuditImageUuid = "";

    @ViewById(R.id.pv_pager)
    ViewPager mPager;

    @ViewById(R.id.tv_code_label)
    TextView mTvCodeLabel;

    @ViewById(R.id.tv_code_fullname)
    TextView mTvCodeFullName;

    @ViewById(R.id.tv_length)
    TextView mTvLength;

    @ViewById(R.id.tv_height)
    TextView mTvHeight;

    @ViewById(R.id.tv_quantity)
    TextView mTvQuantity;

    String codeComponent = "";
    String codeDamage = "";
    String codeRepair = "";

    @AfterExtras
    void doExtra() {
        mAuditItem = new Gson().fromJson(strAuditItem, AuditItem.class);
    }

    @AfterViews
    void afterViews() {
        if (mAuditItem != null)
            mAuditImage = mAuditItem.getAuditImage(mAuditImageUuid);

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
        // Set is allowed is null
        Logger.Log("set null for allowed");
        mAuditItem.setAllowed(null);

        dataCenter.add(new UpdateAuditItemCommand(getApplicationContext(), mContainerId, mAuditItem,
                codeComponent, codeDamage, codeRepair));
        // go back
        onBackPressed();
    }

    private void configureActionBar() {
        // Get actionbar
        actionBar = getActionBar();

        // Set ActionBar Title
        actionBar.setTitle(mContainerId);

        // Fix tab layout
        final Method method;
        try {
            method = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
            method.setAccessible(true);
            method.invoke(actionBar, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


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

        if (null == mAuditItem) {
            Utils.showCrouton(this, "Có lỗi, vui lòng thử lại sau");
            return;
        }

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
                codeDamage = val;
                break;
            case TYPE_REPAIR_CODE:
                codeRepair = val;
                break;
            case TYPE_COMPONENT_CODE:
                codeComponent = val;
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

        showKeyboard(tab);
//        showIssueDetailSummary(tab);
    }

    private void showKeyboard(Tab tab) {
        int position = tab.getPosition();

        // show keyboard for specific tabs
        switch (position) {
            case TAB_ISSUE_COMPONENT:
            case TAB_ISSUE_DAMAGE:
            case TAB_ISSUE_REPAIR:
            case TAB_ISSUE_DIMENSION:
            case TAB_ISSUE_QUANTITY:
                IssueReportFragment fragment = (IssueReportFragment) mViewPagerAdapter.getRegisteredFragment(position);
                if (fragment != null) {
                    fragment.showKeyboard();
                }
                break;

            default:
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
                    InputMethodManager imm = (InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    break;
                case TAB_ISSUE_DIMENSION:
                default:
                    fragment = new IssueReportDimensionFragment_();
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
}
