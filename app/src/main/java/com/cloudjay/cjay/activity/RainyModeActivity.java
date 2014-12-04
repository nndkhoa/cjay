package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.RainyModeImageAdapter;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.task.command.image.GetRainyImagesCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_rainy_mode)
public class RainyModeActivity extends Activity {
    public final static String CHECKED_IMAGES = "com.cloudjay.wizard.checked_images";

    @Extra(CHECKED_IMAGES)
    ArrayList<String> checkedImages;

    @ViewById(R.id.gv_reuse_images)
    GridView gvReuseImages;

    @ViewById(R.id.btn_input_rainy_mode)
    Button btnInputRainy;

    @ViewById(R.id.btn_done_rainy_mode)
    Button btnDoneRainy;

    @ViewById(R.id.ll_bottom_rainy_mode)
    LinearLayout rainyModeButtonLinearLayout;

    @Bean
    DataCenter dataCenter;

    // use when rainy mode is true
    RainyModeImageAdapter mAdapter = null;
    private ActionMode mActionMode;

    boolean rainyMode;

    @AfterViews
    void doAfterViews() {
        rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
                        false);

        if (mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = startActionMode(new ActionModeCallBack());
        }

        mAdapter = new RainyModeImageAdapter(this, R.layout.item_image_gridview, true);
        gvReuseImages.setAdapter(mAdapter);

        // get rainy image
        dataCenter.add(new GetRainyImagesCommand(getApplicationContext()));

        Intent intent = getIntent();
        if (null == intent.getAction()) {
            rainyModeButtonLinearLayout.setVisibility(View.VISIBLE);
        } else {
            if (intent.getAction().equals(CJayConstant.ACTION_PICK_MORE)) {
                rainyModeButtonLinearLayout.setVisibility(View.VISIBLE);
                btnDoneRainy.setVisibility(View.GONE);

                mAdapter.setCheckedImageUrls(checkedImages);
            }
        }

        gvReuseImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckablePhotoGridItemLayout layout = (CheckablePhotoGridItemLayout)
                        view.findViewById(R.id.photo_layout);
                layout.toggle();

                if (mActionMode != null)
                    mActionMode.setTitle(String.valueOf(mAdapter.getCheckedImageUrlsCount()) + " selected");

                if (mAdapter.getCheckedImageUrlsCount() > 0) {
                    rainyModeButtonLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    rainyModeButtonLinearLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    @Click(R.id.btn_input_rainy_mode)
    void buttonInputRainyClicked() {
        Intent intent = getIntent();
        if (null == intent.getAction()) {
            openWizadActivityRainy();
        } else {
            if (intent.getAction().equals(CJayConstant.ACTION_PICK_MORE)) {

                Logger.Log("size: " + mAdapter.getCheckedImageUrls().size());

                EventBus.getDefault().post(new RainyImagesGotEvent(mAdapter.getCheckedImageUrls()));
                this.finish();
            }
        }
    }

    @Click(R.id.btn_done_rainy_mode)
    void buttonDoneRainyClicked() {
        if (mAdapter.getCount() != 0) {
            showRainyDiaglog();
        } else {
            closeActivity();
        }
    }

    private void showRainyDiaglog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage(getResources().getString(R.string.warning_rainy_mode_dialog));

        builder.setPositiveButton("Quay lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                closeActivity();
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for BUTTON_NEGATIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setBackgroundResource(R.drawable.btn_green_selector);

                // Set background and text color for BUTTON_POSITIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        });
        dialog.show();

    }

    private void closeActivity() {
        this.finish();
    }

    private void openWizadActivityRainy() {
        ArrayList<String> imageUrls = mAdapter.getCheckedImageUrls();

        // Open Wizard Activity
        Intent intent = new Intent(this, WizardActivity_.class);
        intent.putExtra(WizardActivity.STEP_EXTRA, Step.IMPORT.value);
        intent.putExtra(WizardActivity.IMAGE_URLS, imageUrls);
        intent.putExtra(WizardActivity.OPEN_FROM_REUSE_ACTIVITY, true);
        intent.setAction(CJayConstant.ACTION_SEND_GATE_IMAGES);
        startActivity(intent);
        this.finish();
    }

    @UiThread
    void onEvent(RainyImagesGotEvent event) {
        ArrayList<String> imageUrls = event.getImageUrls();

        if (imageUrls != null) {
            refreshInRainyMode(imageUrls);
        }
    }

    void refreshInRainyMode(ArrayList<String> imageUrls) {
        mAdapter.clear();
        if (imageUrls != null) {
            for (String object : imageUrls) {
                if (object.contains("containerId") && object.contains("imageType")) {
                    mAdapter.add(object);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (rainyMode) {
                // Go direct to camera
                Intent cameraActivityIntent = new Intent(getApplicationContext(), CameraActivity_.class);
                cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, "");
                cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, "");
                cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
                cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.IMPORT.value);
                cameraActivityIntent.putExtra(CameraActivity_.OPEN_RAINY_MODE_ACTIVITY, true);
                startActivity(cameraActivityIntent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
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


            ArrayList<String> selected = new ArrayList<>();
            switch (menuItem.getItemId()) {
                case R.id.item_select_all:
                    if (mAdapter.getCheckedImageUrlsCount() < mAdapter.getCount()) {
                        // Do select all
                        Logger.Log("Do select all");
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            selected.add(mAdapter.getItem(i));
                        }

                        mAdapter.setCheckedImageUrls(selected);
                        mAdapter.notifyDataSetChanged();

                        Logger.Log("selected: " + selected.size());
                        actionMode.setTitle(String.valueOf(selected.size()) + " selected");
                        rainyModeButtonLinearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.item_unselect_all:
                    // Do unselect all
                    Logger.Log("Do unselect all");
                    selected.clear();
                    mAdapter.removeAllCheckedImageUrl();
                    mAdapter.notifyDataSetChanged();

                    mActionMode.setTitle(String.valueOf(mAdapter.getCheckedImageUrlsCount()) + " selected");
                    rainyModeButtonLinearLayout.setVisibility(View.GONE);

                    break;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }
    }
}
