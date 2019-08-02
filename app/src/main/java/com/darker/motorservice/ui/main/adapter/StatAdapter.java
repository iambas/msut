package com.darker.motorservice.ui.main.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.main.model.StatItem;

import java.util.ArrayList;

public class StatAdapter extends ArrayAdapter {

    private ArrayList<StatItem> items;
    private Context context;
    private int layoutId;

    public StatAdapter(Context context, int layoutId, ArrayList<StatItem> items) {
        super(context, layoutId);
        this.context = context;
        this.layoutId = layoutId;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(final int position, View v, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        v = inflater.inflate(layoutId, parent, false);
        StatItem statItem = (StatItem) items.get(position);

        TextView date = (TextView) v.findViewById(R.id.date);
        TextView numCall = (TextView) v.findViewById(R.id.num_call);
        TextView numChat = (TextView) v.findViewById(R.id.num_chat);

        date.setText(statItem.getDate());
        numCall.setText(statItem.getNumCall());
        numChat.setText(statItem.getNumChat());

        Log.d("LA date", statItem.getDate());

        return v;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
