package com.cloudjay.cjay.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.ImportFragment;
import com.cloudjay.cjay.fragment.ImportFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_wizard)
public class WizardActivity extends FragmentActivity {

    @Extra("containerID")
	String containerID;

    @AfterViews
    void addFragmentImport() {
        // Add ImportFragment to MainProcessActivity

        ImportFragment importFragment = ImportFragment_.builder().containerID(containerID).build();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.ll_main_process, importFragment);
        transaction.commit();
    }
}