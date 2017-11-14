package com.darker.motorservice.activity;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.darker.motorservice.database.PictureDatabse;
import com.darker.motorservice.model.ChatMessage;
import com.darker.motorservice.model.NewChat;
import com.darker.motorservice.model.Pictures;
import com.darker.motorservice.utils.ImageUtils;
import com.darker.motorservice.utils.NetWorkUtils;
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

import static com.darker.motorservice.Constant.ALERT;
import static com.darker.motorservice.Constant.CHAT;
import static com.darker.motorservice.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.Constant.DATA;
import static com.darker.motorservice.Constant.IMG;
import static com.darker.motorservice.Constant.KEY_CHAT;
import static com.darker.motorservice.Constant.KEY_IMAGE;
import static com.darker.motorservice.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.Constant.NAME;
import static com.darker.motorservice.Constant.PHOTO;
import static com.darker.motorservice.Constant.SERVICE;
import static com.darker.motorservice.Constant.STATUS;
import static com.darker.motorservice.Constant.TEL_NUM;
import static com.darker.motorservice.Constant.USER;

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "ChatActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int IMAGE_REQUEST = 2;

    private TextView tvNetAlert;
    private EditText edInputMessage;
    private ProgressBar progressBar;

    private String keyChat;
    private String chatWithName;
    private String telNum;
    private String status;
    private String uid;
    private String service, user, myName;

    private boolean gpsStatus;
    private boolean found = false;

    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private SharedPreferences.Editor spedChat, spedLogin;
    private String chatWithId;
    private String photo;
    private ImageButton imgBtnImage;
    private ImageButton imgBtnSend;
    private SharedPreferences spLogin;
    private ListView chatListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        googleAPI();
        getDataIntent();
        LogDataIntent();
        supportActionBar();
        initInstance();
        checkKeyChat();
        sharedPreChat();
        setVisibility();
        setOnClicked();
        setServiceAndUser();
        setAdapter();
        refreshUI();
    }

    private void supportActionBar() {
        getSupportActionBar().setTitle(chatWithName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setAdapter() {
        chatListView.setAdapter(messageAdapter);
    }

    private void setServiceAndUser() {
        if (status.equals(USER)) {
            service = chatWithId;
            user = uid;
        } else {
            service = uid;
            user = chatWithId;
        }
    }

    private void setVisibility() {
        imgBtnImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void sharedPreChat() {
        spedChat = getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        spedChat.putBoolean(ALERT, false);
        spedChat.apply();
    }

    private void checkKeyChat() {
        if (!keyChat.isEmpty()) {
            spedLogin.putString(KEY_CHAT, keyChat);
            spedLogin.apply();
        }
    }

    private void setOnClicked() {
        imgBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnImageClicked();
            }
        });
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClicked();
            }
        });
        tvNetAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshUI();
            }
        });
    }

    private void initInstance() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgBtnImage = (ImageButton) findViewById(R.id.btn_img);
        imgBtnSend = (ImageButton) findViewById(R.id.btn_send);
        chatListView = (ListView) findViewById(R.id.list);
        edInputMessage = (EditText) findViewById(R.id.ed_input_message);
        tvNetAlert = (TextView) findViewById(R.id.txt_net_alert);

        spLogin = sharedPreferencesLogin();
        myName = spLogin.getString(NAME, "");
        storageRef = FirebaseStorage.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(CHAT);
        chatMessageList = new ArrayList<ChatMessage>();
        messageAdapter = new MessageAdapter(this, R.layout.message_item, chatMessageList);
    }

    @NonNull
    private SharedPreferences sharedPreferencesLogin() {
        SharedPreferences shLogin = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        spedLogin = shLogin.edit();
        spedLogin.putString(IMG, photo);
        spedLogin.putString(CHAT_WITH_ID, chatWithId);
        spedLogin.apply();
        return shLogin;
    }

    private void LogDataIntent() {
        Log.d("chat", "." + keyChat);
        Log.d("chat", "." + chatWithId);
        Log.d("chat", "." + chatWithName);
        Log.d("chat", "." + telNum);
        Log.d("chat", "." + status);
        Log.d("chat", "." + photo);
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        keyChat = intent.getStringExtra(KEY_CHAT);
        chatWithId = intent.getStringExtra(CHAT_WITH_ID);
        chatWithName = intent.getStringExtra(CHAT_WITH_NAME);
        telNum = intent.getStringExtra(TEL_NUM);
        status = intent.getStringExtra(STATUS);
        photo = intent.getStringExtra(PHOTO);
    }

    private void googleAPI() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
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

    @Override
    protected void onStart() {
        super.onStart();
        spedChat.putBoolean(ALERT, false);
        spedChat.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        spedChat.putBoolean(ALERT, false);
        spedChat.commit();
        Log.d("Services check", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        spedChat.putBoolean(ALERT, true);
        spedChat.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        spedChat.putBoolean(ALERT, true);
        spedChat.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spedChat.putBoolean(ALERT, true);
        spedChat.commit();
    }

    private boolean stringOk(String s) {
        return s != null && !s.equals("");
    }

    public void onSendClicked() {
        String msg = edInputMessage.getText().toString();
        if (stringOk(msg))
            if (NetWorkUtils.disable(this)) {
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
                checkBeforeUploadImage(bitmap);
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void checkBeforeUploadImage(Bitmap bitmap) {
        if (NetWorkUtils.disable(this)) {
            Toast.makeText(this, "ข้อผิดพลาดเครือข่าย! ไม่สามารถส่งรูปภาพได้", Toast.LENGTH_LONG).show();
        } else {
            prepareImage(bitmap);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void prepareImage(Bitmap bitmap) {
        String date = getDateFormate("-yyyy_MM_dd_HH_mm_ss");
        final String imgName = "image/" + uid.substring(0, 5) + date + ".png";
        StorageReference mountainsRef = FirebaseStorage.getInstance().getReference().child(imgName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadImg(uploadTask, imgName, bitmap);
    }

    private void uploadImg(UploadTask uploadTask, final String imgName, final Bitmap bitmap) {
        Toast.makeText(this, "กำลังส่งรูปภาพ กรุณารอสักครู่...", Toast.LENGTH_SHORT).show();

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("upload", exception.getMessage());
                Toast.makeText(ChatActivity.this, "การส่งรูปภาพมีปัญหา โปรดลองอีกครั้ง", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("upload", "OK");
                pushMsg(KEY_IMAGE + imgName);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "ส่งรูปภาพเรียบร้อย", Toast.LENGTH_SHORT).show();
                pushStat("chat");
                pushImageToDatabase(bitmap, imgName);
            }
        });
    }

    private void pushImageToDatabase(Bitmap bitmap, String imgName) {
        PictureDatabse handle = new PictureDatabse(ChatActivity.this);
        byte[] bytes = new ImageUtils().toByte(bitmap);
        handle.addPicture(new Pictures(imgName, bytes));
    }

    private void pushMsg(String msg) {
        String time = getDateFormate("yyyy-MM-dd HH:mm:ss");
        mDatabase
                .child(keyChat)
                .child(DATA)
                .push()
                .setValue(new ChatMessage(time, myName, msg, status, ""));
        edInputMessage.setText("");
    }

    @SuppressLint("SimpleDateFormat")
    private String getDateFormate(String pattern){
        Date date = new Date();
        return new SimpleDateFormat(pattern).format(date);
    }

    private void pushStat(final String type) {
        DatabaseReference dbStat = FirebaseDatabase.getInstance().getReference().child("stat");
        String my = getDateFormate("yyyy-MM");
        String day = getDateFormate("dd-MM-yyyy");
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

    private void refreshUI() {
        progressBar.setVisibility(View.VISIBLE);
        if (NetWorkUtils.disable(this)) {
            tvNetAlert.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            edInputMessage.setFocusableInTouchMode(true);
            tvNetAlert.setVisibility(View.GONE);
            find();
        }
    }

    private void find() {
        found = false;
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (!eachForloop(data)) break;
                }
                if (!found) mDatabase.push().setValue(new NewChat(service, user));
                checkKeyChatEmpty();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private boolean eachForloop(DataSnapshot data){
        if (data.getKey().equals("data")) {
            mDatabase.child(data.getKey()).removeValue();
            return true;
        }
        try {
            if (data.child(SERVICE).getValue().toString().equals(service) &&
                    data.child(USER).getValue().toString().equals(user)) {
                keyChat = data.getKey();
                found = true;
                spedLogin.putString(KEY_CHAT, keyChat);
                spedLogin.commit();
                return false;
            }
        } catch (Exception e) {
            Log.d("ex", e.getMessage());
        }
        return true;
    }

    private void checkKeyChatEmpty(){
        if (keyChat.isEmpty()) {
            find();
            progressBar.setVisibility(View.GONE);
        }else{
            query();
        }
    }

    private void query() {
        final int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
        mDatabase.child(keyChat).child(DATA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessageList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = data.getValue(ChatMessage.class);
                    removeChat(chatMessage, data, m);
                    prepareLoadImage(chatMessage);
                }
                sortChatList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removeChat(ChatMessage chatMessage, DataSnapshot data, int m){
        int t = Integer.parseInt(chatMessage.getDate().split("-")[1]);
        Log.d("Chat", m + " : " + t);
        if (t > 10) {
            if (t - 10 == m) {
                if (chatMessage.getMessage().contains(KEY_IMAGE)) {
                    storageRef.child(chatMessage.getMessage().replace(KEY_IMAGE, "")).delete();
                }
                mDatabase.child(keyChat).child(DATA).child(data.getKey()).removeValue();
            }
        } else if (t + 2 <= m) {
            if (chatMessage.getMessage().contains(KEY_IMAGE)) {
                storageRef.child(chatMessage.getMessage().replace(KEY_IMAGE, "")).delete();
            }
            mDatabase.child(keyChat).child(DATA).child(data.getKey()).removeValue();
        }
    }

    private void sortChatList(){
        Collections.sort(chatMessageList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        messageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void prepareLoadImage(ChatMessage chatMessage){
        String path = checkMessage(chatMessage);
        if (path == null) return;
        if (!setBitmap(path, chatMessage)) return;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_edit_white);
        chatMessage.setBitmap(bitmap);
        addChatMessageToList(chatMessage);
        loadImg(chatMessage, path);
    }

    private String checkMessage(ChatMessage chatMessage){
        if (chatMessage == null) return null;
        if (chatMessage.getMessage() == null) return null;

        if (chatMessage.getMessage().contains(KEY_IMAGE)) {
            String path = chatMessage.getMessage().replace(KEY_IMAGE, "");
            return path;
        } else {
            addChatMessageToList(chatMessage);
            return null;
        }
    }

    private void addChatMessageToList(ChatMessage chatMessage){
        chatMessageList.add(chatMessage);
        messageAdapter.notifyDataSetChanged();
    }

    private boolean setBitmap(String path, ChatMessage chatMessage){
        final PictureDatabse handle = new PictureDatabse(this);
        if (handle.hasPicture(path)) {
            Log.d("hasPicture", "YES");
            byte[] bytes = handle.getPicture(path).getPicture();
            Bitmap bitmap = new ImageUtils().convertToBitmap(bytes);
            chatMessage.setBitmap(bitmap);
            addChatMessageToList(chatMessage);
            return true;
        }
        return false;
    }

    private void loadImg(final ChatMessage chatMessage, final String path) {
        StorageReference islandRef = FirebaseStorage.getInstance().getReference().child(path);
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = new ImageUtils().convertToBitmap(bytes);
                addPictureToDatabaseWithBytes(bytes, path);
                removeThenAddChatMessage(chatMessage, bitmap);
                Log.d("load Picture", "OK");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }

    private void addPictureToDatabaseWithBytes(byte[] bytes, String path){
        PictureDatabse handle = new PictureDatabse(this);
        handle.addPicture(new Pictures(path, bytes));
    }

    private void removeThenAddChatMessage(ChatMessage chatMessage, Bitmap bitmap){
        try {
            int index = chatMessageList.indexOf(chatMessage);
            ChatMessage c = chatMessageList.get(index);
            c.setBitmap(bitmap);
            chatMessageList.remove(index);
            chatMessageList.add(c);
            messageAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d("Excep chatact", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (status.equals(USER)) {
            if (NetWorkUtils.disable(this))
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
        if (NetWorkUtils.disable(this)) return;
        checkGpsStatus();
        if (!gpsStatus) return;

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
                        bitmap = new ImageUtils().scaleBitmap(bitmap, 720);
                    else
                        bitmap = new ImageUtils().scaleBitmap(bitmap, bitmap.getWidth());
                    dialogImg(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsStatus) {
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

    private void back() {
        ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

        spedChat.putBoolean(ALERT, true);
        spedChat.commit();
        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {
            startActivity(new Intent(this, MainActivity.class));
            Log.i("Current", "This is last activity in the stack");
        }
        finish();
    }
}