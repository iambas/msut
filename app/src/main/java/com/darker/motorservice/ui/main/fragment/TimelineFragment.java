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
import com.darker.motorservice.database.PictureDatabse;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.model.TimelineItem;
import com.darker.motorservice.ui.main.adapter.TimelineAdapter;
import com.darker.motorservice.ui.main.model.PictureItem;
import com.darker.motorservice.ui.post.PostActivity;
import com.darker.motorservice.utils.ImageUtils;
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
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.TIMELINE;
import static com.darker.motorservice.utils.Constant.USER;

public class TimelineFragment extends Fragment implements View.OnClickListener{

    private List<TimelineItem> timelineItems;
    private TimelineAdapter timelineAdapter;
    private String id, status;
    private DatabaseReference dbRef;
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

        bindContextAndFirebase(view);
        chatSharedPreference();
        sharedPreferences();
        setRecycleView(view);
        unVisibleForUser(view);
        queryTimeline();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_new_post:
                startPsotActivity();
                break;
            default: break;
        }
    }

    private void sharedPreferences() {
        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        id = sh.getString(ID, "");
        status = sh.getString(STATUS, USER);
    }

    private void bindContextAndFirebase(View view) {
        context = view.getContext();
        dbRef = FirebaseDatabase.getInstance().getReference();
        sRef = FirebaseStorage.getInstance().getReference();
    }

    private void chatSharedPreference() {
        SharedPreferences.Editor edChat = context.getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.apply();
    }

    private void setRecycleView(View view) {
        timelineItems = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(context, timelineItems);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(timelineAdapter);
    }

    private void startPsotActivity() {
        Intent intent = new Intent(context, PostActivity.class);
        intent.putExtra(KEY, "");
        intent.putExtra(ID, "");
        intent.putExtra(MESSAGE, "");
        intent.putExtra(IMG, "");
        intent.putExtra(DATE, "");
        startActivity(intent);
    }

    private void unVisibleForUser(View view) {
        if (status.equals(USER))
            view.findViewById(R.id.fab_new_post).setVisibility(View.GONE);
    }

    private void queryTimeline() {
        dbRef.child(TIMELINE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timelineItems.clear();
                checkTimelineToRemove(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void checkTimelineToRemove(DataSnapshot dataSnapshot) {
        int nowMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            TimelineItem timelineItem = ds.getValue(TimelineItem.class);
            int timelineMonth = Integer.parseInt(timelineItem.getDate().split("/")[1]);
            if (timelineMonth > 10) {
                if (timelineMonth - 10 == nowMonth) {
                    if (!timelineItem.getImgName().isEmpty()) {
                        sRef.child(timelineItem.getImgName()).delete();
                    }
                    dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                    continue;
                }
            } else if (timelineMonth + 2 <= nowMonth) {
                if (!timelineItem.getImgName().isEmpty()) {
                    sRef.child(timelineItem.getImgName()).delete();
                }
                dbRef.child(TIMELINE).child(ds.getKey()).removeValue();
                continue;
            }
            timelineItem.setKey(ds.getKey());
            loadImageByStatus(timelineItem);
        }
    }

    private void loadImageByStatus(TimelineItem timelineItem) {
        if (status.equals(USER)){
            ServiceDatabase handle = new ServiceDatabase(getContext());
            ServicesItem servicesItem = handle.getService(timelineItem.getId());
            setUpLoadImage(timelineItem, servicesItem);
        }else{
            if (timelineItem.getId().equals(id)) {
                ServiceDatabase handle = new ServiceDatabase(getContext());
                ServicesItem servicesItem = handle.getService(id);
                setUpLoadImage(timelineItem, servicesItem);
            }
        }
    }

    private void setUpLoadImage(final TimelineItem timelineItem, ServicesItem servicesItem) {
        Bitmap bitmap = ImageUtils.convertToBitmap(servicesItem.getImgProfile());
        timelineItem.setName(servicesItem.getName());
        timelineItem.setProfile(bitmap);

        if (timelineItem.getImgName().isEmpty()) {
            timelineItems.add(timelineItem);
            sortTimelineItems();
            return;
        }

        PictureDatabse handle = new PictureDatabse(context);
        String imagePath = timelineItem.getImgName();
        if (checkHasImage(timelineItem, handle, imagePath)) return;
        loadImageFromStorage(timelineItem, handle, imagePath);
    }

    private boolean checkHasImage(TimelineItem timelineItem, PictureDatabse handle, String imagePath) {
        if (handle.hasPicture(imagePath)) {
            Log.d("hasPicture", "YES");
            addtimelineToList(timelineItem, handle, imagePath);
            sortTimelineItems();
            return true;
        }
        return false;
    }

    private void addtimelineToList(TimelineItem timelineItem, PictureDatabse handle, String imagePath) {
        byte[] bytes = handle.getPicture(imagePath).getPicture();
        Bitmap bitmap = ImageUtils.convertToBitmap(bytes);
        timelineItem.setImage(bitmap);
        timelineItems.add(timelineItem);
    }

    private void loadImageFromStorage(final TimelineItem timelineItem, final PictureDatabse handle, final String imagePath) {
        final long ONE_MEGABYTE = 1024 * 1024;
        sRef.child(imagePath).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                handle.addPicture(new PictureItem(imagePath, bytes));
                Bitmap bitmap = ImageUtils.convertToBitmap(bytes);
                timelineItem.setImage(bitmap);
                timelineItems.add(timelineItem);
                sortTimelineItems();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }


    private void sortTimelineItems() {
        Collections.sort(timelineItems, new Comparator<TimelineItem>() {
            @Override
            public int compare(TimelineItem o1, TimelineItem o2) {
                return o2.toString().compareToIgnoreCase(o1.toString());
            }
        });
        timelineAdapter.notifyDataSetChanged();
    }
}
