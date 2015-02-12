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
import com.cloudjay.cjay.event.pubnub.PubnubSubscriptionChangedEvent;
import com.cloudjay.cjay.event.session.ContainersFetchedEvent;
import com.cloudjay.cjay.task.job.FetchSessionsJob;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
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

    @OptionsMenuItem(R.id.menu_subcribe_pubnub)
    MenuItem menuPubnubStatus;

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

    @OptionsItem(R.id.menu_subcribe_pubnub)
    void subscribePubnubItemClicked() {
        boolean connected = Utils.canReachInternet();
        boolean alarmUp = Utils.isAlarmUp(this);
        if (connected && !alarmUp) {
            Utils.startAlarm(this);
        } else if (!connected) {
            Utils.showCrouton(this, getResources().getString(R.string.error_try_again), Style.ALERT);
        } else if (alarmUp) {
            Utils.showCrouton(this, getResources().getString(R.string.info_notification_is_working), Style.INFO);
        }
    }

    @OptionsItem(R.id.menu_refresh)
    void refreshItemClicked() {

        // 1. clear preferences
        PreferencesUtil.removePrefsValue(getApplicationContext(), PreferencesUtil.PREF_MODIFIED_PAGE);
        PreferencesUtil.removePrefsValue(getApplicationContext(), PreferencesUtil.PREF_MODIFIED_DATE);
        PreferencesUtil.removePrefsValue(getApplicationContext(), PreferencesUtil.PREF_FIRST_PAGE_MODIFIED_DATE);

        // 2. fetch page sessions again
        String lastModifiedDate = PreferencesUtil.getPrefsValue(this, PreferencesUtil.PREF_MODIFIED_DATE);
        if (lastModifiedDate.isEmpty()) {
            JobManager jobManager = App.getJobManager();
            jobManager.addJobInBackground(new FetchSessionsJob(lastModifiedDate));
        }
    }

    protected void showLogoutPrompt() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.logout_prompt_title);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Utils.logOut(getApplicationContext());

                // Open Login Activity
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity_.class);
                startActivity(loginIntent);
                EventBus.getDefault().post(new UserLoggedOutEvent());
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

    @UiThread
    public void onEvent(PubnubSubscriptionChangedEvent event) {
        try {
            boolean isSubscribed = event.isSubscribed();
            if (!isSubscribed) {
                menuPubnubStatus.setIcon(getResources().getDrawable(R.drawable.ic_red));
            } else {
                menuPubnubStatus.setIcon(getResources().getDrawable(R.drawable.ic_green));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
