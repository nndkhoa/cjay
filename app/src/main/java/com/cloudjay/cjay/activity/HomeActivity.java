package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.upload.PreUploadStartedEvent;
import com.cloudjay.cjay.fragment.CameraFragment;
import com.cloudjay.cjay.fragment.SearchFragment;
import com.cloudjay.cjay.fragment.SearchFragment_;
import com.cloudjay.cjay.fragment.UploadFragment_;
import com.cloudjay.cjay.fragment.WorkingFragment_;
import com.cloudjay.cjay.task.job.FetchSessionsJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.JobManager;
import com.rampo.updatechecker.UpdateChecker;
import com.rampo.updatechecker.UpdateCheckerResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

@EActivity(R.layout.activity_home)
public class HomeActivity extends BaseActivity implements ActionBar.TabListener, UpdateCheckerResult {

	public int currentPosition = 0;

	@ViewById(R.id.pager)
	ViewPager mViewPager;

	@Bean
	DataCenter dataCenter;

	PagerAdapter mPagerAdapter;
	ActionBar actionBar;

	/**
	 * > MAIN FUNCTION
	 * 0. Navigate to Login Activity if user has not logged in
	 * 1. Config action bar NAVIGATION MODE
	 * 2. Config view pager
	 * 3. Start JobQueue to get all session
	 */
	@AfterViews
	void setup() {
        // Check for update
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        boolean isUpdateEnabled = preferences.getBoolean("auto_check_update_checkbox", false);
        if (isUpdateEnabled) {
            Logger.Log("Check for update");
            UpdateChecker checker = new UpdateChecker(this, this);
            checker.setSuccessfulChecksRequired(2);
            checker.start();
        }

		// Check if user was logged in
		String token = PreferencesUtil.getPrefsValue(this, PreferencesUtil.PREF_TOKEN);
		if (TextUtils.isEmpty(token)) {

			// Navigate to LoginActivity
			Intent intent = new Intent(getApplicationContext(), LoginActivity_.class);
			startActivity(intent);
			finish();

		} else {

			configureActionBar();
			configureViewPager();

			// Run job lấy tất cả sessions nếu chưa từng lấy lần nào
			String lastModifiedDate = PreferencesUtil.getPrefsValue(this, PreferencesUtil.PREF_MODIFIED_DATE);
			if (lastModifiedDate.isEmpty()) {
				JobManager jobManager = App.getJobManager();
				jobManager.addJobInBackground(new FetchSessionsJob(lastModifiedDate));
			}
		}

		Utils.keepNotificationAlive(this);
	}

	/**
	 * Config action bar
	 */
	private void configureActionBar() {
		actionBar = getActionBar();
		final Method method;
		try {
			method = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
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

	/**
	 * Config and add view pager
	 */
	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(getApplicationContext(), getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				ActionBar.Tab tab = actionBar.getTabAt(position);
				actionBar.selectTab(tab);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					if (mViewPager.getCurrentItem() != 0) {
						// Hide the keyboard.
						((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
								.hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
					}
				}
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
		mViewPager.setCurrentItem(position);
		currentPosition = position;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	public void onEvent(PreUploadStartedEvent event) {
		if (event.uploadType == UploadType.SESSION) {
			dataCenter.changeStatusWhenUpload(this, event.getSession(), UploadType.SESSION, UploadStatus.UPLOADING);
		} else if (event.uploadType == UploadType.AUDIT_ITEM) {
			dataCenter.changeStatusWhenUpload(this, event.getSession(), UploadType.AUDIT_ITEM, UploadStatus.UPLOADING);
		}
	}

    @Override
    public void foundUpdateAndShowIt(String versionDownloadable) {
        Logger.Log("Update available\n" + "Version downloadable: " + versionDownloadable + "\nVersion installed: " + getVersionInstalled());
    }

    @Override
    public void foundUpdateAndDontShowIt(String versionDownloadable) {
        Logger.Log("Already Shown\n" + "Version downloadable: " + versionDownloadable + "\nVersion installed: " + getVersionInstalled());
    }

    @Override
    public void returnUpToDate(String versionDownloadable) {
        Logger.Log("Updated\n" + "Version downloadable: " + versionDownloadable + "\nVersion installed: " + getVersionInstalled());
    }

    @Override
    public void returnMultipleApksPublished() {

    }

    @Override
    public void returnNetworkError() {

    }

    @Override
    public void returnAppUnpublished() {
        Logger.Log("App unpublished");
    }

    @Override
    public void returnStoreError() {

    }

    public String getVersionInstalled() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
                        false);

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (rainyMode) {
                // Go direct to camera
                Intent cameraActivityIntent = new Intent(getApplicationContext(), CameraActivity_.class);
                cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, "");
                cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, "");
                cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
                cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.IMPORT.value);
                startActivity(cameraActivityIntent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}

class ViewPagerAdapter extends FragmentPagerAdapter {

	Context mContext;

	public ViewPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new SearchFragment_();
			case 1:
				return new WorkingFragment_();
			case 2:
				return new UploadFragment_();
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
			case 0:
				return mContext.getResources().getString(R.string.fragment_search_title).toUpperCase(l);
			case 1:
				return mContext.getResources().getString(R.string.fragment_working_title).toUpperCase(l);
			case 2:
				return mContext.getResources().getString(R.string.fragment_upload_title).toUpperCase(l);
		}
		return null;
	}
}

