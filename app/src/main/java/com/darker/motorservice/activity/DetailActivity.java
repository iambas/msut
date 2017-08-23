package com.darker.motorservice.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.assets.MyImage;
import com.darker.motorservice.data.Services;
import com.darker.motorservice.database.ServiceHandle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.darker.motorservice.data.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.data.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.KEY_CHAT;
import static com.darker.motorservice.data.Constant.LATLNG;
import static com.darker.motorservice.data.Constant.NAME;
import static com.darker.motorservice.data.Constant.PHOTO;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.TEL_NUM;
import static com.darker.motorservice.data.Constant.USER;

public class DetailActivity extends AppCompatActivity {

    private String id, name, telNum, photo;
    private Services services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        id = getIntent().getStringExtra(ID);
        ServiceHandle serviceHandle = new ServiceHandle(this);
        services = serviceHandle.getService(id);
        name = services.getName();
        telNum = services.getTel();
        photo = services.getPhoto();

        getSupportActionBar().setTitle(services.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setData();
        setFab();
        setImage();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setData() {
        ((TextView) findViewById(R.id.store_name)).setText(name);
        ((TextView) findViewById(R.id.store_pos)).setText(services.getPos());
        ((TextView) findViewById(R.id.work_time)).setText(services.getWorkTime());
        ((TextView) findViewById(R.id.service)).setText(services.getService());
        ((TextView) findViewById(R.id.distribute)).setText(services.getDistribute());

        final TextView status = (TextView) findViewById(R.id.status);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(services.getId());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean mStatus = (boolean) dataSnapshot.getValue();
                if (mStatus){
                    status.setText(R.string.open);
                    status.setBackgroundResource(R.color.openLight);
                }else{
                    status.setText(R.string.close);
                    status.setBackgroundResource(R.color.closeLight);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setFab() {
        FloatingActionButton callFab = (FloatingActionButton) findViewById(R.id.fab_call);
        FloatingActionButton mapFab = (FloatingActionButton) findViewById(R.id.fab_map);
        FloatingActionButton chatFab = (FloatingActionButton) findViewById(R.id.fab_chat);

        callFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });

        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, MapsActivity.class);
                intent.putExtra(NAME, name);
                intent.putExtra(LATLNG, services.getLatlng());
                startActivity(intent);
            }
        });

        chatFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
                intent.putExtra(KEY_CHAT, "");
                intent.putExtra(CHAT_WITH_ID, id);
                intent.putExtra(CHAT_WITH_NAME, name);
                intent.putExtra(STATUS, USER);
                intent.putExtra(TEL_NUM, telNum);
                intent.putExtra(PHOTO, photo);
                view.getContext().startActivity(intent);
            }
        });
    }

    private void setImage() {
        ImageView imageCover = (ImageView) findViewById(R.id.image);
        ImageView imageProfile = (ImageView) findViewById(R.id.profile_service);
        MyImage image = new MyImage();

        Bitmap cover, profile;
        try{
            cover = image.convertToBitmap(services.getImgCover());
        } catch (Exception e){
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        }
        try{
            profile = image.convertToBitmap(services.getImgProfile());
        }catch (Exception e){
            profile = BitmapFactory.decodeResource(getResources(), R.drawable.pro);
        }
        imageCover.setImageBitmap(cover);
        imageProfile.setImageBitmap(profile);
    }

    public void call() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View vb = inflater.inflate(R.layout.contact_service, null);
        builder.setView(vb);
        final AlertDialog d = builder.show();
        TextView txtName = (TextView) vb.findViewById(R.id.txt_name);
        txtName.setText("กดปุ่มด้านล่างเพื่อโทรหา " + name);

        FloatingActionButton fab = (FloatingActionButton) vb.findViewById(R.id.fab_call);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + telNum));

                if (ActivityCompat.checkSelfPermission(view.getContext(),
                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) view.getContext(),
                            new String[]{Manifest.permission.CALL_PHONE}, 10);
                }
                startActivity(intent);
                pushStat();
                d.dismiss();
            }
        });
    }

    private void pushStat(){
        DatabaseReference dbStat = FirebaseDatabase.getInstance().getReference().child("stat");
        Date date = new Date();
        String my = new SimpleDateFormat("yyyy-MM").format(date);
        String day = new SimpleDateFormat("dd-MM-yyyy").format(date);
        final DatabaseReference db = dbStat.child(id).child(my).child(day);
        db.child("call").child(id).setValue("1");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("chat")){
                    db.child("chat").setValue("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
