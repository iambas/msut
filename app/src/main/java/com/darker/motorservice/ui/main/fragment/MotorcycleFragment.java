package com.darker.motorservice.ui.main.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darker.motorservice.R;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.main.adapter.MotorcycleAdapter;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.darker.motorservice.utils.Constant.ALERT;
import static com.darker.motorservice.utils.Constant.CHAT;
import static com.darker.motorservice.utils.Constant.STATUS;

public class MotorcycleFragment extends Fragment {

    private List<ServicesItem> servicesItemList;
    private MotorcycleAdapter motorcycleAdapter;

    public MotorcycleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycle_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        SharedPreferences.Editor edChat = context.getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.commit();

        ServiceDatabase handle = new ServiceDatabase(getContext());
        servicesItemList = new ArrayList<>();
        try{
            servicesItemList = handle.getAllSerivce();
        }catch (Exception e){
            Log.e("servicesItemList Except", e.getMessage());
        }

        motorcycleAdapter = new MotorcycleAdapter(context, servicesItemList);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(motorcycleAdapter);
        update();
    }

    private void update() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(STATUS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if ((boolean) ds.getValue()) {
                        String key = ds.getKey();
                        for (ServicesItem s : servicesItemList){
                            if (s.getId().equals(key)){
                                servicesItemList.remove(s);
                                servicesItemList.add(0,s);
                                motorcycleAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
