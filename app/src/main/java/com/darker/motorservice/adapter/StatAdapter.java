package com.darker.motorservice.adapter;

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
import com.darker.motorservice.data.Stat;

import java.util.ArrayList;

public class StatAdapter extends ArrayAdapter {

    private ArrayList<Stat> items;
    private Context context;
    private int layoutId;

    public StatAdapter(Context context, int layoutId, ArrayList<Stat> items) {
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
        Stat stat = (Stat) items.get(position);

        TextView date = (TextView) v.findViewById(R.id.date);
        TextView numCall = (TextView) v.findViewById(R.id.num_call);
        TextView numChat = (TextView) v.findViewById(R.id.num_chat);

        date.setText(stat.getDate());
        numCall.setText(stat.getNumCall());
        numChat.setText(stat.getNumChat());

        Log.d("LA date", stat.getDate());

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
