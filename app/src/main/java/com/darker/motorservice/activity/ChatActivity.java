package com.darker.motorservice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.adapter.MessageAdapter;
import com.darker.motorservice.assets.MyImage;
import com.darker.motorservice.assets.NetWork;
import com.darker.motorservice.data.ChatMessage;
import com.darker.motorservice.data.NewChat;
import com.darker.motorservice.data.Pictures;
import com.darker.motorservice.database.PictureHandle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.darker.motorservice.data.Constant.ALERT;
import static com.darker.motorservice.data.Constant.CHAT;
import static com.darker.motorservice.data.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.data.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.data.Constant.DATA;
import static com.darker.motorservice.data.Constant.IMG;
import static com.darker.motorservice.data.Constant.KEY_CHAT;
import static com.darker.motorservice.data.Constant.KEY_IMAGE;
import static com.darker.motorservice.data.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.data.Constant.NAME;
import static com.darker.motorservice.data.Constant.PHOTO;
import static com.darker.motorservice.data.Constant.SERVICE;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.TEL_NUM;
import static com.darker.motorservice.data.Constant.USER;

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private int PLACE_PICKER_REQUEST = 1;
    private int IMAGE_REQUEST = 2;
    private boolean GpsStatus;
    private DatabaseReference mDb;
    private MessageAdapter adapter;
    private List<ChatMessage> chatList;
    private String keyChat;
    private String chatWithName;
    private String telNum;
    private String status;
    private String uid;
    private String service, user, myName;
    private EditText editText;
    private boolean found = false;
    private ProgressBar progressBar;
    private TextView netAlert;
    private SharedPreferences.Editor edChat, edLogin;
    private StorageReference sRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        sRef = FirebaseStorage.getInstance().getReference();
        Intent intent = getIntent();
        keyChat = intent.getStringExtra(KEY_CHAT);
        String chatWithId = intent.getStringExtra(CHAT_WITH_ID);
        chatWithName = intent.getStringExtra(CHAT_WITH_NAME);
        telNum = intent.getStringExtra(TEL_NUM);
        status = intent.getStringExtra(STATUS);
        String photo = intent.getStringExtra(PHOTO);

        Log.d("chat", "." + keyChat);
        Log.d("chat", "." + chatWithId);
        Log.d("chat", "." + chatWithName);
        Log.d("chat", "." + telNum);
        Log.d("chat", "." + status);
        Log.d("chat", "." + photo);

        getSupportActionBar().setTitle(chatWithName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences shLogin = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        edLogin = shLogin.edit();
        edLogin.putString(IMG, photo);
        edLogin.putString(CHAT_WITH_ID, chatWithId);
        edLogin.commit();
        myName = shLogin.getString(NAME, "");
        if (!keyChat.isEmpty()) {
            edLogin.putString(KEY_CHAT, keyChat);
            edLogin.commit();
        }

        edChat = getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.commit();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDb = FirebaseDatabase.getInstance().getReference().child(CHAT);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        ImageButton btnImg = (ImageButton) findViewById(R.id.btn_img);
        ImageButton btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnImg.setVisibility(View.VISIBLE);
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnImageClicked();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClicked();
            }
        });

        if (status.equals(USER)) {
            service = chatWithId;
            user = uid;
        } else {
            service = uid;
            user = chatWithId;
        }

        chatList = new ArrayList<ChatMessage>();
        adapter = new MessageAdapter(this,R.layout.message_item, chatList);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        editText = (EditText) findViewById(R.id.input);
        netAlert = (TextView) findViewById(R.id.txt_net_alert);
        refresh();
        netAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
    }

    public void onSendClicked() {
        String msg = editText.getText().toString();
        if (msg.equals(""))
            return;

        if (!(new NetWork(this).isNetworkAvailiable())) {
            Toast.makeText(this, "ข้อผิดพลาดเครือข่าย! ไม่สามารถส่งข้อความได้", Toast.LENGTH_LONG).show();
            return;
        }

        if (keyChat.isEmpty()) find();
        pushMsg(msg);
        if (status.equals(USER)) pushStat(CHAT);
    }

    public void onBtnImageClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_REQUEST);
    }

    private void dialogImg(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        final View vb = inflater.inflate(R.layout.image, null);
        builder.setView(vb);

        ImageView imageView = (ImageView) vb.findViewById(R.id.img);
        imageView.setImageBitmap(bitmap);

        builder.setTitle("ส่งรูปภาพ");
        builder.setPositiveButton("ส่ง", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadImg(bitmap);
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void uploadImg(final Bitmap bitmap) {
        if (!(new NetWork(this).isNetworkAvailiable())) {
            Toast.makeText(this, "ข้อผิดพลาดเครือข่าย! ไม่สามารถส่งรูปภาพได้", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String date = new SimpleDateFormat("-yyyy_MM_dd_HH_mm_ss").format(new Date());
        final String imgName = "image/" + uid.substring(0, 5) + date + ".png";
        StorageReference mountainsRef = FirebaseStorage.getInstance().getReference().child(imgName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        Toast.makeText(this, "กำลังส่งรูปภาพ กรุณารอสักครู่...",Toast.LENGTH_SHORT).show();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("upload", exception.getMessage());
                Toast.makeText(ChatActivity.this, "การส่งรูปภาพมีปัญหา โปรดลองอีกครั้ง",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("upload", "OK");
                pushMsg(KEY_IMAGE + imgName);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "ส่งรูปภาพเรียบร้อย",Toast.LENGTH_SHORT).show();
                pushStat("chat");

                PictureHandle handle = new PictureHandle(ChatActivity.this);
                byte[] bytes = new MyImage().toByte(bitmap);
                handle.addPicture(new Pictures(imgName, bytes));
            }
        });
    }

    private void pushMsg(String msg) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        mDb.child(keyChat).child(DATA).push()
                .setValue(new ChatMessage(time, myName, msg, status, ""));
        editText.setText("");
    }

    private void pushStat(final String type) {
        DatabaseReference dbStat = FirebaseDatabase.getInstance().getReference().child("stat");
        Date date = new Date();
        String my = new SimpleDateFormat("yyyy-MM").format(date);
        String day = new SimpleDateFormat("dd-MM-yyyy").format(date);
        final DatabaseReference db = dbStat.child(service).child(my).child(day);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("call")) {
                    db.child("call").setValue("1");
                }

                if (!dataSnapshot.hasChild("chat")) {
                    db.child("chat").setValue("1");
                }
                db.child(type).child(uid).setValue("1");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (!(new NetWork(this).isNetworkAvailiable())) {
            netAlert.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            editText.setFocusableInTouchMode(true);
            netAlert.setVisibility(View.GONE);
            find();
        }
    }

    private void find() {
        found = false;
        mDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals("data")) {
                        mDb.child(data.getKey()).removeValue();
                        continue;
                    }
                    try {
                        if (data.child(SERVICE).getValue().toString().equals(service) &&
                                data.child(USER).getValue().toString().equals(user)) {
                            keyChat = data.getKey();
                            found = true;
                            edLogin.putString(KEY_CHAT, keyChat);
                            edLogin.commit();
                            break;
                        }
                    } catch (Exception e) {
                        Log.d("ex", e.getMessage());
                    }
                }
                if (!found) mDb.push().setValue(new NewChat(service, user));
                query();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void query() {
        if (keyChat.isEmpty()) {
            find();
            progressBar.setVisibility(View.GONE);
            return;
        }

        final int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mDb.child(keyChat).child(DATA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = data.getValue(ChatMessage.class);
                    int t = Integer.parseInt(chatMessage.getDate().split("-")[1]);
                    if ((t > 10 && t - 10 >= m) || t + 2 >= m) {
                        if (chatMessage.getMessage().contains(KEY_IMAGE)){
                            sRef.child(chatMessage.getMessage().replace(KEY_IMAGE, "")).delete();
                        }
                        mDb.child(keyChat).child(DATA).child(data.getKey()).removeValue();
                        continue;
                    }
                    loadImg(chatMessage);
                }

                Collections.sort(chatList, new Comparator<ChatMessage>() {
                    @Override
                    public int compare(ChatMessage o1, ChatMessage o2) {
                        return o1.toString().compareToIgnoreCase(o2.toString());
                    }
                });
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadImg(final ChatMessage cm) {
        String path = "";
        if (cm.getMessage().contains(KEY_IMAGE)){
            path = cm.getMessage().replace(KEY_IMAGE, "");
        }else {
            chatList.add(cm);
            adapter.notifyDataSetChanged();
            return;
        }

        final PictureHandle handle = new PictureHandle(this);
        if (handle.hasPicture(path)){
            Log.d("hasPicture", "YES");
            byte[] bytes = handle.getPicture(path).getPicture();
            Bitmap bitmap = new MyImage().convertToBitmap(bytes);
            cm.setBitmap(bitmap);
            chatList.add(cm);
            adapter.notifyDataSetChanged();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_edit_white);
        cm.setBitmap(bitmap);
        chatList.add(cm);
        adapter.notifyDataSetChanged();

        final String imgName = path;
        StorageReference islandRef = FirebaseStorage.getInstance().getReference().child(path);
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = new MyImage().convertToBitmap(bytes);
                handle.addPicture(new Pictures(imgName, bytes));
                try {
                    int index = chatList.indexOf(cm);
                    ChatMessage c = chatList.get(index);
                    c.setBitmap(bitmap);
                    chatList.remove(index);
                    chatList.add(c);
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    Log.d("Excep chatact", e.getMessage());
                }
                Log.d("load Picture", "OK");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (status.equals(USER)) {
            if (!(new NetWork(this).isNetworkAvailiable()))
                getMenuInflater().inflate(R.menu.menu_chat_no_net, menu);
            else
                getMenuInflater().inflate(R.menu.menu_chat_net, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_tel:
                call();
                return true;
            case R.id.menu_gps:
                myGps();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void call() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View vb = inflater.inflate(R.layout.contact_service, null);
        builder.setView(vb);
        final AlertDialog d = builder.show();
        TextView txtName = (TextView) vb.findViewById(R.id.txt_name);
        txtName.setText("กดปุ่มด้านล่างเพื่อโทรหา " + chatWithName);

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
                pushStat("call");
                d.dismiss();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println(connectionResult.getErrorMessage());
        Log.d("onConnectionFailed", connectionResult.getErrorMessage());
    }

    public void myGps() {
        if (!(new NetWork(this).isNetworkAvailiable())) return;
        checkGpsStatus();
        if (!GpsStatus) return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            Log.d("place", "ok");
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Log.e("place", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                pushMsg(place.getLatLng().toString());
                pushStat("chat");
            }
        } else {
            Log.d("requestCode", "." + requestCode);
        }

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    if (bitmap.getWidth() > 720)
                        bitmap = new MyImage().scaleBitmap(bitmap, 720);
                    else
                        bitmap = new MyImage().scaleBitmap(bitmap, bitmap.getWidth());
                    dialogImg(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!GpsStatus) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setMessage("ในการดำเนินการต่อ ให้อุปกรณ์เปิดตำแหน่ง (GPS)");
            builder.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("ยกเลิก", null);
            builder.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        back();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

        edChat.putBoolean(ALERT, true);
        edChat.commit();
        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {
            startActivity(new Intent(this, MainActivity.class));
            Log.i("Current", "This is last activity in the stack");
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        edChat.putBoolean(ALERT, false);
        edChat.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        edChat.putBoolean(ALERT, false);
        edChat.commit();
        Log.d("Services check", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
    }
}