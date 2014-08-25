package com.prostoradio.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by timonsk on 16.05.14.
 */
public class StreamStationSpinnerAdapter extends ArrayAdapter<StreamStation> {
    int mTextViewResourceId;
    Context mContext;

    public StreamStationSpinnerAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mTextViewResourceId = textViewResourceId;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)(super.getView(position, convertView, parent));
        assert tv != null;
        tv.setText(getItem(position).getStationLabel());
        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView)(super.getDropDownView(position, convertView, parent));
        assert tv != null;
        tv.setText(getItem(position).getStationLabel());
        return tv;
    }
}
