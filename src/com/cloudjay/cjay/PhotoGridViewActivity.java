package com.cloudjay.cjay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.GridView;

@EActivity(R.layout.activity_photo_gridview)
public class PhotoGridViewActivity extends CJayActivity implements
	OnRefreshListener, LoaderCallbacks<Cursor> {

	@ViewById(R.id.gridview) GridView gridView;	
	
	@AfterViews
	void afterViews() {

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		
	}
	
}