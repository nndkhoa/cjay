package com.cloudjay.cjay;

import java.util.List;

import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;

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

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design
 * guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for
 * more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PREF_KEY_AUTO_CHECK_UPDATE = getString(R.string.pref_key_auto_check_update_checkbox);
		PREF_KEY_ENABLE_LOGGER = getString(R.string.pref_key_enable_logger_checkbox);
		PREF_KEY_ENABLE_USER_LOG = getString(R.string.pref_key_enable_user_log_checkbox);
	}

	// /**
	// * This fragment shows general preferences only. It is used when the
	// * activity is showing a two-pane settings UI.
	// */
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// public static class GeneralPreferenceFragment extends PreferenceFragment {
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	//
	// Logger.Log("onCreate GeneralPreferenceFragment");
	//
	// super.onCreate(savedInstanceState);
	// addPreferencesFromResource(R.xml.pref_general);
	//
	// // bindPreferenceSummaryToValue(findPreference("auto_check_update_checkbox"));
	// // bindPreferenceSummaryToValue(findPreference("enable_logger_checkbox"));
	// // bindPreferenceSummaryToValue(findPreference("enable_user_log_checkbox"));
	//
	// bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_auto_check_update_checkbox)));
	// bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_enable_logger_checkbox)));
	// bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_enable_user_log_checkbox)));
	// }
	// }

	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	static String PREF_KEY_AUTO_CHECK_UPDATE;
	static String PREF_KEY_ENABLE_LOGGER;
	static String PREF_KEY_ENABLE_USER_LOG;

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {

			String key = preference.getKey();
			if (key.equals(PREF_KEY_ENABLE_LOGGER)) {
				Logger.getInstance().setDebuggable((Boolean) value);

			} else if (key.equals(PREF_KEY_ENABLE_USER_LOG)) {
				Logger.getInstance().setUserActivitiesLoggable((Boolean) value);

			}

			// if (preference instanceof ListPreference) {
			// // For list preferences, look up the correct display value in
			// // the preference's 'entries' list.
			// ListPreference listPreference = (ListPreference) preference;
			// int index = listPreference.findIndexOfValue(stringValue);
			//
			// // Set the summary to reflect the new value.
			// preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
			//
			// } else if (preference instanceof RingtonePreference) {
			// // For ringtone preferences, look up the correct display value
			// // using RingtoneManager.
			// if (TextUtils.isEmpty(stringValue)) {
			// // Empty values correspond to 'silent' (no ringtone).
			// preference.setSummary(R.string.pref_ringtone_silent);
			//
			// } else {
			// Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
			//
			// if (ringtone == null) {
			// // Clear the summary if there was a lookup error.
			// preference.setSummary(null);
			// } else {
			// // Set the summary to reflect the new ringtone display
			// // name.
			// String name = ringtone.getTitle(preference.getContext());
			// preference.setSummary(name);
			// }
			// }
			//
			// } else {
			// // For all other preferences, set the summary to the value's
			// // simple string representation.
			// preference.setSummary(stringValue);
			//
			// }

			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
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

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
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
