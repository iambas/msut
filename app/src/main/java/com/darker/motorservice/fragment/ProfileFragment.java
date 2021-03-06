package com.darker.motorservice.fragment;

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
import com.darker.motorservice.activity.LoginActivity;
import com.darker.motorservice.activity.MapsActivity;
import com.darker.motorservice.activity.UpdateDataServiceActivity;
import com.darker.motorservice.activity.UpdateImageActivity;
import com.darker.motorservice.activity.UpdatePasswordActivity;
import com.darker.motorservice.utils.MyImage;
import com.darker.motorservice.utils.NetWork;
import com.darker.motorservice.data.Services;
import com.darker.motorservice.database.ServiceHandle;
import com.darker.motorservice.service.BackgroundService;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.darker.motorservice.data.Constant.COVER;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.IMG;
import static com.darker.motorservice.data.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.data.Constant.LATLNG;
import static com.darker.motorservice.data.Constant.NAME;
import static com.darker.motorservice.data.Constant.ONLINE;
import static com.darker.motorservice.data.Constant.PHOTO;
import static com.darker.motorservice.data.Constant.SERVICE;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.USER;

public class ProfileFragment extends Fragment {

    private String id, status;
    private View view;
    private SharedPreferences sh;
    private SharedPreferences.Editor ed;
    private Services services;
    private FragmentActivity activity;
    private boolean mStatus;
    private Switch sw;

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
        sh = view.getContext().getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        ed = sh.edit();
        id = sh.getString(ID, "");
        status = sh.getString(STATUS, "");
        mStatus = sh.getBoolean(ONLINE, false);
        sw = (Switch) view.findViewById(R.id.status);
        if (mStatus) {
            sw.setBackgroundResource(R.color.openLight);
        } else {
            sw.setBackgroundResource(R.color.closeLight);
        }

        if (status.equals(USER))
            user();
        else
            service();
    }

    private void user() {
        view.findViewById(R.id.btn_user_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        String name = sh.getString(NAME, "");
        String photo = sh.getString(PHOTO, "pro.png");
        String url = "https://graph.facebook.com/" + photo + "/picture?height=250&width=250";

        view.findViewById(R.id.for_user).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.name_user)).setText(name);
        ImageView imageView = (ImageView) view.findViewById(R.id.profile_user);
        Picasso.with(view.getContext()).load(url).error(R.drawable.ic_account_circle_large).into(imageView);
    }

    private void service() {
        ServiceHandle serviceHandle = new ServiceHandle(activity);
        services = serviceHandle.getService(id);

        view.findViewById(R.id.for_service).setVisibility(View.VISIBLE);
        if (!(new NetWork(getContext()).isNetworkAvailiable()))
            sw.setBackgroundResource(R.color.white);
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(services.getId());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mStatus = (boolean) dataSnapshot.getValue();
                ed.putBoolean(ONLINE, mStatus);
                ed.commit();
                sw.setChecked(mStatus);
                if (mStatus) {
                    sw.setBackgroundResource(R.color.openLight);
                } else {
                    sw.setBackgroundResource(R.color.closeLight);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // click edit cover
        view.findViewById(R.id.cover_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UpdateImageActivity.class);
                intent.putExtra(COVER, true);
                intent.putExtra(IMG, services.getCover());
                intent.putExtra(ID, id);
                startActivity(intent);
            }
        });
        // click edit profile
        view.findViewById(R.id.profile_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UpdateImageActivity.class);
                intent.putExtra(COVER, false);
                intent.putExtra(IMG, services.getPhoto());
                intent.putExtra(ID, id);
                startActivity(intent);
            }
        });
        // switch
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isNet = new NetWork(getContext()).isNetworkAvailiable();
                if (!isNet){
                    Toast.makeText(getContext(), "เครือข่ายมีปัญหา! ไม่สามารถเปลี่ยนสถานะร้านได้", Toast.LENGTH_LONG).show();
                    sw.setChecked(!isChecked);
                    return;
                }
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
        });
        // click map
        view.findViewById(R.id.on_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MapsActivity.class);
                intent.putExtra(NAME, services.getName());
                intent.putExtra(LATLNG, services.getLatlng());
                startActivity(intent);
            }
        });
        // click edit data
        view.findViewById(R.id.btn_update_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UpdateDataServiceActivity.class);
                intent.putExtra(ID, services.getId());
                startActivity(intent);
            }
        });
        // click updata password
        view.findViewById(R.id.btn_update_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, UpdatePasswordActivity.class));
            }
        });
        // click logout
        view.findViewById(R.id.btn_service_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        setData();
    }

    private void setData() {
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
        ((ImageView) view.findViewById(R.id.cover_service)).setImageBitmap(cover);
        ((ImageView) view.findViewById(R.id.profile_service)).setImageBitmap(profile);

        ((TextView) view.findViewById(R.id.service_name)).setText(services.getName());
        ((TextView) view.findViewById(R.id.service_pos)).setText(services.getPos());
        ((TextView) view.findViewById(R.id.tel)).setText(services.getTel());
        ((TextView) view.findViewById(R.id.email)).setText(services.getEmail());
        ((TextView) view.findViewById(R.id.latlng)).setText(services.getLatlng());
        ((TextView) view.findViewById(R.id.work_time)).setText(services.getWorkTime());
        ((TextView) view.findViewById(R.id.services)).setText(services.getService());
        ((TextView) view.findViewById(R.id.distribute)).setText(services.getDistribute());
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
        if (!(new NetWork(activity).isNetworkAvailiable())){
            Toast.makeText(activity, "เครือข่ายมีปัญหา! ไม่สามารถออกจากระบบได้", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPref = activity.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        activity.finish();
        startActivity(new Intent(activity, LoginActivity.class));
        activity.stopService(new Intent(activity, BackgroundService.class));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (status.equals(SERVICE))
            service();
    }
}
