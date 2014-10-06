package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudjay.cjay.model.Operator;

import java.util.List;

/**
 * Created by nambv on 06/10/2014.
 */
public class OperatorAdapter extends ArrayAdapter<Operator> {

	private Context context;
	private List<Operator> values;

	public OperatorAdapter(Context context, int resource, List<Operator> values) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
	}

	public int getCount() {
		return values.size();
	}

	public Operator getItem(int position) {
		return values.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TextView label = new TextView(context);
		label.setTextColor(Color.BLACK);
		label.setText(values.get(position).getOperatorName());
		return label;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView label = new TextView(context);
		label.setTextColor(Color.BLACK);
		label.setText(values.get(position).getOperatorName());
		return label;
	}
}
