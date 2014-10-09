package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.cloudjay.cjay.model.Session;

/**
 * Created by thai on 09/10/2014.
 */
public class UploadSessionAdapter extends ArrayAdapter<Session> {

    private LayoutInflater mInflater;
    private int layoutResId;

    public UploadSessionAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
