package com.darker.motorservice.ui.main.fragment;

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
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.main.adapter.StatAdapter;
import com.darker.motorservice.ui.main.model.StatItem;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.utils.StringUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsFragment extends Fragment{
    private Context context;
    private DatabaseReference dbStat;
    private ArrayList<StatItem> statItemList;
    private ArrayAdapter adapter;
    private String statMonth;
    private View mView;
    private ProgressBar progressBar;
    private TextView tvTextNull;

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

        initGlobal(view);
        bindAdapter(view);
        checkAdmin(view);
    }

    public void initGlobal(View view) {
        this.context = view.getContext();
        this.mView = view;
        statMonth = StringUtils.getDateFormate("yyyy-MM");
    }

    public void checkAdmin(View view) {
        String uid = FirebaseUtil.getUid();
        AdminDatabase admin = new AdminDatabase(view.getContext());
        if (admin.isAdmin(uid)){
            setupSpinner(view);
        }else{
            getMonth(uid);
        }
    }

    public void setupSpinner(View view) {
        view.findViewById(R.id.for_admin).setVisibility(View.VISIBLE);
        List<ServicesItem> servicesItems = new ServiceDatabase(context).getAllSerivce();
        List<String> nameList = new ArrayList<String>();
        final List<String> idList = new ArrayList<String>();
        for (ServicesItem item : servicesItems){
            nameList.add(item.getName());
            idList.add(item.getId());
        }
        monthSpinner(view, nameList, idList);
    }

    public void monthSpinner(View view, List<String> nameList, final List<String> idList) {
        Spinner areaSpinner = (Spinner) view.findViewById(R.id.sel_service);
        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nameList);
        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areasAdapter);

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String sid = idList.get(position);
                Log.d("ST stmonth", statMonth);
                getMonth(sid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void bindAdapter(View view) {
        statItemList = new ArrayList<StatItem>();
        ListView listView = (ListView) view.findViewById(R.id.list);
        adapter = new StatAdapter(context, R.layout.fragment_statistics_item, statItemList);
        listView.setAdapter(adapter);
    }

    private void getMonth(final String uid){
        TextView txtNet = (TextView) mView.findViewById(R.id.txt_net_alert);
        tvTextNull = (TextView) mView.findViewById(R.id.txt_null);
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
        tvTextNull.setVisibility(View.GONE);

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
                    tvTextNull.setVisibility(View.VISIBLE);
                }else{
                    tvTextNull.setVisibility(View.GONE);
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
                statItemList.clear();
                for (DataSnapshot dataDay : dataMonth.getChildren()){
                    String date = dataDay.getKey();
                    String numCall = String.valueOf(dataDay.child("dialogCall").getChildrenCount());
                    String numChat = String.valueOf(dataDay.child("chat").getChildrenCount());
                    StatItem statItem = new StatItem(date, numCall, numChat);
                    statItemList.add(statItem);
                    Collections.sort(statItemList, new Comparator<StatItem>(){
                        @Override
                        public int compare(StatItem s1, StatItem s2) {
                            return s1.toString().compareToIgnoreCase(s2.toString());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }

                adapter.notifyDataSetChanged();
                if (statItemList.size() == 0){
                    tvTextNull.setVisibility(View.VISIBLE);
                }else{
                    tvTextNull.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
