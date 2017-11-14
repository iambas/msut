package com.darker.motorservice.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.darker.motorservice.R;
import com.darker.motorservice.model.Services;
import com.darker.motorservice.database.AdminDatabase;
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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static com.darker.motorservice.Constant.ADMIN;
import static com.darker.motorservice.Constant.SERVICE;

public class LoadServiceUtils {

    private Context context;
    private ServiceDatabase handle;
    private StorageReference storageRef;

    public LoadServiceUtils(){}

    public LoadServiceUtils(Context context){
        this.context = context;
        storageRef = FirebaseStorage.getInstance().getReference();
        handle = new ServiceDatabase(context);
    }

    public void loadAdmin() {
        final AdminDatabase admin = new AdminDatabase(context);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(ADMIN);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ServiceDatabase handle = new ServiceDatabase(context);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getValue().toString();
                    if (!admin.isAdmin(id))
                        admin.addAdmin(id);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void loadData() {
        Log.d("load data", "OK");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(SERVICE);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ServiceDatabase handle = new ServiceDatabase(context);
                int count = handle.getServiceCount();
                if (count > dataSnapshot.getChildrenCount()){
                    handle.delTable();
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        Services services = ds.getValue(Services.class);
                        Bitmap cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.cover);
                        Bitmap pro = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
                        services.setImgCover(toByte(cover));
                        services.setImgProfile(toByte(pro));
                        handle.addService(services);

                        Log.d("load cover", services.getCover());
                        Log.d("load profile", services.getPhoto());
                        loadImg(services, services.getCover(), true);
                        loadImg(services, services.getPhoto(), false);
                    }
                    return;
                }

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Services services = ds.getValue(Services.class);
                    if (!handle.hasService(services.getId())) {
                        Log.d("hasService", "NO");
                        Bitmap cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.cover);
                        Bitmap pro = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
                        services.setImgCover(toByte(cover));
                        services.setImgProfile(toByte(pro));
                        handle.addService(services);
                    }

                    Log.d("load cover", services.getCover());
                    Log.d("load profile", services.getPhoto());
                    loadImg(services, services.getCover(), true);
                    loadImg(services, services.getPhoto(), false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void loadImg(final Services services, String image, final boolean isCover) {
        Log.d("image", image);
        StorageReference islandRef = storageRef.child(image);

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (isCover && (!hasImg(services.getId(), bytes, isCover))){
                        services.setImgCover(bytes);
                }else if(!isCover && (!hasImg(services.getId(), bytes, !isCover))) {
                    services.setImgProfile(bytes);
                }
                handle.updateService(services);
                Log.d("load image", "OK");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }

    private boolean hasImg(String id, byte[] bytes, boolean isCover){
        ServiceDatabase handle = new ServiceDatabase(context);
        Services services = handle.getService(id);
        byte[] bs = isCover ? services.getImgCover() : services.getImgProfile();
        return Arrays.equals(bs, bytes);
    }

    private byte[] toByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
