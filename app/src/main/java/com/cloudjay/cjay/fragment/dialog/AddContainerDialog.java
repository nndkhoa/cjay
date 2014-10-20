package com.cloudjay.cjay.fragment.dialog;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.WizardActivity;
import com.cloudjay.cjay.activity.WizardActivity_;
import com.cloudjay.cjay.util.Utils;

import org.androidannotations.annotations.AfterViews;
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

					// Kiểm tra containerID theo chuẩn ISO
					if (!Utils.isContainerIdValid(containerId)) {

						getNeutralButton().setVisibility(View.VISIBLE);
						etContainerID.setError(getString(R.string.dialog_container_id_invalid_iso));

					} else {

						// Hide button create container wrong ISO
						getNeutralButton().setVisibility(View.GONE);

						// Start workflow
						Intent intent = new Intent(getActivity(), WizardActivity_.class);
						intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
						startActivity(intent);
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

		builder.setNeutralButton("Taọ sai ISO", new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), WizardActivity_.class);
				intent.putExtra(WizardActivity.CONTAINER_ID_EXTRA, containerId);
				startActivity(intent);
				dismiss();
			}
		});

		return builder;
	}

	/**
	 * 1. Khởi tạo các thành phần trên dialog
	 * 2. Cấu hình EditText
	 * 3. Nếu containerID truyền vào đủ 11 kí tự, thực hiện validate theo chuẩn ISO ngay lập tức
	 */
	@AfterViews
	void doAfterViews() {

		// Set search keyword into edit text
		etContainerID.setText(containerId);

		// Setup keyboard
		Utils.setupEditText(etContainerID);

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

		// Check containerID
		if (containerId.length() == 11) {
			if (!Utils.isContainerIdValid(containerId)) {
				getNeutralButton().setVisibility(View.VISIBLE);
				etContainerID.setError(getString(R.string.dialog_container_id_invalid_iso));
			}
		}
	}
}
