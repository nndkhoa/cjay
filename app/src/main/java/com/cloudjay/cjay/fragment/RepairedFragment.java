package com.cloudjay.cjay.fragment;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by thai on 20/10/2014.
 */

@EFragment(R.layout.fragment_repaired)
public class RepairedFragment extends Fragment implements ActionBar.TabListener {

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    private FragmentTabHost mTabHost;

    @ViewById(R.id.pager_repaired)
    ViewPager pager;

    ActionBar actionBar;
    private ViewPagerAdapter mPagerAdapter;
    public int currentPosition = 0;

    @AfterViews
    void doAfterViews() {
        configureActionBar();
        configureViewPager();
    }

    private void configureViewPager() {
        // Get actionbar
        actionBar = getActivity().getActionBar();

        // Set ActionBar Title
        actionBar.setTitle(R.string.fragment_repair_title);

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

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set Providing Up Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void configureActionBar() {
        mPagerAdapter = new ViewPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), containerID,2);
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
                            .setTabListener(this)
            );
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        pager.setCurrentItem(position);
        currentPosition = position;

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
