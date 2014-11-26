package com.cloudjay.cjay.fragment.dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.activity.SettingActivity;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.session.update.AddWorkingSessionCommand;
import com.cloudjay.cjay.task.command.session.update.SaveSessionCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

@EFragment(R.layout.fragment_addcontainer)
public class AddContainerDialog extends SimpleDialogFragment {

	@FragmentArg("containerId")
	String containerId;

	@ViewById(R.id.et_container_id)
	EditText etContainerID;

    @Bean
    DataCenter dataCenter;

    Session mSession;

	@Override
	protected Builder build(final Builder builder) {
		builder.setTitle(R.string.dialog_search_container_title);

		builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_addcontainer, null));
		builder.setNegativeButton("Tạo mới", new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Get Container Id from EditText
				containerId = etContainerID.getText().toString();

				// Check container id is valid or not
				if (TextUtils.isEmpty(containerId)) {
					etContainerID.setError(getString(R.string.dialog_container_id_required));

				} else {

					// Kiểm tra containerId theo chuẩn ISO
					if (!Utils.isContainerIdValid(containerId)) {

						getNeutralButton().setVisibility(View.VISIBLE);
						etContainerID.setError(getString(R.string.dialog_container_id_invalid_iso));

					} else {

						// Hide button create container wrong ISO
						getNeutralButton().setVisibility(View.GONE);

						// Start workflow
						//Intent intent = new Intent(getActivity(), WizardActivity_.class);
						//intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
						//startActivity(intent);

                        createContainerSession();
                        openCamera();

						dismiss();
					}
				}
			}
		});

		builder.setPositiveButton("Bỏ qua", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		builder.setNeutralButton("Tạo sai ISO", new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String containerId = etContainerID.getText().toString();
				if (Utils.simpleValid(containerId)) {
					createContainerSession();
					openCamera();
				} else {
					etContainerID.setError(getString(R.string.dialog_container_id_invalid_length));
				}

			}
		});

		return builder;
	}

	/**
	 * 1. Khởi tạo các thành phần trên dialog
	 * 2. Cấu hình EditText
	 * 3. Nếu containerId truyền vào đủ 11 kí tự, thực hiện validate theo chuẩn ISO ngay lập tức
	 */
	@AfterViews
	void doAfterViews() {
		// Set search keyword into edit text
		etContainerID.setText(containerId);

		// Set background and text color for Negative button
		this.getNegativeButton().setBackgroundResource(R.drawable.btn_green_selector);
		this.getNegativeButton().setTextColor(getActivity().
				getResources().getColor(android.R.color.white));

		// Set background and text color for Neutral button
		this.getNeutralButton().setBackgroundResource(R.drawable.btn_red_selector);
		this.getNeutralButton().setTextColor(getActivity().
				getResources().getColor(android.R.color.white));

		// Hide button create container wrong ISO
		this.getNeutralButton().setVisibility(View.GONE);

		// Check containerId
		if (containerId.length() == 11) {

			if (!Utils.isContainerIdValid(containerId)) {
				getNeutralButton().setVisibility(View.VISIBLE);
				etContainerID.setError(getString(R.string.dialog_container_id_invalid_iso));
			}
		}

		Utils.setupEditText(etContainerID);
	}

    void createContainerSession() {
        // Add new session to database
        String currentTime = StringUtils.getCurrentTimestamp(CJayConstant.
                CJAY_DATETIME_FORMAT_NO_TIMEZONE);

        // Create container session
        mSession = new Session().withContainerId(containerId)
                .withLocalStep(Step.IMPORT.value)
                .withStep(Step.IMPORT.value)
                .withCheckInTime(currentTime)
                .withPreStatus(1);

        // Save normal session and working session.
        // add working session also post an event
	    dataCenter.add(new SaveSessionCommand(getActivity(), mSession));
	    dataCenter.add(new AddWorkingSessionCommand(getActivity(), mSession));
    }

    void openCamera() {
        Intent intent = new Intent(getActivity(), WizardActivity_.class);
        intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
        startActivity(intent);

        // Open camera activity
        Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
        cameraActivityIntent.putExtra(CameraActivity_.CONTAINER_ID_EXTRA, containerId);
        cameraActivityIntent.putExtra(CameraActivity_.OPERATOR_CODE_EXTRA, "");
        cameraActivityIntent.putExtra(CameraActivity_.IMAGE_TYPE_EXTRA, ImageType.IMPORT.value);
        cameraActivityIntent.putExtra(CameraActivity_.CURRENT_STEP_EXTRA, Step.IMPORT.value);
        startActivity(cameraActivityIntent);
        dismiss();
    }
}
