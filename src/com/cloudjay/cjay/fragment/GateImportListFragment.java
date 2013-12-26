package com.cloudjay.cjay.fragment;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.StaticImageLoader;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.googlecode.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_feeds)
public class GateImportListFragment extends SherlockDialogFragment implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {

	private final static String TAG = "FeedListFragment";

	private Button mAddNewBtn;

	private ListView mFeedListView;
	private ListView mOperatorListView;
	private EditText mOperatorEditText;
	
	private String containerName;
	private String operatorName;

	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;
	private ArrayList<Operator> mOperators;
	private FunDapter<Operator> mOperatorsAdapter;
	
	private Dialog mNewContainerDialog;
	private Dialog mSearchOperatorDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListContainerSessions(getActivity());
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity()); 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feeds, container, false);

		mAddNewBtn = (Button) view.findViewById(R.id.btn_add_new);
		mAddNewBtn.setOnClickListener(this);

		mFeedListView = (ListView) view.findViewById(R.id.feeds);
		mFeedListView.setOnItemClickListener(this);
		initContainerFeedAdapter(mFeeds);

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance().getListContainerSessions(getActivity());
		mFeedsAdapter.updateData(mFeeds);
	}

	@Override
	public void onClick(View view) {
		showDialogNewContainer();
	}
	
	private void showDialogNewContainer() {
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View newContainerView = factory.inflate(
				R.layout.dialog_new_container, null);
		
		final EditText newContainerIdEditText = (EditText) newContainerView
				.findViewById(R.id.dialog_new_container_id);
		
		if (containerName != null) {
			newContainerIdEditText.setText(containerName);
		}
		
		final Spinner newContainerOwnerSpinner = (Spinner) newContainerView
				.findViewById(R.id.dialog_new_container_owner);
		
		if (operatorName != null) {
			String ops[] = {operatorName};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item, ops);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			newContainerOwnerSpinner.setAdapter(adapter);
		} else {
			String ops[] = {DataCenter.getInstance().getListOperatorNames(getActivity()).get(0)};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item, ops);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			newContainerOwnerSpinner.setAdapter(adapter);
		}

		newContainerOwnerSpinner.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_UP) {
		        	containerName = newContainerIdEditText.getText().toString();
		        	mNewContainerDialog.dismiss();
		            showDialogSearchOperator();
		        }
		        return true;
			}
		});
		
		newContainerOwnerSpinner.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
		        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
		        	containerName = newContainerIdEditText.getText().toString();
		        	mNewContainerDialog.dismiss();
		            showDialogSearchOperator();
		            return true;
		        } else {
		            return false;
		        }
			}
		});

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity())
				.setTitle(getString(R.string.dialog_new_container))
				.setView(newContainerView)
				.setPositiveButton(R.string.dialog_container_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								
								containerName = null;

								// Get the container id and container operator
								// code
								String containerId = newContainerIdEditText
										.getText().toString();
								String operatorCode = newContainerOwnerSpinner
										.getSelectedItem().toString();

								// Create a tmp Container Session
								TmpContainerSession newTmpContainer = new TmpContainerSession();
								newTmpContainer.setContainerId(containerId);
								newTmpContainer.setOperatorCode(operatorCode);
								newTmpContainer.setCheckInTime(StringHelper
										.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT));

								User currentUser = Session.restore(
										getActivity()).getCurrentUser();

								newTmpContainer.setDepotCode(currentUser
										.getDepot().getDepotCode());
								newTmpContainer.printMe();

								// Save the current temp Container Session
								DataCenter.getInstance().setTmpCurrentSession(
										newTmpContainer);

								// Pass tmpContainerSession away
								// Then start showing the Camera
								Intent intent = new Intent(getActivity(),
										CameraActivity_.class);
								intent.putExtra(
										CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
										newTmpContainer);
								intent.putExtra("type", 0); // in
								startActivity(intent);
							}
						})
				.setNegativeButton(R.string.dialog_container_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								containerName = null;
							}
						});
		mNewContainerDialog = dialogBuilder.create();
		mNewContainerDialog.show();
	}
	
	private void showDialogSearchOperator() {
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View searchOperatorView = factory.inflate(
				R.layout.dialog_select_operator, null);
		
		mOperatorEditText = (EditText) searchOperatorView.findViewById(R.id.dialog_operator_name);
		mOperatorListView = (ListView) searchOperatorView.findViewById(R.id.dialog_operator_list);
		mOperatorListView.setOnItemClickListener(this);
		initContainerOperatorAdapter(mOperators);
		
		if (operatorName != null) {
			mOperatorEditText.setText(operatorName);
		}
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity())
				.setTitle(getString(R.string.dialog_container_owner))
				.setView(searchOperatorView)
				.setPositiveButton(R.string.dialog_container_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								operatorName = mOperatorEditText.getText().toString();
								showDialogNewContainer();
							}
						})
				.setNegativeButton(R.string.dialog_container_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialogNewContainer();
							}
						});
		mSearchOperatorDialog = dialogBuilder.create();
		mSearchOperatorDialog.show();
	}
	
	private void initContainerOperatorAdapter(ArrayList<Operator> operators) {
		BindDictionary<Operator> operatorsDict = new BindDictionary<Operator>();
		operatorsDict.addStringField(R.id.operator_name,
				new StringExtractor<Operator>() {

					@Override
					public String getStringValue(Operator item,
							int position) {
						return item.getName();
					}
				});

		operatorsDict.addStringField(R.id.operator_code,
				new StringExtractor<Operator>() {

					@Override
					public String getStringValue(Operator item,
							int position) {
						// TODO Auto-generated method stub
						return item.getCode();
					}
				});
		
		mOperatorsAdapter = new FunDapter<Operator>(
				getActivity(), operators, R.layout.list_item_operator,
				operatorsDict);

		mOperatorListView.setAdapter(mOperatorsAdapter);
	}

	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {

		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {

					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {

					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						// TODO Auto-generated method stub
						return item.getOperatorName();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						// TODO Auto-generated method stub
						return item.getCheckInTime();
					}
				});

		feedsDict.addStringField(R.id.feed_item_container_export_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						// TODO Auto-generated method stub
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

		mFeedsAdapter = new FunDapter<ContainerSession>(
				getActivity(), containers, R.layout.list_item_container,
				feedsDict);

		mFeedListView.setAdapter(mFeedsAdapter);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (view == mFeedListView) {
			android.util.Log.d(TAG, "Show item at position: " + position);
		} else {
			operatorName = mOperators.get(position).getName();
			mOperatorEditText.setText(operatorName);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		// TODO: display menu item

		return false;
	}
}
