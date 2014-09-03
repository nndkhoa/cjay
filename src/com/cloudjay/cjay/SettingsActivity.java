package com.cloudjay.cjay;

import java.util.List;

import com.cloudjay.cjay.util.Logger;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PREF_KEY_AUTO_CHECK_UPDATE = getString(R.string.pref_key_auto_check_update_checkbox);
		PREF_KEY_ENABLE_LOGGER = getString(R.string.pref_key_enable_logger_checkbox);
		PREF_KEY_ENABLE_USER_LOG = getString(R.string.pref_key_enable_user_log_checkbox);
	}
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	static String PREF_KEY_AUTO_CHECK_UPDATE;
	static String PREF_KEY_ENABLE_LOGGER;
	static String PREF_KEY_ENABLE_USER_LOG;

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {

			String key = preference.getKey();
			if (key.equals(PREF_KEY_ENABLE_LOGGER)) {
				Logger.getInstance().setDebuggable((Boolean) value);

			} else if (key.equals(PREF_KEY_ENABLE_USER_LOG)) {
				Logger.getInstance().setUserActivitiesLoggable((Boolean) value);

			}
			
			return true;
		}
	};

	private static void bindPreferenceSummaryToValue(Preference preference) {

		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		Object newValue = null;
		if (preference instanceof CheckBoxPreference) {

			newValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
										.getBoolean(preference.getKey(), false);

		} else {
			newValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
										.getString(preference.getKey(), "");
		}

		// Trigger the listener immediately with the preference's current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
	}

	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {

		return super.getSharedPreferences(name, mode);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) return;

		addPreferencesFromResource(R.xml.pref_general);
		bindPreferenceSummaryToValue(findPreference(PREF_KEY_AUTO_CHECK_UPDATE));
		bindPreferenceSummaryToValue(findPreference(PREF_KEY_ENABLE_LOGGER));
		bindPreferenceSummaryToValue(findPreference(PREF_KEY_ENABLE_USER_LOG));
		Preference findPreference = findPreference("secret_log");

		findPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingsActivity.this, UserLogActivity_.class);
				startActivity(intent);
				return false;
			}
		});
	}
}
