package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.event.ContainerSearchedEvent;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_reuse)
public class ReuseActivity extends Activity {


    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @Extra(CONTAINER_ID_EXTRA)
    String containerID;

    @ViewById(R.id.btn_done)
    Button btnDone;

    @ViewById(R.id.gv_reuse_images)
    GridView gvReuseImages;

    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @Bean
    DataCenter dataCenter;

    GateImageAdapter gateImageAdapter = null;
    private ActionMode mActionMode;

    long currentStatus;
    Session mSession;

    @AfterViews
    void doAfterViews() {
        // Get session by containerId
        mSession = dataCenter.getSession(getApplicationContext(), containerID);

        if (null == mSession) {

            // Set ContainerId to TextView
            tvContainerId.setText(containerID);

        } else {

            containerID = mSession.getContainerId();

            // Set ContainerId to TextView
            tvContainerId.setText(containerID);

            // Set currentStatus to TextView
            currentStatus = mSession.getStatus();
            tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

            gateImageAdapter = new GateImageAdapter(this, R.layout.item_image_gridview, true);
            gvReuseImages.setAdapter(gateImageAdapter);

            refresh();
        }

        // Set item click event on grid view
        gvReuseImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckablePhotoGridItemLayout layout = (CheckablePhotoGridItemLayout)
                        view.findViewById(R.id.photo_layout);
                layout.toggle();
                boolean hasCheckedItems = gateImageAdapter.getCheckedCJayImageUrlsCount() > 0;

                Logger.Log("count : " + gateImageAdapter.getCheckedCJayImageUrlsCount());

                if (hasCheckedItems && mActionMode == null) {
                    // there are some selected items, start the actionMode
                    mActionMode = startActionMode(new ActionModeCallBack());
                } else if (!hasCheckedItems && mActionMode != null) {
                    // there no selected items, finish the actionMode
                    mActionMode.finish();
                }

                if (mActionMode != null)
                    mActionMode.setTitle(String.valueOf(gateImageAdapter.getCheckedCJayImageUrlsCount()) + " selected");

            }
        });

    }

    @Click(R.id.btn_done)
    void buttonDoneClicked() {
        List<GateImage> gateImages = gateImageAdapter.getCheckedCJayImageUrls();
        for (int i = 0; i < gateImages.size(); i++) {
            try {
				// Getting the last part of the referrer url
				String name = gateImages.get(i).getUrl().substring(gateImages.get(i).getUrl().lastIndexOf("/") + 1,
						gateImages.get(i).getUrl().length());
                // Create new audit image object
                AuditImage auditImage = new AuditImage()
                        .withId(0)
                        .withType(ImageType.AUDIT)
                        .withUrl(gateImages.get(i).getUrl())
                        .withName(name)
						.withUUID(UUID.randomUUID().toString());

                dataCenter.addAuditImage(getApplicationContext(), auditImage, containerID);
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);

        EventBus.getDefault().post(new ImageCapturedEvent(containerID, ImageType.AUDIT, null));
        this.finish();
    }

    @Background
    void refresh() {
        if (mSession != null) {
            List<AuditItem> auditItems = mSession.getAuditItems();
            List<GateImage> importImages = mSession.getImportImages();
            List<GateImage> deletedImportImages = new ArrayList<GateImage>();

            if (auditItems != null) {
                Logger.Log("Size: " + auditItems.size());
                for (GateImage gateImage : importImages) {
                    for (AuditItem auditItem : auditItems) {

                        if (auditItem.getAuditImages() != null) {
                            for (AuditImage auditImage : auditItem.getAuditImages()) {
                                if (auditImage.getUrl().equals(gateImage.getUrl())) {
                                    deletedImportImages.add(gateImage);
                                }
                            }
                        }
                    }
                }
            }

            importImages.removeAll(deletedImportImages);

            updatedData(importImages);
        }
    }

    @UiThread
    public void updatedData(List<GateImage> importImages) {
        Logger.Log("Size: " + importImages.size());
        gateImageAdapter.clear();
        if (importImages != null) {
            for (GateImage object : importImages) {
                gateImageAdapter.add(object);
            }
        }
        gateImageAdapter.notifyDataSetChanged();
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            // inflate contextual menu
            actionMode.getMenuInflater().inflate(R.menu.contextual_grid_view, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            ArrayList<GateImage> selected = new ArrayList<GateImage>();
            switch (menuItem.getItemId()) {
                case R.id.item_select_all:
                    if (gateImageAdapter.getCheckedCJayImageUrlsCount() < gateImageAdapter.getCount()) {
                        // Do select all
                        Logger.Log("Do select all");
                        for (int i = 0; i < gateImageAdapter.getCount(); i++) {
                            selected.add(gateImageAdapter.getItem(i));
                        }

                        gateImageAdapter.setCheckedCJayImageUrls(selected);
                        gateImageAdapter.notifyDataSetChanged();

                        Logger.Log("selected: " + selected.size());
                        actionMode.setTitle(String.valueOf(selected.size()) + " selected");

                    }
                    break;
                case R.id.item_unselect_all:
                    // Do unselect all
                    Logger.Log("Do unselect all");
                    selected.clear();
                    gateImageAdapter.removeAllCheckedCJayImageUrl();
                    gateImageAdapter.notifyDataSetChanged();

                    actionMode.finish();
                    break;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            // remove selection
            mActionMode = null;
        }
    }

}
