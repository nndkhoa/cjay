package com.cloudjay.cjay.fragment;



import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;

/**
 * Màn hình sửa chữa
 */
public class RepairFragment extends Fragment {

    ActionBar.Tab Tab1, Tab2;
    Fragment fragmentTab1 = new IssuePendingFragment();
    Fragment fragmentTab2 = new IssueRepairedFragment();

    Button btnContinue;

    public RepairFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_repair, container, false);

        Logger.i("Open Repair Fragment");
        final ActionBar actionBar = getActivity().getActionBar();

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set Tab Icon and Titles
        Tab1 = actionBar.newTab().setText("Danh sách lỗi");
        Tab2 = actionBar.newTab().setText("Đã sữa chữa");

        // Set Tab Listeners
        Tab1.setTabListener(new TabListener(fragmentTab1));
        Tab2.setTabListener(new TabListener(fragmentTab2));

        // Add tabs to actionbar
        actionBar.addTab(Tab1);
        actionBar.addTab(Tab2);

        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Remove all tabs */
                actionBar.removeAllTabs();
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

                /* Go to next fragment */
                Fragment fragment = new ExportFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.ll_main_process, fragment);
                transaction.commit();
            }
        });

        return v;
    }

    public class TabListener implements ActionBar.TabListener {

        Fragment fragment;

        public TabListener(Fragment fragment) {
            // TODO Auto-generated constructor stub
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            fragmentTransaction.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            fragmentTransaction.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }
}
