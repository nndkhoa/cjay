package com.cloudjay.cjay.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.EActivity;

/**
 * Created by thai on 07/11/2014.
 */
@EActivity
public class SettingActivity extends PreferenceActivity {

    public static String PREF_KEY_AUTO_CHECK_UPDATE;
    public static String PREF_KEY_RAINY_MODE;
    public static String PREF_KEY_ENABLE_LOGGER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PREF_KEY_AUTO_CHECK_UPDATE = getString(R.string.pref_key_auto_check_update_checkbox);
        PREF_KEY_RAINY_MODE = getString(R.string.pref_key_enable_temporary_fragment_checkbox);
        PREF_KEY_ENABLE_LOGGER = getString(R.string.pref_key_enable_logger_checkbox);

        setupSimplePreferencesScreen();
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_AUTO_CHECK_UPDATE));
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_RAINY_MODE));
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_ENABLE_LOGGER));

        Preference findPreference = findPreference("notification_log");

        findPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
	            // Open Wizard Activity
	            Intent intent = new Intent(App.getInstance().getApplicationContext(), LogActivity_.class);
	            intent.putExtra(LogActivity.LOG_TYPE_EXTRA, CJayConstant.PREFIX_NOTIFI_LOG);
	            startActivity(intent);
                return false;
            }
        });
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String key = preference.getKey();
            if (key.equals(PREF_KEY_ENABLE_LOGGER)) {
                Logger.getInstance().setDebuggable((Boolean) value);
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

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }
}
