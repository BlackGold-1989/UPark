package com.mario.upark.common;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.mario.upark.R;
import com.mario.upark.RecordActivity;

public class HistoryAdapter extends BaseAdapter {

    private RecordActivity mRecordActivity;
    private List<History> mArrayList;

    public HistoryAdapter(RecordActivity activity, List<History> arrayList){
        mRecordActivity = activity;
        mArrayList = arrayList;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return mArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mRecordActivity).inflate(R.layout.item_history, null);
        TextView txt_code = view.findViewById(R.id.txt_history_code);
        TextView txt_regdate = view.findViewById(R.id.txt_history_regdate);
//        TextView txt_city = view.findViewById(R.id.txt_history_city);
//        TextView txt_other = view.findViewById(R.id.txt_history_other);

        txt_code.setText(mArrayList.get(i).code);
        txt_regdate.setText(mArrayList.get(i).regdate);
//        txt_city.setText(mArrayList.get(i).city);
//        txt_other.setText(mArrayList.get(i).other);

        return view;
    }
}
