package com.cloudjay.cjay.fragment;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.event.issue.RepairedItemsGotEvent;
import com.cloudjay.cjay.event.session.ContainerForUploadGotEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.session.ContainerGotParentFragmentEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.cjayobject.AddUploadObjectCommand;
import com.cloudjay.cjay.task.command.issue.UpdateAuditItemCommand;
import com.cloudjay.cjay.task.command.session.get.GetSessionCommand;
import com.cloudjay.cjay.task.command.session.get.GetSessionForUploadCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.ChangeSessionLocalStepCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_audit_repair)
public class AuditAndRepairFragment extends Fragment implements ActionBar.TabListener {

    //region ATTR
    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
    public final static String TAB_TYPE_EXTRA = "com.cloudjay.wizard.tabtype";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @FragmentArg(TAB_TYPE_EXTRA)
    public int tabType;

    @Bean
    DataCenter dataCenter;

    @ViewById(R.id.pager)
    ViewPager pager;

    @ViewById(R.id.btn_complete_repair)
    Button btnCompleteRepair;

    @ViewById(R.id.btn_complete_audit)
    Button btnCompleteAudit;

    ActionBar actionBar;
    private ViewPagerAdapter mPagerAdapter;
    public int currentPosition = 0;

    private boolean mIsUploading = false;

    Session mSession;

    //endregion

