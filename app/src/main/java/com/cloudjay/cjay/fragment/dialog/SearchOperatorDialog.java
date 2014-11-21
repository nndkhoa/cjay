package com.cloudjay.cjay.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.OperatorAdapter;
import com.cloudjay.cjay.event.operator.OperatorChosenEvent;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.dialog_select_operator)
public class SearchOperatorDialog extends DialogFragment {

	List<Operator> operators;
	OperatorAdapter operatorAdapter;

	@ViewById(R.id.et_operator_name)
	EditText etOperatorName;

	@ViewById(R.id.lv_operators_list)
	ListView lvOperators;

	@Bean
	DataCenter dataCenter;

    @SystemService
    InputMethodManager inputMethodManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@UiThread
	public void onEvent(OperatorsGotEvent event) {
		// Retrieve list operators
		operators = event.getOperators();
		// Init and set adapter
		if (null == operatorAdapter) {
			operatorAdapter = new OperatorAdapter(getActivity(), operators);
			lvOperators.setAdapter(operatorAdapter);
		}

		// Notify change
		operatorAdapter.swapOperators(operators);

	}

	@AfterViews
	void doAfterViews() {
		// Set title for search operator dialog
		getDialog().setTitle(getResources().getString(R.string.dialog_operator_title));

//		// Begin to get operators from cache
		dataCenter.searchOperator("");

        // show keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

	@ItemClick(R.id.lv_operators_list)
	void listViewOperatorsItemClicked(Operator selectedOperator) {
		EventBus.getDefault().post(new OperatorChosenEvent(selectedOperator));
		this.dismiss();
	}

	@AfterTextChange(R.id.et_operator_name)
	void search(Editable text) {
		String keyword = text.toString();
		dataCenter.searchOperator(keyword);
	}

	@TextChange(R.id.et_operator_name)
	void upperCase(){
		etOperatorName.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}

	public void setParent(Fragment parent) {
	}
}
