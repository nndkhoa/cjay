package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudjay.cjay.R;

/**
 * Created by thai on 20/10/2014.
 */
public class RepairedFragment extends Fragment {

    private FragmentTabHost mTabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tabhost_repaired);

        mTabHost.addTab(mTabHost.newTabSpec("simple").setIndicator("Trước sữa chữa"),
                BeforeRepairFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Sau sữa chữa"),
                AfterRepairFragment.class, null);


        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

}
