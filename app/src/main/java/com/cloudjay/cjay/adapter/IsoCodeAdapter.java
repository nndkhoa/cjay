package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.IsoCode;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vule on 2/11/2014.
 */
public class IsoCodeAdapter extends ArrayAdapter<IsoCode> implements Filterable {
    private LayoutInflater mInflater;
    private int layoutResId;
    private Context context;
    private List<IsoCode> mData;
    private Filter mFilter;

    public IsoCodeAdapter(Context context, int resource, List<IsoCode> data) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;

        // Initialize list
        mData = data;
        setData(mData);

        // Create custom filter for iso codes
        mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //constraint is the result from text you want to filter against.
                FilterResults filterResults = new FilterResults();
                String prefix = constraint.toString().toLowerCase();

                if (TextUtils.isEmpty(prefix)) {

                    filterResults.values = mData;
                    filterResults.count = mData.size();

                } else {

                    ArrayList<IsoCode> results = new ArrayList<IsoCode>();

                    for (int i = 0; i < mData.size(); i++)
                    {
                        final IsoCode isoCode = mData.get(i);
                        final String codeName = isoCode.getFullName().toLowerCase();

                        if (codeName.contains(prefix))
                        {
                            results.add(isoCode);
                        }
                    }

                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                setData((ArrayList<IsoCode>) results.values);

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private static class ViewHolder {
        TextView tvName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final IsoCode isoCode = getItem(position);

        // Apply ViewHolder pattern for better performance
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Set data to view
        viewHolder.tvName.setText(isoCode.getFullName());

        return convertView;
    }

    public void setData(List<IsoCode> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
