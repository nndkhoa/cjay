package com.cloudjay.cjay.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình sửa chữa
 */
public class RepairFragment extends Fragment implements ActionBar.TabListener {

    ActionBar.Tab Tab1, Tab2;
    Fragment fragmentTab1 = new IssuePendingFragment();
    Fragment fragmentTab2 = new IssueRepairedFragment();
    private ViewPagerAdapter mPagerAdapter;
    View v;
    ViewPager pager;
    List<String> locations = new ArrayList<String>();
    Button btnContinue;
    ActionBar actionBar;
    private int currentPosition = 0;

    public RepairFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_repair, container, false);

        Logger.i("Open Repair Fragment");
        configureActionBar();
        getControl();
        configureViewPager();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Remove all tabs */
                actionBar.removeAllTabs();
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

                // Go to next fragment
                Fragment fragment = new ExportFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.ll_main_process, fragment);
                transaction.commit();
            }
        });

        return v;
    }

    private void getControl() {
        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        pager = (ViewPager) v.findViewById(R.id.pager);
    }

    private void configureActionBar() {
        actionBar = getActivity().getActionBar();
        final Method method;
        try {
            method = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", new Class[] { Boolean.TYPE });
            method.setAccessible(true);
            method.invoke(actionBar, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    private void configureViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getActivity(), getActivity().getSupportFragmentManager());
        pager.setAdapter(mPagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                ActionBar.Tab tab = actionBar.getTabAt(position);
                actionBar.selectTab(tab);
            }

        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        pager.setCurrentItem(position);
        currentPosition = position;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }
}

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class ViewPagerAdapter extends FragmentPagerAdapter {

    Context mContext;
    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
//		return fragments.get(position);

        switch (position) {
            case 0:
                return IssuePendingFragment.newInstance(0);
            case 1:
                return IssueRepairedFragment.newInstance(1);
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Danh sách lỗi";
            case 1:
                return "Đã sữa chữa";
        }
        return null;
    }
}
