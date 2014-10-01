package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.ImportFragment;

public class MainProcessActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_process);

        /* Add ImportFragment to MainProcessActivity */
        ImportFragment importFragment = new ImportFragment(this);
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.ll_main_process, importFragment);
        transaction.commit();
    }
}
