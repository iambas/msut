package com.darker.motorservice.ui.main.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.post.PostActivity;
import com.darker.motorservice.ui.main.adapter.TimelineAdapter;
import com.darker.motorservice.utils.ImageUtils;
import com.darker.motorservice.ui.main.model.PictureItem;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.model.TimelineItem;
import com.darker.motorservice.database.PictureDatabse;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.darker.motorservice.utils.Constant.ALERT;
import static com.darker.motorservice.utils.Constant.CHAT;
import static com.darker.motorservice.utils.Constant.DATE;
import static com.darker.motorservice.utils.Constant.ID;
import static com.darker.motorservice.utils.Constant.IMG;
import static com.darker.motorservice.utils.Constant.KEY;
import static com.darker.motorservice.utils.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utils.Constant.MESSAGE;
import static com.darker.motorservice.utils.Constant.SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.TIMELINE;
import static com.darker.motorservice.utils.Constant.USER;

public class TimelineFragment extends Fragment {

    private List<TimelineItem> timelineItems;
    private TimelineAdapter adapter;
    private String id;
    private DatabaseReference dbRef;
    private Bundle bundle;
    private Context context;
    private StorageReference sRef;

    public TimelineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bundle = savedInstanceState;
        context = view.getContext();
        sRef = FirebaseStorage.getInstance().getReference();
        SharedPreferences.Editor edChat = context.getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.commit();

        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        id = sh.getString(ID, "");
        String status = sh.getString(STATUS, USER);
        dbRef = FirebaseDatabase.getInstance().getReference();

        timelineItems = new ArrayList<>();
        adapter = new TimelineAdapter(context, timelineItems);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra(KEY, "");
                intent.putExtra(ID, "");
                intent.putExtra(MESSAGE, "");
                intent.putExtra(IMG, "");
                intent.putExtra(DATE, "");
                startActivity(intent);
            }
        });

        if (status.equals(SERVICE)) {
            updateService();
        } else {
            view.findViewById(R.id.fab_new_post).setVisibility(View.GONE);
            updateUser();
        }
    }

    private void updateUser() {
        final int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
        dbRef.child(TIMELINE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timelineItems.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    TimelineItem timelineItem = ds.getValue(TimelineItem.class);
                    int t = Integer.parseInt(timelineItem.getDate().split("/")[1]);
                    if (t > 10){
                        if (t - 10 == m) {
                            if (!timelineItem.getImgName().isEmpty()){
                                sRef.child(timelineItem.getImgName()).delete();
                            }
                            dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                            continue;
                        }
                    } else if (t + 2 <= m) {
                        if (!timelineItem.getImgName().isEmpty()){
                            sRef.child(timelineItem.getImgName()).delete();
                        }
                        dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                        continue;
                    }
                    timelineItem.setKey(ds.getKey());
                    ServiceDatabase handle = new ServiceDatabase(getContext());
                    ServicesItem servicesItem = handle.getService(timelineItem.getId());
                    loadImg(timelineItem, servicesItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateService() {
        final int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
        dbRef.child(TIMELINE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timelineItems.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    TimelineItem timelineItem = ds.getValue(TimelineItem.class);
                    int t = Integer.parseInt(timelineItem.getDate().split("/")[1]);
                    if (t > 10){
                        if (t - 10 == m) {
                            if (!timelineItem.getImgName().isEmpty()){
                                sRef.child(timelineItem.getImgName()).delete();
                            }
                            dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                            continue;
                        }
                    } else if (t + 2 <= m) {
                        if (!timelineItem.getImgName().isEmpty()){
                            sRef.child(timelineItem.getImgName()).delete();
                        }
                        dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                        continue;
                    }
                    timelineItem.setKey(ds.getKey());
                    if (timelineItem.getId().equals(id)) {
                        ServiceDatabase handle = new ServiceDatabase(getContext());
                        ServicesItem servicesItem = handle.getService(id);
                        loadImg(timelineItem, servicesItem);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void loadImg(final TimelineItem timelineItem, ServicesItem servicesItem) {
        ImageUtils myImage = new ImageUtils();
        Bitmap bitmap = myImage.convertToBitmap(servicesItem.getImgProfile());
        timelineItem.setName(servicesItem.getName());
        timelineItem.setProfile(bitmap);

        if (timelineItem.getImgName().isEmpty()) {
            timelineItems.add(timelineItem);
            sort();
            return;
        }

        final PictureDatabse handle = new PictureDatabse(context);
        if (handle.hasPicture(timelineItem.getImgName())) {
            Log.d("hasPicture", "YES");
            byte[] bytes = handle.getPicture(timelineItem.getImgName()).getPicture();
            Bitmap b = new ImageUtils().convertToBitmap(bytes);
            timelineItem.setImage(b);
            timelineItems.add(timelineItem);
            sort();
            return;
        }

        final long ONE_MEGABYTE = 1024 * 1024;
        sRef.child(timelineItem.getImgName()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                handle.addPicture(new PictureItem(timelineItem.getImgName(), bytes));
                Bitmap bitmap = new ImageUtils().convertToBitmap(bytes);
                timelineItem.setImage(bitmap);
                timelineItems.add(timelineItem);
                sort();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }

    private void sort() {
        Collections.sort(timelineItems, new Comparator<TimelineItem>() {
            @Override
            public int compare(TimelineItem o1, TimelineItem o2) {
                return o2.toString().compareToIgnoreCase(o1.toString());
            }
        });
        adapter.notifyDataSetChanged();
    }
}
