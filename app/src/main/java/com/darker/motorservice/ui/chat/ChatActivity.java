package com.darker.motorservice.ui.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.firebase.StatsConstant;
import com.darker.motorservice.sharedpreferences.AccountType;
import com.darker.motorservice.sharedpreferences.SharedPreferencesUtil;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.ui.chat.model.NewChatItem;
import com.darker.motorservice.ui.main.MainActivity;
import com.darker.motorservice.ui.main.callback.ImageUploadCallback;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_ID;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_NAME;
import static com.darker.motorservice.utility.Constant.DATA;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.IMG;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.NAME;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.PHOTO;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.TEL_NUM;
import static com.darker.motorservice.utility.ImageUtil.KEY_IMAGE;

public class ChatActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "ChatActivity";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int IMAGE_REQUEST = 2;

    private TextView tvNetworkAlert;
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
    private String traderId;
    private String customerId;
    private String myName;
    private String chatWithId;
    private String photo;

    private DatabaseReference dbChat;
    private MessageAdapter messageAdapter;
    private List<ChatMessageItem> chatMessageItemList;
    private SharedPreferences.Editor prefsLoginEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        googleAPI();
        getDataIntent();
        LogDataIntent();
        supportActionBar();
        bindView();
        setupViewOnClick();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AccountType.isCustomer(this)) {
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
            case R.id.menu_call_phone:
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        resultPlacePicker(requestCode, resultCode, data);
        resultImageRequest(requestCode, resultCode, data);
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
            traderId = chatWithId;
            customerId = uid;
        } else {
            traderId = uid;
            customerId = chatWithId;
        }
    }

    private void setVisibility() {
        imgBtnImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void checkKeyChat() {
        if (!keyChat.isEmpty()) {
            prefsLoginEditor.putString(KEY_CHAT, keyChat);
            prefsLoginEditor.apply();
        }
    }

    private void bindView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgBtnImage = (ImageButton) findViewById(R.id.btn_img);
        imgBtnSend = (ImageButton) findViewById(R.id.btn_send);
        chatListView = (ListView) findViewById(R.id.list);
        edInputMessage = (EditText) findViewById(R.id.ed_input_message);
        tvNetworkAlert = (TextView) findViewById(R.id.txt_net_alert);
    }

    private void setupViewOnClick() {
        imgBtnImage.setOnClickListener(view -> showImageStoreSelect());
        imgBtnSend.setOnClickListener(view -> validateText());
        tvNetworkAlert.setOnClickListener(view -> refreshUI());
    }

    private void initInstance() {
        uid = FirebaseUtil.getUid();
        dbChat = FirebaseUtil.getChildData(FirebaseUtil.DatabaseChild.CHAT);
        chatMessageItemList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.message_item, chatMessageItemList);
    }

    private void sharedPreferencesLogin() {
        SharedPreferences prefsLogin = SharedPreferencesUtil.getLoginPreferences(this);
        prefsLoginEditor = prefsLogin.edit();
        prefsLoginEditor.putString(IMG, photo);
        prefsLoginEditor.putString(CHAT_WITH_ID, chatWithId);
        prefsLoginEditor.apply();
        myName = prefsLogin.getString(NAME, "");
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
        status = AccountType.getAccountLogin(this);
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
        if (!StringUtil.isStringOk(message)) {
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(this)) {
            ToasAlert.alert(this, R.string.fault_network_cannot_send_message);
            return;
        }

        if (keyChat.isEmpty()) {
            queryChatFromFirebase();
        }

        pushMessageToChatFirebase(message);

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

        builder.setTitle(R.string.send_image);
        builder.setPositiveButton(R.string.send, (dialog, which) -> checkBeforeUploadImage(bitmap));
        builder.setNegativeButton(R.string.cancel, null);
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
                pushMessageToChatFirebase(KEY_IMAGE + imageName);
                setStatsChat();
                ToasAlert.alert(ChatActivity.this, R.string.send_image_complete);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String imageName) {
                ToasAlert.alert(ChatActivity.this, R.string.send_image_problem);
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    private void pushMessageToChatFirebase(String message) {
        String timeDate = DateUtil.getDateFormat(DateUtil.DatePattern.TIME_DATE);
        dbChat.child(keyChat)
                .child(DATA)
                .push()
                .setValue(new ChatMessageItem(timeDate, myName, message, status, ""));
        edInputMessage.setText("");
    }

    private void refreshUI() {
        progressBar.setVisibility(View.VISIBLE);
        if (!NetworkUtil.isNetworkAvailable(this)) {
            tvNetworkAlert.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            edInputMessage.setFocusableInTouchMode(true);
            tvNetworkAlert.setVisibility(View.GONE);
            queryChatFromFirebase();
        }
    }

    private void queryChatFromFirebase() {
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsChildChat : dataSnapshot.getChildren()) {
                    checkChildBug(dsChildChat);
                    if (isFoundKeyChat(dsChildChat)) break;
                }
                hasKeyChatPushData();
                findOrQuery();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void hasKeyChatPushData() {
        if (!keyChat.isEmpty()) {
            dbChat.push().setValue(new NewChatItem(traderId, customerId));
        }
    }

    private void checkChildBug(DataSnapshot dsChildChat) {
        if (dsChildChat.getKey().equals(FirebaseUtil.DatabaseChild.DATA)) {
            dbChat.child(dsChildChat.getKey()).removeValue();
        }
    }

    private boolean isFoundKeyChat(DataSnapshot dsChildChat) {
        if (isFoundServiceAndUser(dsChildChat)) {
            keyChat = dsChildChat.getKey();
            prefsLoginEditor.putString(SharedPreferencesUtil.KEY_CHAT, keyChat);
            prefsLoginEditor.commit();
            return true;
        }
        return false;
    }

    private boolean isFoundServiceAndUser(DataSnapshot data) {
        Object serviceObj = data.child(FirebaseUtil.DatabaseChild.SERVICE).getValue();
        Object userObj = data.child(FirebaseUtil.DatabaseChild.USER).getValue();

        if (serviceObj == null || userObj == null) {
            return false;
        }

        return serviceObj.toString().equals(traderId) &&
                userObj.toString().equals(customerId);
    }

    private void findOrQuery() {
        if (keyChat.isEmpty()) {
            queryChatFromFirebase();
            progressBar.setVisibility(View.GONE);
        } else {
            queryDataByKeyChat();
        }
    }

    private void queryDataByKeyChat() {
        dbChat.child(keyChat)
                .child(FirebaseUtil.DatabaseChild.DATA)
                .addValueEventListener(chatMessageListener());
    }

    private void sortChatList() {
        Collections.sort(chatMessageItemList, (o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString()));
        messageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    public void validateGPSStatus() {
        if (!NetworkUtil.isNetworkAvailable(this)) return;
        if (GPSUtil.isGPSEnable(this)) {
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
        pushMessageToChatFirebase(place.getLatLng().toString());
        setStatsChat();
    }

    private void setStatsChat() {
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

    private void confirmGPSSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage(R.string.suggest_open_gps);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) ->
                GPSUtil.gpsSettings(ChatActivity.this));
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void startMainActivityOrOnlyFinish() {
        if (ClassRunningManager.isOneActivityRunning(this)) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @NonNull
    private ValueEventListener chatMessageListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessageItemList.clear();
                for (DataSnapshot dsChatChildren : dataSnapshot.getChildren()) {
                    ChatMessageItem chatMessageItem = dsChatChildren.getValue(ChatMessageItem.class);
                    FirebaseUtil.removeChatOlderThanTwoMonth(chatMessageItem, keyChat, dsChatChildren.getKey());
                    chatMessageItemList.add(chatMessageItem);
                }
                sortChatList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ToasAlert.alert(ChatActivity.this, R.string.cannot_load_data);
            }
        };
    }
}