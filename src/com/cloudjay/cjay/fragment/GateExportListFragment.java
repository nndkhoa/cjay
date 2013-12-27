package com.cloudjay.cjay.fragment;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.ami.fundapter.interfaces.ItemClickListener;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.listener.OnContainerAddRequestListener;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Mapper;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_gate_export)
@OptionsMenu(R.menu.menu_gate_export)
public class GateExportListFragment extends SherlockDialogFragment {
	private OnContainerAddRequestListener mCallback;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;
	
	private ContainerSession mSelectedContainerSession;

	@ViewById(R.id.container_list) ListView mFeedListView;
	@ViewById(R.id.search_edittext)	EditText mSearchEditText;
	@ViewById(R.id.add_button) Button mAddButton;
	@ViewById(R.id.notfound_textview) TextView mNotfoundTextView;

	@AfterViews
	void afterViews() {
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance().getListContainerSessions(getActivity());
		configureControls(mFeeds);
		initFunDapter(mFeeds);
	}
	
	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		// TODO
	}
	
	@Click(R.id.add_button)
	void addButtonClicked() {
		mCallback.OnContainerAddRequested();
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);
		
		// clear current selection
		mSelectedContainerSession = null;
		getActivity().invalidateOptionsMenu();
		
		// get the selected container session
		ContainerSession containerSession = mFeedsAdapter.getItem(position);
		TmpContainerSession tmpContainerSession = Mapper.toTmpContainerSession(
				containerSession, getActivity());

		// Pass tmpContainerSession away
		// Then start showing the Camera
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				tmpContainerSession);
		intent.putExtra("type", 1); // out
		startActivity(intent);
	}
	
	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);
		
		// refresh menu
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		// Set Soft Input mode so it's always visible
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		return dialog;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnContainerAddRequestListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnContainerAddRequestListener");
        }
    }
    
	@Override
	public void onResume() {
		super.onResume();
		// Hector: will update UI here
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			configureControls(mFeeds);
			mFeedsAdapter.updateData(mFeeds);
		} else {
			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
			for (ContainerSession containerSession : mFeeds) {
				if (containerSession.getContainerId().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {
					searchFeeds.add(containerSession);
				}
			}
			// refresh list
			configureControls(searchFeeds);
			mFeedsAdapter.updateData(searchFeeds);
		}
	}
	
	private void configureControls(ArrayList<ContainerSession> list) {
		boolean hasContainers = list.size() > 0;
		if (hasContainers) {
			mFeedListView.setVisibility(View.VISIBLE);
			mAddButton.setVisibility(View.INVISIBLE);
			mNotfoundTextView.setVisibility(View.INVISIBLE);
		} else {
			mFeedListView.setVisibility(View.INVISIBLE);
			mAddButton.setVisibility(View.VISIBLE);
			mNotfoundTextView.setVisibility(View.VISIBLE);			
		}
	}

	private void initFunDapter(ArrayList<ContainerSession> containers) {
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
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getFullContainerId();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String stringColor, ImageView view) {
						view.setImageResource(R.drawable.ic_logo);
					}
				}).onClick(new ItemClickListener<ContainerSession>() {
			@Override
			public void onClick(ContainerSession item, int position, View view) {
				// TODO Auto-generated method stub
			}
		});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(), containers,
				R.layout.list_item_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
