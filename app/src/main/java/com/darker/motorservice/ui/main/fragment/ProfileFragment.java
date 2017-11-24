package com.darker.motorservice.ui.main.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.service.BackgroundService;
import com.darker.motorservice.ui.login.LoginActivity;
import com.darker.motorservice.ui.map.MapsActivity;
import com.darker.motorservice.ui.update_data_service.UpdateDataServiceActivity;
import com.darker.motorservice.ui.update_image.UpdateImageActivity;
import com.darker.motorservice.ui.update_password.UpdatePasswordActivity;
import com.darker.motorservice.utils.ImageUtils;
import com.darker.motorservice.utils.NetWorkUtils;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.darker.motorservice.utils.Constant.COVER;
import static com.darker.motorservice.utils.Constant.ID;
import static com.darker.motorservice.utils.Constant.IMG;
import static com.darker.motorservice.utils.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utils.Constant.LATLNG;
import static com.darker.motorservice.utils.Constant.NAME;
import static com.darker.motorservice.utils.Constant.ONLINE;
import static com.darker.motorservice.utils.Constant.PHOTO;
import static com.darker.motorservice.utils.Constant.SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.USER;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    private String id, status;
    private View view;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ServicesItem servicesItem;
    private FragmentActivity activity;
    private boolean mStatus;
    private Switch mSwitch;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;
        activity = getActivity();

        initDataFromSharedPreferences(view);
        switchOnOff(view);
        checkStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (status.equals(SERVICE))
            service();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_user_logout:
                confirm();
                break;
            case R.id.btn_service_logout:
                confirm();
                break;
            case R.id.cover_service:
                startUpdateImageActivity(true, servicesItem.getCover());
                break;
            case R.id.profile_service:
                startUpdateImageActivity(false, servicesItem.getPhoto());
                break;
            case R.id.on_map:
                startMapsActivity();
                break;
            case R.id.btn_update_data:
                startUpdateDataServiceActivity();
                break;
            case R.id.btn_update_pass:
                startActivity(new Intent(activity, UpdatePasswordActivity.class));
                break;
            default: break;
        }
    }

    private void checkStatus() {
        if (status.equals(USER))
            user();
        else
            service();
    }

    private void switchOnOff(View view) {
        mSwitch = (Switch) view.findViewById(R.id.status);
        if (mStatus) {
            mSwitch.setBackgroundResource(R.color.openLight);
        } else {
            mSwitch.setBackgroundResource(R.color.closeLight);
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void initDataFromSharedPreferences(View view) {
        preferences = view.getContext().getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        editor = preferences.edit();
        id = preferences.getString(ID, "");
        status = preferences.getString(STATUS, "");
        mStatus = preferences.getBoolean(ONLINE, false);
    }

    private void user() {
        String name = preferences.getString(NAME, "");
        String photo = preferences.getString(PHOTO, "pro.png");
        String url = "https://graph.facebook.com/" + photo + "/picture?height=250&width=250";

        view.findViewById(R.id.for_user).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.name_user)).setText(name);
        ImageView imageView = (ImageView) view.findViewById(R.id.profile_user);
        Picasso.with(view.getContext()).load(url).error(R.drawable.ic_account_circle_large).into(imageView);
    }

    private void startUpdateImageActivity(boolean isCover, String imageName){
        Intent intent = new Intent(activity, UpdateImageActivity.class);
        intent.putExtra(COVER, isCover);
        intent.putExtra(IMG, imageName);
        intent.putExtra(ID, id);
        startActivity(intent);
    }

    private void service() {
        ServiceDatabase serviceDatabase = new ServiceDatabase(activity);
        servicesItem = serviceDatabase.getService(id);

        showUiForOwnerStore();
        checkNetworkForSetBackGround();
        DatabaseReference dbRef = getDatabaseReference();
        checkOnlineStatus(dbRef);
        onSwitchChange(dbRef);
        setData();
    }

    private void checkNetworkForSetBackGround() {
        if (NetWorkUtils.disable(getContext()))
            mSwitch.setBackgroundResource(R.color.white);
    }

    private void showUiForOwnerStore() {
        view.findViewById(R.id.for_service).setVisibility(View.VISIBLE);
    }

    private void onSwitchChange(final DatabaseReference dbRef) {
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkNetworkForSetSwitch(isChecked)) return;
                setOnlineStatus(isChecked, dbRef);
            }
        });
    }

    private void setOnlineStatus(boolean isChecked, DatabaseReference db) {
        if (isChecked != mStatus) {
            if (isChecked) {
                db.setValue(true);
                Toast.makeText(getContext(), "เปิดร้าน", Toast.LENGTH_SHORT).show();
            } else {
                db.setValue(false);
                Toast.makeText(getContext(), "ปิดร้าน", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkNetworkForSetSwitch(boolean isChecked) {
        if (NetWorkUtils.disable(getContext())){
            Toast.makeText(getContext(), "เครือข่ายมีปัญหา! ไม่สามารถเปลี่ยนสถานะร้านได้", Toast.LENGTH_LONG).show();
            mSwitch.setChecked(!isChecked);
            return true;
        }
        return false;
    }
    
    private void checkOnlineStatus(DatabaseReference dbRef) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mStatus = (boolean) dataSnapshot.getValue();
                editor.putBoolean(ONLINE, mStatus);
                editor.commit();
                mSwitch.setChecked(mStatus);
                if (mStatus) {
                    mSwitch.setBackgroundResource(R.color.openLight);
                } else {
                    mSwitch.setBackgroundResource(R.color.closeLight);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference().child(STATUS).child(servicesItem.getId());
    }

    private void startUpdateDataServiceActivity() {
        Intent intent = new Intent(activity, UpdateDataServiceActivity.class);
        intent.putExtra(ID, servicesItem.getId());
        startActivity(intent);
    }

    private void startMapsActivity() {
        Intent intent = new Intent(activity, MapsActivity.class);
        intent.putExtra(NAME, servicesItem.getName());
        intent.putExtra(LATLNG, servicesItem.getLatlng());
        startActivity(intent);
    }

    private void setData() {
        ImageUtils image = new ImageUtils();
        Bitmap cover, profile;
        try{
            cover = image.convertToBitmap(servicesItem.getImgCover());
        } catch (Exception e){
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        }
        try{
            profile = image.convertToBitmap(servicesItem.getImgProfile());
        }catch (Exception e){
            profile = BitmapFactory.decodeResource(getResources(), R.drawable.pro);
        }
        ((ImageView) view.findViewById(R.id.cover_service)).setImageBitmap(cover);
        ((ImageView) view.findViewById(R.id.profile_service)).setImageBitmap(profile);

        ((TextView) view.findViewById(R.id.service_name)).setText(servicesItem.getName());
        ((TextView) view.findViewById(R.id.service_pos)).setText(servicesItem.getPos());
        ((TextView) view.findViewById(R.id.tel)).setText(servicesItem.getTel());
        ((TextView) view.findViewById(R.id.email)).setText(servicesItem.getEmail());
        ((TextView) view.findViewById(R.id.latlng)).setText(servicesItem.getLatlng());
        ((TextView) view.findViewById(R.id.work_time)).setText(servicesItem.getWorkTime());
        ((TextView) view.findViewById(R.id.services)).setText(servicesItem.getService());
        ((TextView) view.findViewById(R.id.distribute)).setText(servicesItem.getDistribute());
    }

    private void confirm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setMessage("คุณกำลังจะออกจากระบบ");
        builder.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void logout(){
        if (NetWorkUtils.disable(getContext())){
            Toast.makeText(activity, "เครือข่ายมีปัญหา! ไม่สามารถออกจากระบบได้", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPref = activity.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        activity.finish();
        startActivity(new Intent(activity, LoginActivity.class));
        activity.stopService(new Intent(activity, BackgroundService.class));
    }
}
