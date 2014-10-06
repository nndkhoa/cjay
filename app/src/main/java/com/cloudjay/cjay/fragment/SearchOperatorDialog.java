package com.cloudjay.cjay.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class SearchOperatorDialog extends DialogFragment {
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

    EditText etOperatorName;
    ListView lvOperators;

    private void initContainerOperatorAdapter(ArrayList<Operator> operators) {
        BindDictionary<Operator> operatorsDict = new BindDictionary<Operator>();
        operatorsDict.addStringField(R.id.tv_operator_name, new StringExtractor<Operator>() {
            @Override
            public String getStringValue(Operator item, int position) {
                return Utils.replaceNullBySpace(item.getOperatorCode());
            }
        });
        operatorsDict.addStringField(R.id.tv_operator_code, new StringExtractor<Operator>() {
            @Override
            public String getStringValue(Operator item, int position) {
                return Utils.replaceNullBySpace(item.getOperatorCode());
            }
        });
        mOperatorsAdapter = new FunDapter<Operator>(getActivity(), operators, R.layout.list_item_operator,
                operatorsDict);
        lvOperators.setAdapter(mOperatorsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_operator, container);
        etOperatorName = (EditText) view.findViewById(R.id.et_operator_name);
        lvOperators = (ListView) view.findViewById(R.id.lv_operators_list);

        // if (mOperatorName != null) {
        // etOperatorName.setText(mOperatorName);
        // }

        etOperatorName.addTextChangedListener(new TextWatcher() {
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

        lvOperators.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                // Select operator

                mOperatorName = mOperatorsAdapter.getItem(position).getOperatorName();
                // etOperatorName.setText(mOperatorName); // Don't set text
                // because it will trigger searching
                mCallback = (SearchOperatorDialogListener) getActivity();
                mCallback.OnOperatorSelected(mParent, mContainerId, mOperatorName, mMode);
            }
        });

        //mOperators = (ArrayList<Operator>) DataCenter.getInstance().getListOperators(getActivity());
        initContainerOperatorAdapter(mOperators);
        getDialog().setTitle(getResources().getString(R.string.dialog_container_owner));

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
                if (operator.getOperatorName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))
                        || operator.getOperatorCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {

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
