package com.darker.motorservice.ui.chat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.darker.motorservice.R;
import com.darker.motorservice.database.PictureDatabase;
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.firebase.StatsConstant;
import com.darker.motorservice.sharedpreferences.AccountType;
import com.darker.motorservice.sharedpreferences.SharedPreferencesUtil;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.ui.chat.model.NewChatItem;
import com.darker.motorservice.ui.main.MainActivity;
import com.darker.motorservice.ui.main.callback.ImageUploadCallback;
import com.darker.motorservice.ui.main.model.PictureItem;
import com.darker.motorservice.utility.CallPhoneUtil;
import com.darker.motorservice.utility.DateUtil;
import com.darker.motorservice.utility.GPSUtil;
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.utility.StringUtil;
import com.darker.motorservice.utility.ToasAlert;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.darker.motorservice.utility.Constant.CHAT;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.utility.Constant.DATA;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.utility.Constant.KEY_IMAGE;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.PHOTO;
import static com.darker.motorservice.utility.Constant.SERVICE;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.TEL_NUM;
import static com.darker.motorservice.utility.Constant.USER;

public class ChatActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public static final String TAG = "ChatActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int IMAGE_REQUEST = 2;

    private TextView tvNetAlert;
    private EditText edInputMessage;
    private ProgressBar progressBar;
    private ImageButton imgBtnImage;
    private ImageButton imgBtnSend;
    private ListView chatListView;

    private String keyChat;
    private String chatWithName;
    private String phoneNumber;
    private String status;
    private String uid;
    private String service;
    private String user;
    private String myName;
    private String chatWithId;
    private String photo;

    private boolean gpsStatus;
    private boolean found = false;

    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private MessageAdapter messageAdapter;
    private List<ChatMessageItem> chatMessageItemList;

    private SharedPreferences.Editor spedLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        googleAPI();
        getDataIntent();
        LogDataIntent();
        supportActionBar();
        bindView();
        sharedPreferencesLogin();
        initInstance();
        checkKeyChat();
        setVisibility();
        setServiceAndUser();
        setAdapter();
        refreshUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        startMainActivityOrOnlyFinish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startMainActivityOrOnlyFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesUtil.disableChatAlert(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil.enableChatAlert(this);
    }

    @Override
    public void onClick(View view) {
        if (view == imgBtnImage) {
            showImageStoreSelect();
        } else if (view == imgBtnSend) {
            validateText();
        } else if (view == tvNetAlert) {
            refreshUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (status.equals(USER)) {
            if (!NetworkUtil.isNetworkAvailable(this))
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
                CallPhoneUtil.callPhoneDialog(this, chatWithId, chatWithName, phoneNumber);
                return true;
            case R.id.menu_gps:
                validateGPSStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println(connectionResult.getErrorMessage());
        Log.d("onConnectionFailed", connectionResult.getErrorMessage());
    }

    private void supportActionBar() {
        getSupportActionBar().setTitle(chatWithName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setAdapter() {
        chatListView.setAdapter(messageAdapter);
    }

    private void setServiceAndUser() {
        if (AccountType.isCustomer(this)) {
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

    private void checkKeyChat() {
        if (!keyChat.isEmpty()) {
            spedLogin.putString(KEY_CHAT, keyChat);
            spedLogin.apply();
        }
    }

    private void bindView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgBtnImage = (ImageButton) findViewById(R.id.btn_img);
        imgBtnSend = (ImageButton) findViewById(R.id.btn_send);
        chatListView = (ListView) findViewById(R.id.list);
        edInputMessage = (EditText) findViewById(R.id.ed_input_message);
        tvNetAlert = (TextView) findViewById(R.id.txt_net_alert);
    }

    private void initInstance() {
        storageRef = FirebaseUtil.getStorageReference();
        uid = FirebaseUtil.getUid();
        mDatabase = FirebaseUtil.getChildData(CHAT);
        chatMessageItemList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.message_item, chatMessageItemList);
    }

    private void sharedPreferencesLogin() {
        SharedPreferences shLogin = SharedPreferencesUtil.getLoginPreferences(this);
        spedLogin = shLogin.edit();
        spedLogin.putString(IMG, photo);
        spedLogin.putString(CHAT_WITH_ID, chatWithId);
        spedLogin.apply();
        myName = shLogin.getString(NAME, "");
    }

    private void LogDataIntent() {
        Log.d(TAG, "." + keyChat);
        Log.d(TAG, "." + chatWithId);
        Log.d(TAG, "." + chatWithName);
        Log.d(TAG, "." + phoneNumber);
        Log.d(TAG, "." + status);
        Log.d(TAG, "." + photo);
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        keyChat = intent.getStringExtra(KEY_CHAT);
        chatWithId = intent.getStringExtra(CHAT_WITH_ID);
        chatWithName = intent.getStringExtra(CHAT_WITH_NAME);
        phoneNumber = intent.getStringExtra(TEL_NUM);
        status = intent.getStringExtra(STATUS);
        photo = intent.getStringExtra(PHOTO);
    }

    private void googleAPI() {
        new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    public void validateText() {
        String message = edInputMessage.getText().toString();
        if (!StringUtil.stringOk(message)) {
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(this)) {
            ToasAlert.alert(this, R.string.fault_network_cannot_send_message);
            return;
        }

        if (keyChat.isEmpty()) {
            find();
        }

        pushMessage(message);

        if (AccountType.isCustomer(this)) {
            setStatsChat();
        }
    }

    public void showImageStoreSelect() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_REQUEST);
    }

    private void confirmSendImageDialog(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View viewInflate = inflater.inflate(R.layout.image, null);
        builder.setView(viewInflate);

        ImageView imageView = (ImageView) viewInflate.findViewById(R.id.img);
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
        if (!NetworkUtil.isNetworkAvailable(this)) {
            ToasAlert.alert(this, R.string.fault_network_cannot_send_image);
        } else {
            imageUpload(bitmap);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void imageUpload(Bitmap bitmap) {
        String imageName = ImageUtil.getImageName(uid);
        ImageUtil.uploadImage(imageName, bitmap, getImageUploadCallback());
        ToasAlert.alert(this, R.string.sending_image);
    }

    @NonNull
    private ImageUploadCallback getImageUploadCallback() {
        return new ImageUploadCallback() {

            @Override
            public void onSuccess(String imageName, Bitmap bitmap) {
                Log.d("upload", "OK");
                pushMessage(KEY_IMAGE + imageName);
                ToasAlert.alert(ChatActivity.this, R.string.send_image_complete);
                setStatsChat();
                pushImageToDatabase(bitmap, imageName);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String imageName) {
                ToasAlert.alert(ChatActivity.this, R.string.send_image_problem);
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    private void pushImageToDatabase(Bitmap bitmap, String imgName) {
        PictureDatabase handle = new PictureDatabase(ChatActivity.this);
        byte[] bytes = ImageUtil.convertBitmapToByte(bitmap);
        handle.addPicture(new PictureItem(imgName, bytes));
    }

    private void pushMessage(String message) {
        String timeDate = DateUtil.getDateFormat(DateUtil.DatePattern.TIME_DATE);
        mDatabase.child(keyChat)
                .child(DATA)
                .push()
                .setValue(new ChatMessageItem(timeDate, myName, message, status, ""));
        edInputMessage.setText("");
    }

    private void refreshUI() {
        progressBar.setVisibility(View.VISIBLE);
        if (!NetworkUtil.isNetworkAvailable(this)) {
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
                if (!found){
                    mDatabase.push().setValue(new NewChatItem(service, user));
                }
                findOrQuery();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private boolean eachForloop(DataSnapshot data) {
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

    private void findOrQuery() {
        if (keyChat.isEmpty()) {
            find();
            progressBar.setVisibility(View.GONE);
        } else {
            queryDataByKeyChat();
        }
    }

    private void queryDataByKeyChat() {
        final int currentMonth = DateUtil.getCurrentMonth();
        mDatabase.child(keyChat).child(DATA)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessageItemList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatMessageItem chatMessageItem = data.getValue(ChatMessageItem.class);
                    removeChat(chatMessageItem, data, currentMonth);
                    prepareLoadImage(chatMessageItem);
                }
                sortChatList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ToasAlert.alert(ChatActivity.this, R.string.cannot_load_data);
            }
        });
    }

    private void removeChat(ChatMessageItem chatMessageItem, DataSnapshot data, int currentMonth) {
        int monthChat = DateUtil.getMonthFromChat(chatMessageItem.getDate());
        Log.d("ChatItem", currentMonth + " : " + monthChat);
        if (monthChat > 10) {
            if (monthChat - 10 == currentMonth) {
                if (chatMessageItem.getMessage().contains(KEY_IMAGE)) {
                    storageRef.child(chatMessageItem.getMessage().replace(KEY_IMAGE, "")).delete();
                }
                mDatabase.child(keyChat).child(DATA).child(data.getKey()).removeValue();
            }
        } else if (monthChat + 2 <= currentMonth) {
            if (chatMessageItem.getMessage().contains(KEY_IMAGE)) {
                storageRef.child(chatMessageItem.getMessage().replace(KEY_IMAGE, "")).delete();
            }
            mDatabase.child(keyChat).child(DATA).child(data.getKey()).removeValue();
        }
    }

    private void sortChatList() {
        Collections.sort(chatMessageItemList, new Comparator<ChatMessageItem>() {
            @Override
            public int compare(ChatMessageItem o1, ChatMessageItem o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        messageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void prepareLoadImage(ChatMessageItem chatMessageItem) {
        String path = checkMessage(chatMessageItem);
        if (path == null) return;
        if (!setBitmap(path, chatMessageItem)) return;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_edit_white);
        chatMessageItem.setBitmap(bitmap);
        addChatMessageToList(chatMessageItem);
        loadImg(chatMessageItem, path);
    }

    private String checkMessage(ChatMessageItem chatMessageItem) {
        if (chatMessageItem == null) return null;
        if (chatMessageItem.getMessage() == null) return null;

        if (chatMessageItem.getMessage().contains(KEY_IMAGE)) {
            return chatMessageItem.getMessage().replace(KEY_IMAGE, "");
        } else {
            addChatMessageToList(chatMessageItem);
            return null;
        }
    }

    private void addChatMessageToList(ChatMessageItem chatMessageItem) {
        chatMessageItemList.add(chatMessageItem);
        messageAdapter.notifyDataSetChanged();
    }

    private boolean setBitmap(String path, ChatMessageItem chatMessageItem) {
        final PictureDatabase handle = new PictureDatabase(this);
        if (handle.hasPicture(path)) {
            Log.d("hasPicture", "YES");
            byte[] bytes = handle.getPicture(path).getPicture();
            Bitmap bitmap = ImageUtil.convertByteToBitmap(bytes);
            chatMessageItem.setBitmap(bitmap);
            addChatMessageToList(chatMessageItem);
            return true;
        }
        return false;
    }

    private void loadImg(final ChatMessageItem chatMessageItem, final String path) {
        StorageReference islandRef = FirebaseUtil.getChildStorage(path);
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = ImageUtil.convertByteToBitmap(bytes);
                addPictureToDatabaseWithBytes(bytes, path);
                removeThenAddChatMessage(chatMessageItem, bitmap);
                Log.d("load Picture", "OK");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("load image", e.getMessage());
            }
        });
    }

    private void addPictureToDatabaseWithBytes(byte[] bytes, String path) {
        PictureDatabase handle = new PictureDatabase(this);
        handle.addPicture(new PictureItem(path, bytes));
    }

    private void removeThenAddChatMessage(ChatMessageItem chatMessageItem, Bitmap bitmap) {
        try {
            int index = chatMessageItemList.indexOf(chatMessageItem);
            ChatMessageItem c = chatMessageItemList.get(index);
            c.setBitmap(bitmap);
            chatMessageItemList.remove(index);
            chatMessageItemList.add(c);
            messageAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d("Excep chatact", e.getMessage());
        }
    }

    public void validateGPSStatus() {
        if (!NetworkUtil.isNetworkAvailable(this)) return;
        if (GPSUtil.isGPSEnable(this)){
            confirmGPSSettingsDialog();
            return;
        }
        startSelectPlace();
    }

    private void startSelectPlace() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            Log.d("place", "ok");
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Log.e("place", e.getMessage());
            ToasAlert.alert(this, R.string.device_not_support_gps);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        resultPlacePicker(requestCode, resultCode, data);
        resultImageRequest(requestCode, resultCode, data);
    }

    private void resultImageRequest(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            setBitmapForDialog(data);
        }
    }

    private void resultPlacePicker(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                pushMessageWithLatLng(data);
            }
        } else {
            Log.d("requestCode", "." + requestCode);
        }
    }

    private void pushMessageWithLatLng(Intent data) {
        Place place = PlacePicker.getPlace(this, data);
        pushMessage(place.getLatLng().toString());
        setStatsChat();
    }

    private void setStatsChat(){
        FirebaseUtil.setValueStats(chatWithId, StatsConstant.CHAT);
    }

    private void setBitmapForDialog(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri != null) {
            try {
                Bitmap bitmap = ImageUtil.getImageMediaStore(this, imageUri);
                if (bitmap.getWidth() > 720)
                    bitmap = ImageUtil.scaleBitmap(bitmap, 720);
                else
                    bitmap = ImageUtil.scaleBitmap(bitmap, bitmap.getWidth());
                confirmSendImageDialog(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void confirmGPSSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage(R.string.suggest_open_gps);
        builder.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                GPSUtil.gpsSettings(ChatActivity.this);
            }
        });
        builder.setNegativeButton(R.string.text_cancel, null);
        builder.show();
    }

    private void startMainActivityOrOnlyFinish() {
        if (ClassRunningManager.isOneActivityRunning(this)) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}