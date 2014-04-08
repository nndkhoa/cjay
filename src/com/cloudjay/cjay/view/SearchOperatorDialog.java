package com.cloudjay.cjay.view;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Utils;

public class SearchOperatorDialog extends SherlockDialogFragment {

	public interface SearchOperatorDialogListener {
		public void OnOperatorSelected(Fragment parent, String containerId, String operatorName, int mode);
	}

	private SearchOperatorDialogListener mCallback;

	private String mOperatorName;
	private String mContainerId;
	private int mMode;
	private Fragment mParent;

	private ArrayList<Operator> mOperators;
	private FunDapter<Operator> mOperatorsAdapter;

	EditText mOperatorEditText;
	ListView mOperatorListView;

	private void initContainerOperatorAdapter(ArrayList<Operator> operators) {
		BindDictionary<Operator> operatorsDict = new BindDictionary<Operator>();
		operatorsDict.addStringField(R.id.operator_name, new StringExtractor<Operator>() {
			@Override
			public String getStringValue(Operator item, int position) {
				return Utils.replaceNullBySpace(item.getName());
			}
		});
		operatorsDict.addStringField(R.id.operator_code, new StringExtractor<Operator>() {
			@Override
			public String getStringValue(Operator item, int position) {
				return Utils.replaceNullBySpace(item.getCode());
			}
		});
		mOperatorsAdapter = new FunDapter<Operator>(getActivity(), operators, R.layout.list_item_operator,
													operatorsDict);
		mOperatorListView.setAdapter(mOperatorsAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_select_operator, container);
		mOperatorEditText = (EditText) view.findViewById(R.id.dialog_operator_name);
		mOperatorListView = (ListView) view.findViewById(R.id.dialog_operator_list);

		// if (mOperatorName != null) {
		// mOperatorEditText.setText(mOperatorName);
		// }

		mOperatorEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		mOperatorListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismiss();
				// Select operator

				mOperatorName = mOperatorsAdapter.getItem(position).getName();
				// mOperatorEditText.setText(mOperatorName); // Don't set text
				// because it will trigger searching
				mCallback = (SearchOperatorDialogListener) getActivity();
				mCallback.OnOperatorSelected(mParent, mContainerId, mOperatorName, mMode);
			}
		});

		mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());
		initContainerOperatorAdapter(mOperators);
		getDialog().setTitle(getResources().getString(R.string.dialog_operator_title));

		// show keyboard
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		return view;
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			mOperatorsAdapter.updateData(mOperators);
		} else {
			ArrayList<Operator> searchFeeds = new ArrayList<Operator>();
			for (Operator operator : mOperators) {
				if (operator.getName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))
						|| operator.getCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {

					searchFeeds.add(operator);
				}
			}
			// refresh list
			mOperatorsAdapter.updateData(searchFeeds);
		}
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
}
