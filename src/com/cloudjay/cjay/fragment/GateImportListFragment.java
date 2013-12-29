package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.StaticImageLoader;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionAddedEvent;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_import)
@OptionsMenu(R.menu.menu_gate_import)
public class GateImportListFragment extends SherlockDialogFragment {

	private final static String LOG_TAG = "GateImportListFragment";

	@ViewById(R.id.btn_add_new)
	Button mAddNewBtn;
	@ViewById(R.id.feeds)
	ListView mFeedListView;

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private boolean mDirty;

	private ContainerSession mSelectedContainerSession;

	@OptionsItem(R.id.menu_camera)
	void cameraMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu camera item clicked");

		String uuid = mSelectedContainerSession.getUuid();
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA, uuid);
		intent.putExtra("type", 0); // in
		startActivity(intent);
	}

	@OptionsItem(R.id.menu_edit_container)
	void editMenuItemSelected() {
		Logger.Log(LOG_TAG, "Menu edit item clicked");

		// Open dialog for editing details
		showContainerDetailDialog(mSelectedContainerSession.getContainerId(),
				mSelectedContainerSession.getOperatorName(),
				AddContainerDialog.CONTAINER_DIALOG_EDIT);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		try {

			Logger.Log(LOG_TAG, "Menu upload item clicked");

			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager()
					.getHelper(getActivity()).getContainerSessionDaoImpl();

			// User confirm upload
			mSelectedContainerSession.setUploadConfirmation(true);

			// this will call EventBus.post(`UploadsModifiedEvent`)
			mSelectedContainerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

			containerSessionDaoImpl.update(mSelectedContainerSession);

			// It will trigger `UploadsFragment` Adapter notifyDataSetChanged
			EventBus.getDefault().post(
					new ContainerSessionAddedEvent(mSelectedContainerSession));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@AfterViews
	void afterViews() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());

		initContainerFeedAdapter(mFeeds);

		mSelectedContainerSession = null;
		mDirty = false;
	}

	@Click(R.id.btn_add_new)
	void addContainerClicked() {
		showContainerDetailDialog(
				getResources().getString(R.string.default_container_id), "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.feeds)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();

		android.util.Log.d(LOG_TAG, "Show item at position: " + position);
	}

	@ItemLongClick(R.id.feeds)
	void listItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_camera).setVisible(isDisplayed);
		menu.findItem(R.id.menu_edit_container).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mDirty) {
			mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
					.getListContainerSessions(getActivity());
			mFeedsAdapter.updateData(mFeeds);
		}
	}

	public void showContainerDetailDialog(String containerId,
			String operatorName, int mode) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog();
		addContainerDialog.setContainerId(containerId);
		addContainerDialog.setOperatorName(operatorName);
		addContainerDialog.setMode(mode);
		addContainerDialog.setParent(this);
		addContainerDialog.show(fm, "add_container_dialog");
	}

	public void OnOperatorSelected(String containerId, String operatorName,
			int mode) {
		showContainerDetailDialog(containerId, operatorName, mode);
	}

	public void OnContainerInputCompleted(String containerId,
			String operatorName, int mode) {
		// Get the container id and container operator code
		String operatorCode = "";
		int operatorId = 0;
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				operatorId = operator.getId();
				break;
			}
		}

		switch (mode) {
		case AddContainerDialog.CONTAINER_DIALOG_ADD:

			User currentUser = Session.restore(getActivity()).getCurrentUser();

			// Create Container Session object
			ContainerSession containerSession = new ContainerSession(
					getActivity(),
					containerId,
					operatorCode,
					StringHelper
							.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT),
					currentUser.getDepot().getDepotCode());

			containerSession.setUploadConfirmation(false);
			containerSession.setOnLocal(true);
			containerSession.setUploadState(ContainerSession.STATE_NONE);

			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
						.getInstance().getDatabaseManager()
						.getHelper(getActivity()).getContainerSessionDaoImpl();

				containerSessionDaoImpl.addContainerSessions(containerSession);

				Intent intent = new Intent(getActivity(), CameraActivity_.class);
				intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
						containerSession.getUuid());
				intent.putExtra("type", 0); // in
				startActivity(intent);

			} catch (SQLException e) {
				e.printStackTrace();
			}

			break;

		case AddContainerDialog.CONTAINER_DIALOG_EDIT:
			Container container = mSelectedContainerSession.getContainer();
			Operator operator = container.getOperator();
			container.setContainerId(containerId);
			operator.setCode(operatorCode);
			operator.setName(operatorName);
			operator.setId(operatorId);
			break;
		}

		mDirty = true;
	}

	// private void showContainerDialog(int mode) {
	// LayoutInflater factory = LayoutInflater.from(getActivity());
	// final View newContainerView = factory.inflate(
	// R.layout.dialog_new_container, null);
	// final EditText newContainerIdEditText = (EditText) newContainerView
	// .findViewById(R.id.dialog_new_container_id);
	// final EditText newContainerOwnerEditText = (EditText) newContainerView
	// .findViewById(R.id.dialog_new_container_owner);
	// final int dialogMode = mode;
	//
	// if (mContainerName == null) {
	// mContainerName = getResources().getString(
	// R.string.default_container_id);
	// }
	// newContainerIdEditText.setText(mContainerName);
	//
	// if (mOperatorName != null) {
	// newContainerOwnerEditText.setText(mOperatorName);
	// }
	//
	// newContainerOwnerEditText
	// .setOnFocusChangeListener(new OnFocusChangeListener() {
	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (hasFocus) {
	// hideKeyboard();
	// mContainerName = newContainerIdEditText.getText()
	// .toString();
	// mNewContainerDialog.dismiss();
	// showDialogSearchOperator(dialogMode);
	// }
	// }
	// });
	//
	// newContainerOwnerEditText.setOnTouchListener(new OnTouchListener() {
	// @Override
	// public boolean onTouch(View view, MotionEvent event) {
	// if (event.getAction() == MotionEvent.ACTION_UP) {
	// hideKeyboard();
	// mContainerName = newContainerIdEditText.getText()
	// .toString();
	// mNewContainerDialog.dismiss();
	// showDialogSearchOperator(dialogMode);
	// }
	// return true;
	// }
	// });
	//
	// newContainerOwnerEditText.setOnKeyListener(new OnKeyListener() {
	// @Override
	// public boolean onKey(View view, int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	// hideKeyboard();
	// mContainerName = newContainerIdEditText.getText()
	// .toString();
	// mNewContainerDialog.dismiss();
	// showDialogSearchOperator(dialogMode);
	// return true;
	// } else {
	// return false;
	// }
	// }
	// });
	//
	// AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
	// getActivity())
	// .setTitle(getString(R.string.dialog_new_container))
	// .setView(newContainerView)
	// .setPositiveButton(R.string.dialog_container_ok,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	//
	// hideKeyboard();
	// mContainerName = null;
	//
	// // Get the container id and container operator
	// // code
	// String containerId = newContainerIdEditText
	// .getText().toString();
	// String operatorName = newContainerOwnerEditText
	// .getText().toString();
	// String operatorCode = "";
	// int operatorId = 0;
	// for (Operator operator : mOperators) {
	// if (operator.getName().equals(operatorName)) {
	// operatorCode = operator.getCode();
	// operatorId = operator.getId();
	// break;
	// }
	// }
	//
	// switch (dialogMode) {
	// case CONTAINER_DIALOG_ADD:
	// // Create a tmp Container Session
	// TmpContainerSession newTmpContainer = new TmpContainerSession();
	// newTmpContainer.setContainerId(containerId);
	// newTmpContainer
	// .setOperatorCode(operatorCode);
	// newTmpContainer.setCheckInTime(StringHelper
	// .getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT));
	//
	// User currentUser = Session.restore(
	// getActivity()).getCurrentUser();
	//
	// newTmpContainer.setDepotCode(currentUser
	// .getDepot().getDepotCode());
	// newTmpContainer.printMe();
	//
	// // Save the current temp Container Session
	// DataCenter.getInstance()
	// .setTmpCurrentSession(
	// newTmpContainer);
	//
	// // Pass tmpContainerSession away
	// // Then start showing the Camera
	// Intent intent = new Intent(getActivity(),
	// CameraActivity_.class);
	// intent.putExtra(
	// CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
	// newTmpContainer);
	// intent.putExtra("type", 0); // in
	// startActivity(intent);
	// break;
	//
	// case CONTAINER_DIALOG_EDIT:
	// Container container = mSelectedContainerSession
	// .getContainer();
	// Operator operator = container.getOperator();
	// container.setContainerId(containerId);
	// operator.setCode(operatorCode);
	// operator.setName(operatorName);
	// operator.setId(operatorId);
	// break;
	// }
	// }
	// })
	// .setNegativeButton(R.string.dialog_container_cancel,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// hideKeyboard();
	// mContainerName = null;
	// }
	// });
	// mNewContainerDialog = dialogBuilder.create();
	// mNewContainerDialog.show();
	//
	// // Show keyboard
	// newContainerIdEditText.requestFocus();
	// showKeyboard();
	// }
	//
	// private void showDialogSearchOperator(int mode) {
	// LayoutInflater factory = LayoutInflater.from(getActivity());
	// final View searchOperatorView = factory.inflate(
	// R.layout.dialog_select_operator, null);
	// final int dialogMode = mode;
	//
	// mOperatorEditText = (EditText) searchOperatorView
	// .findViewById(R.id.dialog_operator_name);
	// mOperatorListView = (ListView) searchOperatorView
	// .findViewById(R.id.dialog_operator_list);
	// mOperatorListView.setOnItemClickListener(new OnItemClickListener() {
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// // Select operator
	// mOperatorName = mOperators.get(position).getName();
	// mOperatorEditText.setText(mOperatorName);
	//
	// // Go back
	// mSearchOperatorDialog.dismiss();
	// showContainerDialog(dialogMode);
	// }
	// });
	// initContainerOperatorAdapter(mOperators);
	//
	// if (mOperatorName != null) {
	// mOperatorEditText.setText(mOperatorName);
	// }
	//
	// AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
	// getActivity())
	// .setTitle(getString(R.string.dialog_container_owner))
	// .setView(searchOperatorView)
	// .setPositiveButton(R.string.dialog_container_ok,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// mOperatorName = mOperatorEditText.getText()
	// .toString();
	// showContainerDialog(dialogMode);
	// }
	// })
	// .setNegativeButton(R.string.dialog_container_cancel,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// showContainerDialog(dialogMode);
	// }
	// });
	// mSearchOperatorDialog = dialogBuilder.create();
	// mSearchOperatorDialog.show();
	// }
	//
	// private void initContainerOperatorAdapter(ArrayList<Operator> operators)
	// {
	// BindDictionary<Operator> operatorsDict = new BindDictionary<Operator>();
	// operatorsDict.addStringField(R.id.operator_name,
	// new StringExtractor<Operator>() {
	//
	// @Override
	// public String getStringValue(Operator item, int position) {
	// return item.getName();
	// }
	// });
	//
	// operatorsDict.addStringField(R.id.operator_code,
	// new StringExtractor<Operator>() {
	//
	// @Override
	// public String getStringValue(Operator item, int position) {
	// // TODO Auto-generated method stub
	// return item.getCode();
	// }
	// });
	//
	// mOperatorsAdapter = new FunDapter<Operator>(getActivity(), operators,
	// R.layout.list_item_operator, operatorsDict);
	//
	// mOperatorListView.setAdapter(mOperatorsAdapter);
	// }
	//
	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getFullContainerId();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getOperatorName();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckInTime();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_export_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckOutTime();
					}
				});
		feedsDict.addStaticImageField(R.id.feed_item_picture,
				new StaticImageLoader<ContainerSession>() {
					@Override
					public void loadImage(ContainerSession item,
							ImageView imageView, int position) {
						// TODO Auto-generated method stub
					}
				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
	//
	// private void hideKeyboard() {
	// mImm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	// }
	//
	// private void showKeyboard() {
	// mImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	// }
}
