package com.cloudjay.cjay.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.OperatorAdapter;
import com.cloudjay.cjay.event.OperatorCallbackEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

@EFragment(R.layout.dialog_select_operator)
public class SearchOperatorDialog extends DialogFragment {

	private String mOperatorName;
	private Fragment mParent;

	private RealmResults<Operator> operators;
	private OperatorAdapter operatorAdapter;

	@ViewById(R.id.et_operator_name)
	EditText etOperatorName;

	@ViewById(R.id.lv_operators_list)
	ListView lvOperators;

	@Bean
	DataCenter dataCenter;

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
		// retrieve list operators
		operators = event.getOperators();
		// Init and set adapter
		operatorAdapter = new OperatorAdapter(getActivity(), operators);
		lvOperators.setAdapter(operatorAdapter);
	}

	@AfterViews
	void doAfterViews() {
		// Set title for search operator dialog
		getDialog().setTitle(getResources().getString(R.string.dialog_operator_title));
		// Begin to get operators from cache
		dataCenter.getOperators();
	}

	@ItemClick(R.id.lv_operators_list)
	void listViewOperatorsItemClicked(Operator selectedOperator) {
		EventBus.getDefault().post(new OperatorCallbackEvent(selectedOperator));
		this.dismiss();
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			//operatorAdapter.updateData(operators);
		} else {
			ArrayList<Operator> searchFeeds = new ArrayList<Operator>();
			for (Operator operator : operators) {
				if (operator.getOperatorName().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))
						|| operator.getOperatorCode().toLowerCase(Locale.US).contains(searchText.toLowerCase(Locale.US))) {

					searchFeeds.add(operator);
				}
			}
			// refresh list
			//operatorAdapter.updateData(searchFeeds);
		}
	}

	public void setParent(Fragment parent) {
		mParent = parent;
	}
}
