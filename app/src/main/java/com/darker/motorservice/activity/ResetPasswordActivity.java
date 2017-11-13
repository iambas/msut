package com.darker.motorservice.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.utils.NetWork;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        getSupportActionBar().setTitle("ลืมรหัสผ่าน");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnReset = (Button) findViewById(R.id.btn_reset_password);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void onBtnResetPasswordClicked(View view){
        btnReset.setClickable(false);
        final String email =  ((EditText) findViewById(R.id.email)).getText().toString();
        if (email.isEmpty()) {
            alert("กรุณากรอกอีเมล!");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            alert("กรุณากรอกอีเมลให้ถูกต้อง!");
            return;
        }
        if (!(new NetWork(this).isNetworkAvailiable())) {
            alert("ข้อผิดพลาดเครือข่าย! กรุณาตรวจสอบและลองใหม่อีกครั้ง");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("ระบบจะส่ง link ไปที่อีเมลนี้");
        builder.setMessage(email);
        builder.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "เรียบร้อยแล้ว โปรดตรวจสอบที่อีเมลเพื่อเปลี่ยนรหัสผ่าน", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "ไม่สามารถทำรายการได้ โปรดลองอีกครั้งในภายหลัง", Toast.LENGTH_SHORT).show();
                                    btnReset.setClickable(true);
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void alert(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        btnReset.setClickable(true);
    }
}
