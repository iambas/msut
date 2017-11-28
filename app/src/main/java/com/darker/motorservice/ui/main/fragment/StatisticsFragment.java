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
import com.darker.motorservice.ui.main.fragment.spinner.SpinnerUtil;
import com.darker.motorservice.ui.main.model.StatItem;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.utility.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.darker.motorservice.utility.Constant.CHAT;
import static com.darker.motorservice.utility.Constant.STAT;

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
        bindView();
        checkAdmin(view);
    }

    public void initGlobal(View view) {
        this.context = view.getContext();
        this.mView = view;
        statMonth = StringUtil.getDateFormate("yyyy-MM");
    }

    public void checkAdmin(View view) {
        String uid = FirebaseUtil.getUid();
        AdminDatabase admin = new AdminDatabase(view.getContext());
        if (admin.isAdmin(uid)){
            setupSpinner(view);
        }else{
            checkNetwork(uid);
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
        storeSpinner(view, nameList, idList);
    }

    public void storeSpinner(View view, List<String> nameList, final List<String> idList) {
        Spinner areaSpinner = SpinnerUtil.getSpinner(view, R.id.sel_service, nameList);
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String sid = idList.get(position);
                Log.d("ST stmonth", statMonth);
                checkNetwork(sid);
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

    private void checkNetwork(final String uid){
        if (isNetworkDisable(uid)) return;
        progressBar.setVisibility(View.VISIBLE);
        hideTextNotList();
        queryStatsByUid(uid);
    }

    private void bindView() {
        tvTextNull = (TextView) mView.findViewById(R.id.txt_null);
        progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
    }

    private void queryStatsByUid(String uid) {
        dbStat = FirebaseUtil.getChild(STAT).child(uid);
        dbStat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> statKeys = new ArrayList<String>();
                for (DataSnapshot areaSnapshot: dataSnapshot.getChildren()) {
                    statKeys.add(areaSnapshot.getKey());
                }
                Collections.sort(statKeys, new Comparator<String>(){
                    @Override
                    public int compare(String s1, String s2) {
                        return s2.compareToIgnoreCase(s1);
                    }
                });

                List<String> monthList = getMonthList(statKeys);
                checkMonthsSize(monthList);
                monthSpinner(monthList, statKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void monthSpinner(List<String> monthList, final List<String> statKeys) {
        Spinner spinner = SpinnerUtil.getSpinner(mView, R.id.spinner, monthList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statMonth = statKeys.get(position);
                Log.d("ST stmonth", statMonth);
                readData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public List<String> getMonthList(List<String> list) {
        List<String> monthList = new ArrayList<String>();
        String[] months = getResources().getStringArray(R.array.month);
        for (String date : list){
            String[] dateSplit = date.split("-");
            int indexMonth = Integer.parseInt(dateSplit[1]) - 1;
            monthList.add(months[indexMonth] + " " + dateSplit[0]);
        }
        return monthList;
    }

    public void checkMonthsSize(List<String> areas) {
        if (areas.size() == 0){
            hideProgressBar();
            showTextNoList();
        }else{
            hideTextNotList();
        }
    }

    public void hideTextNotList() {
        tvTextNull.setVisibility(View.GONE);
    }

    private boolean isNetworkDisable(final String uid) {
        TextView tvNetworkAlert = (TextView) mView.findViewById(R.id.txt_net_alert);
        if (NetworkUtil.disable(getContext())){
            tvNetworkAlert.setVisibility(View.VISIBLE);
            tvNetworkAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkNetwork(uid);
                }
            });
            return true;
        }
            tvNetworkAlert.setClickable(false);
            tvNetworkAlert.setVisibility(View.GONE);

        return false;
    }

    private void readData(){
        dbStat.child(statMonth).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataMonth) {
                statItemList.clear();
                for (DataSnapshot dataDay : dataMonth.getChildren()){
                    String date = dataDay.getKey();
                    String numCall = String.valueOf(dataDay.child("dialogCall").getChildrenCount());
                    String numChat = String.valueOf(dataDay.child(CHAT).getChildrenCount());
                    StatItem statItem = new StatItem(date, numCall, numChat);
                    statItemList.add(statItem);
                    sortStatItemList();
                    adapter.notifyDataSetChanged();
                }

                checkStatItemListSize();
                hideProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void checkStatItemListSize() {
        if (statItemList.size() == 0){
            showTextNoList();
        }else{
            hideTextNotList();
        }
    }

    public void showTextNoList() {
        tvTextNull.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void sortStatItemList() {
        Collections.sort(statItemList, new Comparator<StatItem>(){
            @Override
            public int compare(StatItem s1, StatItem s2) {
                return s1.toString().compareToIgnoreCase(s2.toString());
            }
        });
    }
}