    public AuditAndRepairFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.Log("onResume");
        dataCenter.add(new GetSessionCommand(getActivity(), containerID));
    }

    @AfterViews
    void setUp() {
        configureActionBar();
        configureViewPager();
    }

    private void configureViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getActivity(),
                getActivity().getSupportFragmentManager(), tabType);
        pager.setAdapter(mPagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                ActionBar.Tab tab = actionBar.getTabAt(position);
                actionBar.selectTab(tab);
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

    private void configureActionBar() {
        // Get actionbar
        actionBar = getActivity().getActionBar();

        // Set ActionBar Title
        actionBar.setTitle(R.string.fragment_repair_title);

        // Fix tab layout
        final Method method;
        try {
            method = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
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

    @UiThread
    void checkForShowButton() {

        Logger.Log("on checkForShowButton");

        if (mSession.getLocalStep() == Step.REPAIR.value) {

            for (AuditItem auditItem : mSession.getAuditItems()) {
                if (auditItem.getId() == 0) {
                    dataCenter.add(new ChangeSessionLocalStepCommand(getActivity(), containerID, Step.AUDIT));
                    btnCompleteAudit.setVisibility(View.VISIBLE);
                    btnCompleteRepair.setVisibility(View.VISIBLE);
                    return;
                }
            }

            btnCompleteAudit.setVisibility(View.GONE);
            btnCompleteRepair.setVisibility(View.VISIBLE);
        }

        if (mSession.getLocalStep() == Step.AUDIT.value) {
            int countItemUploading = 0;
            if (mSession.hasRepairImages()) {
                btnCompleteAudit.setVisibility(View.VISIBLE);
                btnCompleteRepair.setVisibility(View.VISIBLE);
            } else {

                for (AuditItem item : mSession.getAuditItems()){
                    if (item.getId() == 0 && item.getUploadStatus() == UploadStatus.UPLOADING.value) {
                        countItemUploading++;
                    }
                }

                if (countItemUploading != 0 && countItemUploading == mSession.getAuditItems().size()) {
                    mIsUploading = true;
                }

                if (mIsUploading) {
                    Logger.Log("size count: " + countItemUploading);
                    btnCompleteAudit.setVisibility(View.GONE);
                } else {
                    btnCompleteAudit.setVisibility(View.VISIBLE);
                }
                btnCompleteRepair.setVisibility(View.GONE);
            }
        }
    }

    @Click(R.id.btn_complete_audit)
    void btnCompleteAuditClicked() {
        Logger.w("btnCompleteAuditClicked");
        Logger.w("step: " + mSession.getLocalStep());
        dataCenter.add(new GetSessionForUploadCommand(getActivity(), containerID));
    }

    @Click(R.id.btn_complete_repair)
    void btnCompleteRepairClicked() {
        Logger.w("btnCompleteRepairClicked");
        dataCenter.add(new GetSessionForUploadCommand(getActivity(), containerID));
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        pager.setCurrentItem(position);
        currentPosition = position;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @UiThread
    public void onEvent(ContainerForUploadGotEvent event) {
        Logger.Log("on ContainerForUploadGotEvent");
        mSession = event.getTarget();

        // Check session is null or not
        if (mSession != null) {
            if (!mSession.isValidToUpload(Step.AUDIT)) {
                Utils.showCrouton(getActivity(), getResources()
                        .getString(R.string.warning_container_invalid));
                return;
            }
        } else {
            Utils.showCrouton(getActivity(), "Không tìm thấy container " + containerID);
            return;
        }

        Logger.w("step: " + mSession.getLocalStep());

        // Xu ly cho session da duoc Giam Dinh
        if (mSession.getLocalStep() == Step.AUDIT.value) {

            // Hide this button
            btnCompleteAudit.setVisibility(View.GONE);

            // Navigate to HomeActivity
            getActivity().finish();

            if (mSession.getId() == 0) {
                for (AuditItem auditItem : mSession.getAuditItems()) {
                    if (auditItem.getId() == 0 && auditItem.isAudited()) {
                        Logger.Log("Set upload confirmed for audit item: " + auditItem.toString());
                        auditItem.setUploadConfirmed(true);
                        dataCenter.add(new UpdateAuditItemCommand(getActivity(), mSession.getContainerId(), auditItem));
                    }
                }
            } else {
                for (AuditItem auditItem : mSession.getAuditItems()) {

                    if (auditItem.getId() == 0 || auditItem.getUploadStatus() == UploadStatus.NONE.value) {
                        // If audit item has not been uploaded yet
                        // Add container session to upload queue
                        Logger.Log("upload audit item with container id: " + mSession.getId());
                        auditItem.setSession(mSession.getId());
                        UploadObject object = new UploadObject(auditItem, AuditItem.class, containerID, mSession.getId());
                        dataCenter.add(new AddUploadObjectCommand(getActivity().getApplicationContext(), object));
                    }
                }

                mSession.prepareForUploading();
                dataCenter.add(new SaveSessionCommand(getActivity().getApplicationContext(), mSession));

                UploadObject object = new UploadObject(mSession, Session.class, mSession.getContainerId());
                dataCenter.add(new AddUploadObjectCommand(getActivity().getApplicationContext(), object));

                // Check if this session has repair image or not
                if (mSession.hasRepairImages()) {
                    btnCompleteRepair.setVisibility(View.VISIBLE);
                } else {

                    // Remove from working session
                    dataCenter.add(new RemoveWorkingSessionCommand(getActivity(), containerID));
                }
            }
        } else if (mSession.getLocalStep() == Step.REPAIR.value) {

            // Hide this button
            btnCompleteRepair.setVisibility(View.GONE);

            // Navigate to HomeActivity
            getActivity().finish();

            // Xu ly cho session da duoc sua chua
            if (mSession != null) {

                if (mSession.getLocalStep() == Step.AUDIT.value) {
                    Utils.showCrouton(getActivity(), getResources().getString(R.string.warning_container_invalid));
                    return;
                }

                if (!mSession.isValidToUpload(Step.REPAIR)) {
                    Utils.showCrouton(getActivity(), getResources().getString(R.string.warning_container_invalid));
                    return;
                }
            } else {
                Utils.showCrouton(getActivity(), "Sth goes wrong. Container Id " + containerID + " not found");
            }

            // Remove from working session
            dataCenter.add(new RemoveWorkingSessionCommand(getActivity(), containerID));

            mSession.prepareForUploading();
            dataCenter.add(new SaveSessionCommand(getActivity(), mSession));

            // Add containerId to upload complete repair queue
            // PUT /api/cjay/containers/{pk}/complete-repair
            UploadObject object = new UploadObject(mSession, Session.class, mSession.getContainerId());
            dataCenter.add(new AddUploadObjectCommand(getActivity(), object));
        }
    }

    public void onEventMainThread(ContainerGotEvent event) {
        mSession = event.getSession();
        checkForShowButton();
        EventBus.getDefault().post(new ContainerGotParentFragmentEvent(mSession));
        EventBus.getDefault().post(new AuditItemsGotEvent(mSession.getAuditItems()));
        EventBus.getDefault().post(new RepairedItemsGotEvent(mSession.getListRepairedItem()));
    }
}