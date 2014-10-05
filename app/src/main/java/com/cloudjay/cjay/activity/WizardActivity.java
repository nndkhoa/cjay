package com.cloudjay.cjay.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.fragment.ImportFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_wizard)
public class WizardActivity extends FragmentActivity {

    @Extra("containerID")
	String containerID;

    @AfterViews
    void addFragmentImport() {
        // Add ImportFragment to MainProcessActivity
        ImportFragment importFragment = new ImportFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.ll_main_process, importFragment);
        transaction.commit();
    }

}
