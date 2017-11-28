package com.darker.motorservice.ui.update_password;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.login.LoginActivity;
import com.darker.motorservice.utility.EncodedUtil;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.service.BackgroundService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.PASSWORD;

public class UpdatePasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SharedPreferences sh;
    private SharedPreferences.Editor ed;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        getSupportActionBar().setTitle(getResources().getString(R.string.change_password));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        sh = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        ed = sh.edit();

        btnSave = (Button) findViewById(R.id.btn_save);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void onUpdatePasswordClicked(View view){
        btnSave.setClickable(false);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        String oldPass = ((EditText) findViewById(R.id.old_password)).getText().toString();
        String newPass = ((EditText) findViewById(R.id.new_password)).getText().toString();
        String newPassCon = ((EditText) findViewById(R.id.new_password_confirm)).getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty() || newPassCon.isEmpty()) {
            alert("กรุณากรอกข้อมูลให้ครบ!");
            return;
        }

        if (NetworkUtil.disable(this)){
            alert("เครือข่ายมีปัญหา! ไม่สามารถเปลี่ยนรหัสผ่านได้");
            return;
        }

        if (newPass.length() < 6 || newPassCon.length() < 6) {
            alert("รหัสผ่านใหม่อย่างน้อย 6 ตัว!");
            return;
        }

        if (!newPass.equals(newPassCon)){
            alert("รหัสผ่านใหม่ไม่ตรงกัน!");
            return;
        }

        Log.d("old password", sh.getString("password", ""));
        if (sh.getString(PASSWORD, "").equals((new EncodedUtil(oldPass)).getResult())){
            progressBar.setVisibility(View.VISIBLE);
            try {
                auth.getCurrentUser().updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            alert("เปลี่ยนรหัสผ่านเรียบร้อย");
                            logout();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            alert("มีปัญหาในการเปลี่ยนรหัสผ่าน! โปรดลองอีกครั้ง");
                        }
                    }
                });
            }catch (NullPointerException e){
                progressBar.setVisibility(View.GONE);
                alert("มีปัญหาในการเปลี่ยนรหัสผ่าน! โปรดลองอีกครั้ง");
                return;
            }
        }else{
            alert("รหัสผ่านเดิมไม่ถูกต้อง!");
            return;
        }
    }

    private void alert(String msg){
        Toast.makeText(UpdatePasswordActivity.this, msg, Toast.LENGTH_LONG).show();
        btnSave.setClickable(true);
    }

    private void logout(){
        ed.clear();
        ed.commit();
        auth.signOut();
        Intent i = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        stopService(new Intent(this, BackgroundService.class));
    }
}
