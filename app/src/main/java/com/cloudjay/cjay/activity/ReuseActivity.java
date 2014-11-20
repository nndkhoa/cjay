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
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.RainyModeImageAdapter;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.fragment.CameraFragment;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;

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


	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
    public final static String CHECKED_IMAGES = "com.cloudjay.wizard.checked_images";

	@Extra(CONTAINER_ID_EXTRA)
	String containerID;

    @Extra(CHECKED_IMAGES)
    ArrayList<String> checkedImages;

	@ViewById(R.id.btn_done)
	Button btnDone;

	@ViewById(R.id.gv_reuse_images)
	GridView gvReuseImages;

	@ViewById(R.id.tv_container_code)
	TextView tvContainerId;

    @ViewById(R.id.tv_divider_line)
    TextView tvDividerLine;

	@ViewById(R.id.tv_current_status)
	TextView tvCurrentStatus;

    @ViewById(R.id.v_line)
    View vLine;

	@ViewById(R.id.btn_input_rainy_mode)
	Button btnInputRainy;

	@ViewById(R.id.btn_done_rainy_mode)
	Button btnDoneRainy;

    @ViewById(R.id.ll_bottom_rainy_mode)
    LinearLayout rainyModeButtonLinearLayout;

    @ViewById(R.id.ll_bottom)
    LinearLayout buttonLinearLayout;

	@Bean
	DataCenter dataCenter;

    GateImageAdapter gateImageAdapter = null;
    // use when rainy mode is true
	RainyModeImageAdapter mAdapter = null;
	private ActionMode mActionMode;

	long currentStatus;
	Session mSession;

	boolean rainyMode;

	@AfterViews
	void doAfterViews() {
        rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
                        false);
        if (!rainyMode) {

            buttonLinearLayout.setVisibility(View.VISIBLE);
            rainyModeButtonLinearLayout.setVisibility(View.GONE);

            dataCenter.getSessionInBackground(this, containerID);
        } else {

            tvContainerId.setVisibility(View.GONE);
            tvDividerLine.setVisibility(View.GONE);
            tvCurrentStatus.setVisibility(View.GONE);
            vLine.setVisibility(View.GONE);

            buttonLinearLayout.setVisibility(View.GONE);

            if (mActionMode == null) {
                // there are some selected items, start the actionMode
                mActionMode = startActionMode(new ActionModeCallBack());
            }

            // Hide container id textview
            tvContainerId.setVisibility(View.INVISIBLE);
            tvCurrentStatus.setVisibility(View.INVISIBLE);

            mAdapter = new RainyModeImageAdapter(this, R.layout.item_image_gridview, true);
            gvReuseImages.setAdapter(mAdapter);

            // get rainy image
            dataCenter.getRainyImages(getApplicationContext());

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
        }

        // Set item click event on grid view
        gvReuseImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CheckablePhotoGridItemLayout layout = (CheckablePhotoGridItemLayout)
                        view.findViewById(R.id.photo_layout);
                layout.toggle();

                if (!rainyMode) {
                    if (mActionMode != null)
                        mActionMode.setTitle(String.valueOf(gateImageAdapter.getCheckedCJayImageUrlsCount()) + " selected");
                } else {
                    if (mActionMode != null)
                        mActionMode.setTitle(String.valueOf(mAdapter.getCheckedImageUrlsCount()) + " selected");

                    if (mAdapter.getCheckedImageUrlsCount() > 0) {
                        rainyModeButtonLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        rainyModeButtonLinearLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
	}

	@UiThread
	public void onEvent(ContainerGotEvent event) {

		// Get session by containerId
		mSession = event.getSession();

		if (mActionMode == null) {
			// there are some selected items, start the actionMode
			mActionMode = startActionMode(new ActionModeCallBack());
		}

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
	}

	@Click(R.id.btn_done)
	void buttonDoneClicked() {
        donePickImage();
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
        ArrayList<String> imageUrls  = mAdapter.getCheckedImageUrls();

		// Open Wizard Activity
		Intent intent = new Intent(this, WizardActivity_.class);
		intent.putExtra(WizardActivity.STEP_EXTRA, Step.IMPORT.value);
        intent.putExtra(WizardActivity.IMAGE_URLS, imageUrls);
        intent.setAction(CJayConstant.ACTION_SEND_GATE_IMAGES);
		startActivity(intent);
		this.finish();
	}

	private void donePickImage() {
        List<GateImage> gateImages = gateImageAdapter.getCheckedCJayImageUrls();
        for (int i = 0; i < gateImages.size(); i++) {
            // Getting the last part of the referrer url
            String name = gateImages.get(i).getName();
            Logger.Log("name: " + name);
            // Create new audit image object
            AuditImage auditImage = new AuditImage()
                    .withId(0)
                    .withType(ImageType.AUDIT)
                    .withUrl(gateImages.get(i).getUrl())
                    .withName(name)
                    .withUUID(UUID.randomUUID().toString());

            dataCenter.addAuditImage(getApplicationContext(), auditImage, containerID);
        }

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);

//      EventBus.getDefault().post(new ImageCapturedEvent(containerID, ImageType.AUDIT, null));
        this.finish();
	}

	@Background
	void refresh() {
		if (mSession != null) {
			List<AuditItem> auditItems = mSession.getAuditItems();
			List<GateImage> importImages = mSession.getImportImages();
			List<GateImage> deletedImportImages = new ArrayList<GateImage>();

			if (auditItems != null) {
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (rainyMode) {
                // Go direct to camera
                Intent cameraActivityIntent = new Intent(getApplicationContext(), CameraActivity_.class);
                cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, "");
                cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, "");
                cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
                cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.IMPORT.value);
                startActivity(cameraActivityIntent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
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

            if (!rainyMode) {

                ArrayList<GateImage> selected = new ArrayList<>();
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

                        mActionMode.setTitle(String.valueOf(gateImageAdapter.getCheckedCJayImageUrlsCount()) + " selected");

                        break;
                }
            } else {
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
            }

			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
            if (!rainyMode) {
                donePickImage();
            }
		}
	}

}
