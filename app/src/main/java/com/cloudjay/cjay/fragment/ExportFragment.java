package com.cloudjay.cjay.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.cjayobject.AddUploadObjectCommand;
import com.cloudjay.cjay.task.command.session.get.GetSessionCommand;
import com.cloudjay.cjay.task.command.session.remove.RemoveWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.AddUploadingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Màn hình xuất
 */
@EFragment(R.layout.fragment_export)
@OptionsMenu(R.menu.back_audit)
public class ExportFragment extends Fragment {

    //region ATTR
    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

    @FragmentArg(CONTAINER_ID_EXTRA)
    String containerID;

    @Bean
    DataCenter dataCenter;

    GateImageAdapter gateImageAdapter = null;

    PhotoExpandableListAdapter mListAdapter;
    int[] mImageTypes;

    List<GateImage> importImages = new ArrayList<GateImage>();
    List<AuditImage> auditImages = new ArrayList<AuditImage>();
    List<AuditImage> repairedImages = new ArrayList<AuditImage>();
    List<GateImage> exportImages = new ArrayList<GateImage>();

    String operatorCode;
    long preStatus;
    long currentStatus;
    Session mSession;
    //endregion

    //region VIEWS
    @ViewById(R.id.tv_container_code)
    TextView tvContainerId;

    @ViewById(R.id.btn_take_export_picture)
    LinearLayout btnTakeExportPicture;

    @ViewById(R.id.gv_images)
    ListView gvExportImages;

    @ViewById(R.id.btn_view_previous_step)
    Button btnViewPreviousSteps;

    @ViewById(R.id.btn_complete_import)
    Button btnComplete;

    @ViewById(R.id.tv_status_name)
    TextView tvPreStatus;

    @ViewById(R.id.tv_current_status)
    TextView tvCurrentStatus;

    @ViewById(R.id.lv_images_expandable)
    ExpandableListView lvImagesExpandable;
    //endregion

    public ExportFragment() {
    }

    @AfterViews
    void setUp() {

        // Set ActionBar Title
        getActivity().getActionBar().setTitle(R.string.fragment_export_title);

        gateImageAdapter = new GateImageAdapter(getActivity(), R.layout.item_image_gridview, false);
        gvExportImages.setAdapter(gateImageAdapter);

        // Init image types
        mImageTypes = new int[3];
        mImageTypes[0] = ImageType.IMPORT.value;
        mImageTypes[1] = ImageType.AUDIT.value;
        mImageTypes[2] = ImageType.REPAIRED.value;

        mListAdapter = new PhotoExpandableListAdapter(getActivity(),
                mImageTypes, importImages, auditImages, repairedImages);
        lvImagesExpandable.setAdapter(mListAdapter);

//		dataCenter.add(new GetSessionCommand(getActivity(), containerID));
    }

    //region VIEW INTERACTION
    @Click(R.id.btn_take_export_picture)
    void buttonTakeExportPictureClicked() {

        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerID);
        cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.EXPORT.value);
        cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, operatorCode);
        cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.EXPORTED.value);
        startActivity(cameraActivityIntent);
    }

    @Click(R.id.btn_view_previous_step)
    void buttonViewPreClicked() {
        lvImagesExpandable.setVisibility(lvImagesExpandable.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        gvExportImages.setVisibility(lvImagesExpandable.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @Click(R.id.btn_complete_import)
    void btnCompleteClicked() {
        if (mSession.isValidToUpload(Step.EXPORTED) == false) {
            Utils.showCrouton(getActivity(), "Container chưa được báo cáo đầy đủ");
            return;
        }

        // Add to Uploading
        dataCenter.add(new AddUploadingSessionCommand(getActivity(), mSession));
        dataCenter.add(new RemoveWorkingSessionCommand(getActivity(), containerID));

        mSession.prepareForUploading();
        dataCenter.add(new SaveSessionCommand(getActivity(), mSession));

        // Add container session to upload queue
        UploadObject object = new UploadObject(mSession, Session.class, mSession.getContainerId());
        dataCenter.add(new AddUploadObjectCommand(getActivity(), object));

        // Navigate to HomeActivity
        getActivity().finish();
    }

    @OptionsItem(R.id.menu_back_audit)
    void exportMenuItemClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert");
        builder.setMessage("Quay về bước giám định?");

        builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Back to audit fragment
                dialogInterface.dismiss();
                dataCenter.add(new BackToAuditCommand(getActivity(), containerID));
                getActivity().finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                // Set background and text color for BUTTON_NEGATIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setBackgroundResource(R.drawable.btn_green_selector);

                // Set background and text color for BUTTON_POSITIVE
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getActivity().getResources().getColor(android.R.color.white));
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setBackgroundColor(getActivity().getResources().getColor(android.R.color.darker_gray));
            }
        });
        dialog.show();
    }

    //endregion

    void refresh() {
        if (mSession != null) {

            // Get import and export images by containerId
            importImages = mSession.getImportImages();
            exportImages = mSession.getExportImages();

            // Get audit and repaired images by containerId
            auditImages = mSession.getIssueImages();
            repairedImages = mSession.getRepairedImages();

            updatedGridView();
            updateExpandableListView();
        }
    }

    @UiThread
    public void updatedGridView() {
        gateImageAdapter.clear();
        if (importImages != null) {
            for (GateImage object : exportImages) {
                gateImageAdapter.add(object);
            }
        }
        gateImageAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void updateExpandableListView() {
        mListAdapter.mImportImages.clear();
        mListAdapter.mAuditImages.clear();
        mListAdapter.mRepairedImages.clear();

        if (importImages != null) {
            mListAdapter.mImportImages.addAll(importImages);
        }

        if (auditImages != null) {
            mListAdapter.mAuditImages.addAll(auditImages);
        }

        if (repairedImages != null) {
            mListAdapter.mRepairedImages.addAll(repairedImages);
        }

        mListAdapter.notifyDataSetChanged();
    }

    //region EVENT HANDLER
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @UiThread
    public void onEvent(ContainerGotEvent event) {

        // Get session
        mSession = event.getSession();

        if (null == mSession) {
            // Set ContainerId to TextView
            tvContainerId.setText(containerID);
        } else {
            // Get operator code
            containerID = mSession.getContainerId();
            operatorCode = mSession.getOperatorCode();

            // Set preStatus to TextView
            preStatus = mSession.getPreStatus();
            tvPreStatus.setText((Status.values()[(int) preStatus]).toString());

            // Set currentStatus to TextView
            currentStatus = mSession.getStatus();
            tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

            // Set ContainerId to TextView
            tvContainerId.setText(containerID);

            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dataCenter.add(new GetSessionCommand(getActivity(), containerID));
    }


    //endregion

}
