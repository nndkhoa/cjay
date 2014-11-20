package com.cloudjay.cjay.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.UserLoggedOutEvent;
import com.cloudjay.cjay.event.session.ContainersFetchedEvent;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Activity chung. Các Activity khác sẽ kế thừa BaseActivity để sử dụng menu items và các hàm chung.
 */
@EActivity
@OptionsMenu(R.menu.base)
public class BaseActivity extends FragmentActivity {

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String name = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_USER_NAME);
		String roleName = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_USER_ROLE_NAME);
		menu.findItem(R.id.menu_username).setTitle(name);
		menu.findItem(R.id.menu_role).setTitle(roleName);
		return super.onPrepareOptionsMenu(menu);
	}

	@Bean
	DataCenter dataCenter;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@OptionsItem(R.id.menu_logout)
	void logoutItemClicked() {
		showLogoutPrompt();
	}

	@OptionsItem(R.id.menu_upload_log)
	void uploadLogMenuItemClicked() {
		Intent intent = new Intent(getApplicationContext(), LogActivity_.class);
		intent.putExtra(LogActivity.LOG_TYPE_EXTRA, CJayConstant.PREFIX_LOG);
		startActivity(intent);
	}

    @OptionsItem(R.id.menu_setting)
    void settingItemClicked() {
        Intent intent = new Intent(getApplicationContext(), SettingActivity_.class);
        startActivity(intent);
    }

	protected void showLogoutPrompt() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.logout_prompt_title);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {

					dialog.dismiss();

					// Clear preference and Database
					PreferencesUtil.clearPrefs(getApplicationContext());
					getApplicationContext().deleteDatabase("db_default_job_manager.db");
					DB db = App.getDB(getApplicationContext());
					db.destroy();

					// Open Login Activity
					startActivity(new Intent(getApplicationContext(), LoginActivity_.class));
					EventBus.getDefault().post(new UserLoggedOutEvent());
				} catch (SnappydbException e) {
					e.printStackTrace();
				}
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@UiThread
	public void onEvent(ContainersFetchedEvent event) {
		Utils.showCrouton(this, "All sessions are fetched", Style.CONFIRM);
	}

	public void onEvent(UserLoggedOutEvent event) {
		finish();
	}

//	@Override
//	public boolean onMenuOpened(int featureId, Menu menu) {
//		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
//			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
//				try {
//					Method m = menu.getClass().getDeclaredMethod(
//							"setOptionalIconsVisible", Boolean.TYPE);
//					m.setAccessible(true);
//					m.invoke(menu, true);
//				} catch (NoSuchMethodException e) {
//					Logger.Log(e.getMessage());
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}
//		return super.onMenuOpened(featureId, menu);
//	}
}
