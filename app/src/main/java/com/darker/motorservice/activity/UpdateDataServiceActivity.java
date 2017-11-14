package com.darker.motorservice.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.utils.LoadService;
import com.darker.motorservice.utils.NetWork;
import com.darker.motorservice.data.Services;
import com.darker.motorservice.database.ServiceHandle;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.SERVICE;

public class UpdateDataServiceActivity extends AppCompatActivity {

    private int PLACE_PICKER_REQUEST = 1;
    private boolean GpsStatus;

    private String id, latlng;
    private Services services;
    private EditText editName, editPos, editTel, editEmail, editOne, editTwo, editThree, editService, editDistribute;
    private TextView txtLatlng;
    private Button btnSave;
    private List<Services> listSer;
    private ProgressBar progressBar;
    private ServiceHandle handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data_service);

        getSupportActionBar().setTitle(getResources().getString(R.string.edit_data));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handle = new ServiceHandle(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSave = (Button) findViewById(R.id.btn_save);
        id = getIntent().getStringExtra(ID);
        services = handle.getService(id);
        listSer = handle.getAllSerivce();
        setData();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setData(){
        editName = (EditText) findViewById(R.id.form_name);
        editPos = (EditText) findViewById(R.id.form_pos);
        editTel = (EditText) findViewById(R.id.form_tel);
        editEmail = (EditText) findViewById(R.id.form_email);
        latlng = services.getLatlng();
        txtLatlng = (TextView) findViewById(R.id.form_latlng);
        editOne = (EditText) findViewById(R.id.form_one);
        editTwo = (EditText) findViewById(R.id.form_two);
        editThree = (EditText) findViewById(R.id.form_three);
        editService = (EditText) findViewById(R.id.form_service);
        editDistribute = (EditText) findViewById(R.id.form_distribute);

        String[] s = services.getWorkTime().split("\n");
        String three = "";
        if (s.length > 2) three = s[2];

        editName.setText(services.getName());
        editPos.setText(services.getPos());
        editTel.setText(services.getTel());
        editEmail.setText(services.getEmail());
        txtLatlng.setText(services.getLatlng());
        editOne.setText(s[0]);
        editTwo.setText(s[1]);
        editThree.setText(three);
        editService.setText(services.getService());
        editDistribute.setText(services.getDistribute());
    }

    public void onSaveClicked(View view){
        btnSave.setClickable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage("บันทึกข้อมูล?");
        builder.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save();
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void save(){
        String formName = editName.getText().toString();
        String formPos = editPos.getText().toString();
        String formTel = editTel.getText().toString();
        String formEmail = editEmail.getText().toString();
        String formOne = editOne.getText().toString();
        String formTwo = editTwo.getText().toString();
        String formThree = editThree.getText().toString();
        String formService = editService.getText().toString();
        String formDistribute = editDistribute.getText().toString();

        if (formName.isEmpty() || formPos.isEmpty() || formTel.isEmpty() || formEmail.isEmpty() ||
                formOne.isEmpty() || formTwo.isEmpty() || formService.isEmpty() || formDistribute.isEmpty()){
            alert("กรุณากรอกข้อมูลให้ครบ");
            return;
        }

        if (formTel.length() < 9){
            alert("เบอร์โทรศัพท์ไม่ถูกต้อง!");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(formEmail).matches()) {
            alert("กรุณากรอกอีเมลที่ถูกต้อง!");
            return;
        }

        if (hasEmail(formEmail)) {
            alert("อีเมลนี้ถูกใช้แล้ว! โปรดใช้อีเมลอื่น");
            return;
        }

        String work = formOne + "\n" + formTwo + "\n" + formThree;

        if (NetWork.disable(this)){
            alert("เครือข่ายมีปัญหา! ไม่สามารถบันทึกได้");
        }else{
            progressBar.setVisibility(View.VISIBLE);
            final Services s = new Services();
            s.setId(id);
            s.setName(formName);
            s.setPos(formPos);
            s.setEmail(formEmail);
            s.setTel(formTel);
            s.setPhoto(services.getPhoto());
            s.setCover(services.getCover());
            s.setLatlng(latlng);
            s.setWorkTime(work);
            s.setService(formService);
            s.setDistribute(formDistribute);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null){
                alert("ไม่สามารถบันทึกได้! โปรดออกจากระบบและเข้าสู่ระบบ แล้วลองใหม่อีกครั้ง");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (formEmail.equals(services.getEmail())){
                update(s);
            }else {
                user.updateEmail(formEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            update(s);
                        } else {
                            alert("ไม่สามารถบันทึกได้! โปรดลองอีกครั้ง");
                            progressBar.setVisibility(View.GONE);
                            Log.d("save no", task.getException().getMessage());
                        }
                    }
                });
            }
        }
    }

    private void update(Services s){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(SERVICE).child(services.getId());
        dbRef.setValue(s);
        new LoadService(UpdateDataServiceActivity.this).loadData();
        progressBar.setVisibility(View.GONE);
        s.setImgProfile(services.getImgProfile());
        s.setImgCover(services.getImgCover());
        handle.updateService(s);
        finish();
        Toast.makeText(UpdateDataServiceActivity.this, "บันทึกเรียบร้อย", Toast.LENGTH_LONG).show();
    }

    private boolean hasEmail(String email) {
        if (email.equals(services.getEmail()))
            return false;
        for (Services s : listSer) {
            if (s.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private void alert(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        btnSave.setClickable(true);
    }

    public void onEditLatlngClicked(View view){
        if (NetWork.disable(this)) return;
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
                String s = place.getLatLng().toString();
                latlng = s.replace("lat/lng: ", "").replace("(", "").replace(")", "");
                txtLatlng.setText(latlng);
            }
        }else{
            Log.d("requestCode", "." + requestCode);
        }
    }

    public void checkGpsStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!GpsStatus){
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
}
