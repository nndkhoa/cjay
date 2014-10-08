package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Operator;

import java.util.List;

public class OperatorAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Operator> values;

	private class ViewHolder {
		TextView tvOperatorName;
		TextView tvOperatorCode;
	}

	public OperatorAdapter(Context context, List<Operator> values) {
		this.inflater = LayoutInflater.from(context);
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

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_operator, null);
			holder.tvOperatorName = (TextView) convertView.findViewById(R.id.tv_operator_name);
			holder.tvOperatorCode = (TextView) convertView.findViewById(R.id.tv_operator_code);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvOperatorName.setText(values.get(position).getOperatorName());
		holder.tvOperatorCode.setText(values.get(position).getOperatorCode());
		return convertView;
	}

    public void swapOperators(List<Operator> values) {
        this.values = values;
        notifyDataSetChanged();
    }
}
