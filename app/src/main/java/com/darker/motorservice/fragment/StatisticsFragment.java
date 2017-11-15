package com.darker.motorservice.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.adapter.StatAdapter;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.model.Services;
import com.darker.motorservice.model.Stat;
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StatisticsFragment extends Fragment{

    private Context context;
    private DatabaseReference dbStat;
    private ArrayList<Stat> statList;
    private ArrayAdapter adapter;
    private String statMonth;
    private View mView;
    private ProgressBar progressBar;
    private TextView txtNull;

    public StatisticsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.context = view.getContext();
        this.mView = view;
        statMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        statList = new ArrayList<Stat>();
        ListView listView = (ListView) view.findViewById(R.id.list);
        adapter = new StatAdapter(context, R.layout.fragment_statistics_item, statList);
        listView.setAdapter(adapter);

        AdminDatabase admin = new AdminDatabase(view.getContext());
        if (admin.isAdmin(uid)){
            view.findViewById(R.id.for_admin).setVisibility(View.VISIBLE);
            List<Services> list = new ServiceDatabase(context).getAllSerivce();
            List<String> sName = new ArrayList<String>();
            final List<String> sId = new ArrayList<String>();
            for (Services s : list){
                sName.add(s.getName());
                sId.add(s.getId());
            }

            Spinner areaSpinner = (Spinner) view.findViewById(R.id.sel_service);
            ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sName);
            areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            areaSpinner.setAdapter(areasAdapter);

            areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                    String sid = sId.get(position);
                    Log.d("ST stmonth", statMonth);
                    getMonth(sid);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }else{
            getMonth(uid);
        }
    }

    private void getMonth(final String uid){
        TextView txtNet = (TextView) mView.findViewById(R.id.txt_net_alert);
        txtNull = (TextView) mView.findViewById(R.id.txt_null);
        progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
        if (NetWorkUtils.disable(getContext())){
            txtNet.setVisibility(View.VISIBLE);
            txtNet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMonth(uid);
                }
            });
            return;
        }else{
            txtNet.setClickable(false);
            txtNet.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.VISIBLE);
        txtNull.setVisibility(View.GONE);

        dbStat = FirebaseDatabase.getInstance().getReference().child("stat").child(uid);
        dbStat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> areas = new ArrayList<String>();
                final List<String> spin = new ArrayList<String>();
                String[] month = getResources().getStringArray(R.array.month);

                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren()) {
                    spin.add(areaSnapshot.getKey());
                }
                Collections.sort(spin, new Comparator<String>(){
                    @Override
                    public int compare(String s1, String s2) {
                        return s2.compareToIgnoreCase(s1);
                    }
                });

                for (String s : spin){
                    String[] r = s.split("-");
                    int m = Integer.parseInt(r[1]) - 1;
                    areas.add(month[m] + " " + r[0]);
                }

                if (areas.size() == 0){
                    progressBar.setVisibility(View.GONE);
                    txtNull.setVisibility(View.VISIBLE);
                }else{
                    txtNull.setVisibility(View.GONE);
                }

                Spinner areaSpinner = (Spinner) mView.findViewById(R.id.spinner);
                ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, areas);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                areaSpinner.setAdapter(areasAdapter);

                areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        statMonth = spin.get(position);
                        Log.d("ST stmonth", statMonth);
                        readData();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void readData(){
        dbStat.child(statMonth).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataMonth) {
                statList.clear();
                for (DataSnapshot dataDay : dataMonth.getChildren()){
                    String date = dataDay.getKey();
                    String numCall = String.valueOf(dataDay.child("dialogCall").getChildrenCount());
                    String numChat = String.valueOf(dataDay.child("chat").getChildrenCount());
                    Stat stat = new Stat(date, numCall, numChat);
                    statList.add(stat);
                    Collections.sort(statList, new Comparator<Stat>(){
                        @Override
                        public int compare(Stat s1, Stat s2) {
                            return s1.toString().compareToIgnoreCase(s2.toString());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }

                adapter.notifyDataSetChanged();
                if (statList.size() == 0){
                    txtNull.setVisibility(View.VISIBLE);
                }else{
                    txtNull.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
