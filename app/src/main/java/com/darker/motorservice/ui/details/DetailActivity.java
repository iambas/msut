package com.darker.motorservice.ui.details;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.chat.ChatActivity;
import com.darker.motorservice.ui.map.MapsActivity;
import com.darker.motorservice.utility.CallPhoneUtil;
import com.darker.motorservice.utility.DateUtil;
import com.darker.motorservice.utility.ImageUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.darker.motorservice.utility.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.utility.Constant.LATLNG;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.PHOTO;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.TEL_NUM;
import static com.darker.motorservice.utility.Constant.USER;

public class DetailActivity extends AppCompatActivity {

    private String id;
    private String contactName;
    private String phoneNumber;
    private String photo;
    private ServicesItem servicesItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initToolbar();

        id = getIntent().getStringExtra(ID);
        ServiceDatabase serviceDatabase = new ServiceDatabase(this);
        servicesItem = serviceDatabase.getService(id);
        contactName = servicesItem.getName();
        phoneNumber = servicesItem.getTel();
        photo = servicesItem.getPhoto();

        setTitleActionBar();
        setData();
        setFab();
        setImage();
    }

    private void setTitleActionBar() {
        getSupportActionBar().setTitle(servicesItem.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setData() {
        ((TextView) findViewById(R.id.store_name)).setText(contactName);
        ((TextView) findViewById(R.id.store_pos)).setText(servicesItem.getPos());
        ((TextView) findViewById(R.id.work_time)).setText(servicesItem.getWorkTime());
        ((TextView) findViewById(R.id.service)).setText(servicesItem.getService());
        ((TextView) findViewById(R.id.distribute)).setText(servicesItem.getDistribute());

        final TextView status = (TextView) findViewById(R.id.status);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(servicesItem.getId());
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
                CallPhoneUtil.callPhoneDialog(DetailActivity.this, contactName, phoneNumber);
            }
        });

        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, MapsActivity.class);
                intent.putExtra(NAME, contactName);
                intent.putExtra(LATLNG, servicesItem.getLatlng());
                startActivity(intent);
            }
        });

        chatFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
                intent.putExtra(KEY_CHAT, "");
                intent.putExtra(CHAT_WITH_ID, id);
                intent.putExtra(CHAT_WITH_NAME, contactName);
                intent.putExtra(STATUS, USER);
                intent.putExtra(TEL_NUM, phoneNumber);
                intent.putExtra(PHOTO, photo);
                view.getContext().startActivity(intent);
            }
        });
    }

    private void setImage() {
        ImageView imageCover = (ImageView) findViewById(R.id.image);
        ImageView imageProfile = (ImageView) findViewById(R.id.profile_service);

        Bitmap cover, profile;
        try{
            cover = ImageUtil.convertByteToBitmap(servicesItem.getImgCover());
        } catch (Exception e){
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        }
        try{
            profile = ImageUtil.convertByteToBitmap(servicesItem.getImgProfile());
        }catch (Exception e){
            profile = BitmapFactory.decodeResource(getResources(), R.drawable.pro);
        }
        imageCover.setImageBitmap(cover);
        imageProfile.setImageBitmap(profile);
    }

    private void pushStat(){
        DatabaseReference dbStat = FirebaseUtil.getChildData("stat");
        String yearAndMonth = DateUtil.getDateFormat("yyyy-MM");
        String dateFormat = DateUtil.getDateFormat("dd-MM-yyyy");

        final DatabaseReference dbStatsSetValue = dbStat.child(id).child(yearAndMonth).child(dateFormat);
        dbStatsSetValue.child("call").child(id).setValue("1");
        dbStatsSetValue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("chat")){
                    dbStatsSetValue.child("chat").setValue("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
