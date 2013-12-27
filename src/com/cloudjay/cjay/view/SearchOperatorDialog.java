package com.cloudjay.cjay.view;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	
	public void setContainerId(String containerId) {
		mContainerId = containerId;
	}
	
	public void setOperatorName(String operatorName) {
		mOperatorName = operatorName;
	}
	
	public void setMode(int mode) {
		mMode = mode;
	}
	
	public void setParent(Fragment parent) {
		mParent = parent;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.dialog_select_operator, container);
    	mOperatorEditText = (EditText)view.findViewById(R.id.dialog_operator_name);
    	mOperatorListView = (ListView)view.findViewById(R.id.dialog_operator_list);

		if (mOperatorName != null) {
			mOperatorEditText.setText(mOperatorName);
		}
		
		mOperatorListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				dismiss();
				// Select operator
				mOperatorName = mOperators.get(position).getName();
				mOperatorEditText.setText(mOperatorName);
				mCallback = (SearchOperatorDialogListener)getActivity();
				mCallback.OnOperatorSelected(mParent, mContainerId, mOperatorName, mMode);
			}
		});
		
		mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());
		initContainerOperatorAdapter(mOperators);
		
		return view;
	}

	private void initContainerOperatorAdapter(ArrayList<Operator> operators) {
		BindDictionary<Operator> operatorsDict = new BindDictionary<Operator>();
		operatorsDict.addStringField(R.id.operator_name,
				new StringExtractor<Operator>() {
					@Override
					public String getStringValue(Operator item, int position) {
						return item.getName();
					}
				});
		operatorsDict.addStringField(R.id.operator_code,
				new StringExtractor<Operator>() {
					@Override
					public String getStringValue(Operator item, int position) {
						return item.getCode();
					}
				});
		mOperatorsAdapter = new FunDapter<Operator>(getActivity(), operators,
				R.layout.list_item_operator, operatorsDict);
		mOperatorListView.setAdapter(mOperatorsAdapter);
	}
}
