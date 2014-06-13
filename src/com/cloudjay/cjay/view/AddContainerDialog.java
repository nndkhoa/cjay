package com.cloudjay.cjay.view;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;

@SuppressLint("DefaultLocale")
public class AddContainerDialog extends SherlockDialogFragment {

	public interface AddContainerDialogListener {
		void OnContainerInputCompleted(Fragment parent, String containerId, String operatorName, int mode);
	}

	private AddContainerDialogListener mCallback;

	public final static int CONTAINER_DIALOG_ADD = 0;
	public final static int CONTAINER_DIALOG_EDIT = 1;

	private String mContainerId;
	private String mOperatorName;
	private int mMode;
	private Fragment mParent;
	public boolean isOperatorRequired = true;

	TextView mOperatorLabel;
	EditText mContainerEditText;
	EditText mOperatorEditText;
	Button mCancelButton;
	Button mOkButton;
	Button mForceOkButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_new_container, container);

		mContainerEditText = (EditText) view.findViewById(R.id.dialog_new_container_id);
		mOperatorEditText = (EditText) view.findViewById(R.id.dialog_new_container_owner);
		mForceOkButton = (Button) view.findViewById(R.id.dialog_new_container_force_ok);
		mCancelButton = (Button) view.findViewById(R.id.dialog_new_container_cancel);
		mOkButton = (Button) view.findViewById(R.id.dialog_new_container_ok);
		mOperatorLabel = (TextView) view.findViewById(R.id.dialog_new_container_owner_label);

		if (mContainerId != null) {
			mContainerEditText.setText(mContainerId);
		}
		if (mOperatorName != null) {
			mOperatorEditText.setText(mOperatorName);
		}

		mContainerEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() < 4) {
					if (mContainerEditText.getInputType() != InputType.TYPE_CLASS_TEXT) {
						mContainerEditText.setInputType(InputType.TYPE_CLASS_TEXT
								| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					}

				} else {
					if (mContainerEditText.getInputType() != InputType.TYPE_CLASS_NUMBER) {
						mContainerEditText.setInputType(InputType.TYPE_CLASS_NUMBER
								| InputType.TYPE_NUMBER_VARIATION_NORMAL);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		mOperatorEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					startSearchOperator();
				}
			}
		});

		mOperatorEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					startSearchOperator();
				}
				return true;
			}
		});

		mOperatorEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					startSearchOperator();
					return true;
				}
				return false;
			}
		});

		mForceOkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mContainerId = mContainerEditText.getText().toString().toUpperCase();
				mOperatorName = mOperatorEditText.getText().toString();

				SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity()).getWritableDatabase();
				String sql = "select * from csview where container_id like ?";
				Cursor cursor = db.rawQuery(sql, new String[] { mContainerId });

				// TODO: add new container session based on existed container
				if (cursor.moveToFirst()) {
					mContainerEditText.setError(getString(R.string.dialog_container_existed));
					return;
				}
				cursor.close();

				mContainerEditText.setError(null);
				mOperatorEditText.setError(null);

				if (TextUtils.isEmpty(mContainerId)) {

					mContainerEditText.setError(getString(R.string.dialog_container_id_required));

				} else if (isOperatorRequired && TextUtils.isEmpty(mOperatorName)) {

					mOperatorEditText.setError(getString(R.string.dialog_container_owner_required));

				} else {

					mCallback = (AddContainerDialogListener) getActivity();
					mCallback.OnContainerInputCompleted(mParent, mContainerId, mOperatorName, mMode);
					dismiss();
				}

				mForceOkButton.setVisibility(View.GONE);

			}
		});

		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		mOkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mContainerId = mContainerEditText.getText().toString().toUpperCase();
				mOperatorName = mOperatorEditText.getText().toString();

				SQLiteDatabase db = DataCenter.getDatabaseHelper(getActivity()).getWritableDatabase();
				String sql = "select * from csview where container_id like ?";
				Cursor cursor = db.rawQuery(sql, new String[] { mContainerId });

				// TODO: add new container session based on existed container
				if (cursor.moveToFirst()) {
					mContainerEditText.setError(getString(R.string.dialog_container_existed));
					return;
				}

				cursor.close();

				if (!Utils.isContainerIdValid(mContainerId)) {
					mContainerEditText.setError(getString(R.string.dialog_container_id_invalid));
					mForceOkButton.setVisibility(View.VISIBLE);
					return;
				}

				mContainerEditText.setError(null);
				mOperatorEditText.setError(null);

				if (TextUtils.isEmpty(mContainerId)) {
					mContainerEditText.setError(getString(R.string.dialog_container_id_required));

				} else if (isOperatorRequired && TextUtils.isEmpty(mOperatorName)) {

					mOperatorEditText.setError(getString(R.string.dialog_container_owner_required));

				} else {
					mCallback = (AddContainerDialogListener) getActivity();
					mCallback.OnContainerInputCompleted(mParent, mContainerId, mOperatorName, mMode);
					dismiss();
				}
			}

		});

		getDialog().setTitle(getResources().getString(R.string.dialog_new_container_title));

		// show keyboard
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		if (!isOperatorRequired) {
			LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) mOperatorEditText.getLayoutParams();
			p.height = 0;
			mOperatorEditText.setLayoutParams(p);
			p = (LinearLayout.LayoutParams) mOperatorLabel.getLayoutParams();
			p.height = 0;
			mOperatorLabel.setLayoutParams(p);
		}

		return view;
	}

	public void setContainerId(String containerId) {
		mContainerId = containerId;
	}

	public void setMode(int mode) {
		mMode = mode;
	}

	public void setOperatorName(String operatorName) {
		mOperatorName = operatorName;
	}

	public void setParent(Fragment parent) {
		mParent = parent;
	}

	private void showDialogSearchOperator(int mode) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		SearchOperatorDialog searchOperatorDialog = new SearchOperatorDialog();
		searchOperatorDialog.setContainerId(mContainerId);
		searchOperatorDialog.setOperatorName(mOperatorName);
		searchOperatorDialog.setMode(mMode);
		searchOperatorDialog.setParent(mParent);
		searchOperatorDialog.show(fm, "search_operator_dialog");
	}

	private void startSearchOperator() {
		mContainerId = mContainerEditText.getText().toString();
		dismiss();
		showDialogSearchOperator(mMode);
	}
}
