package com.cloudjay.cjay.activity;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import java.lang.reflect.Method;

/**
 * Activity chung. Các Activity khác sẽ kế thừa BaseActivity để sử dụng menu items và các hàm chung.
 */
@EActivity
public class BaseActivity extends FragmentActivity {

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	@Bean
	DataCenter dataCenter;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.base, menu);
//		menu.findItem(R.id.menu_username).setTitle();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (NoSuchMethodException e) {
					Logger.Log(e.getMessage());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}
}
