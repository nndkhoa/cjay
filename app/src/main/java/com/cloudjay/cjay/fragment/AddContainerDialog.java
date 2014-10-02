package com.cloudjay.cjay.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudjay.cjay.R;

import butterknife.InjectView;

/**
 * Created by Thai on 10/1/2014.
 */
public class AddContainerDialog extends android.support.v4.app.DialogFragment {

	Context context;

	@InjectView(R.id.tv_containterIDSearch)
	TextView tvContainerSearch;
	@InjectView(R.id.tv_searchresult)
	TextView tvSearchResult;
	@InjectView(R.id.btn_addContainer)
	Button btnAddContainer;
	@InjectView(R.id.btn_cancelAddContainer)
	Button btnCancelAddContainer;

	public AddContainerDialog(Context context) {
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_addcontainer, container, false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		btnAddContainer = (Button) rootView.findViewById(R.id.btn_addContainer);
		btnAddContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Click add Container", Toast.LENGTH_SHORT).show();
			}
		});
		btnCancelAddContainer = (Button) rootView.findViewById(R.id.btn_cancelAddContainer);
		btnCancelAddContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		return rootView;
	}

}
