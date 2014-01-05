package com.cloudjay.cjay;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.cloudjay.cjay.service.UploadIntentService_;

public class UploadAlertDialogActivity extends CJayActivity {
	private NotificationManager nm;
	private String uuid;
	private String tmpImgUri;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			uuid = extras.getString("uuid");
			tmpImgUri = extras.getString("tmpImgUri");
		}

		displayAlert();
	}

	private void displayAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.lbl_confirm_reupload_delete)
				.setCancelable(false)
				.setPositiveButton(R.string.btn_reupload,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								Intent uploadIntent = new Intent(
										UploadAlertDialogActivity.this,
										UploadIntentService_.class);
								uploadIntent.putExtra("uuid", uuid);
								uploadIntent.putExtra("tmpImgUri", tmpImgUri);
								startService(uploadIntent);

								dialog.dismiss();
								UploadAlertDialogActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								UploadAlertDialogActivity.this.finish();
								// nm.cancel(notificationTag, 0);
							}
						})
				.setNeutralButton(R.string.btn_delete,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								dialog.dismiss();
								nm.cancel(uuid, 0);
								UploadAlertDialogActivity.this.finish();
							}
						}).setMessage(R.string.btn_retry_message);
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}