package com.darker.motorservice.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.utils.MyImage;
import com.darker.motorservice.utils.NetWork;
import com.darker.motorservice.data.Pictures;
import com.darker.motorservice.data.Timeline;
import com.darker.motorservice.database.PictureHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.darker.motorservice.data.Constant.DATE;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.IMG;
import static com.darker.motorservice.data.Constant.KEY;
import static com.darker.motorservice.data.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.data.Constant.MESSAGE;
import static com.darker.motorservice.data.Constant.TIMELINE;

public class PostActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private TextView btnPost, btnImg;
    private ProgressBar progressBar;
    private EditText editMsg;
    private Bitmap bitmap;
    private ImageView imageView;
    private String uid, key, id, message, date, imgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        getSupportActionBar().setTitle(getResources().getString(R.string.post));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        key = intent.getStringExtra(KEY);
        id = intent.getStringExtra(ID);
        message = intent.getStringExtra(MESSAGE);
        date = intent.getStringExtra(DATE);
        imgName = intent.getStringExtra(IMG);

        SharedPreferences sh = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        uid = sh.getString(ID, "");
        dbRef = FirebaseDatabase.getInstance().getReference();
        btnPost = (TextView) findViewById(R.id.btn_post);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editMsg = (EditText) findViewById(R.id.message);
        imageView = (ImageView) findViewById(R.id.image);
        btnImg = (TextView) findViewById(R.id.btn_img);
        setDate();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setDate(){
        if (!key.isEmpty())
            btnPost.setText("บันทึก");
        editMsg.setText(message);

        if (!imgName.isEmpty()){
            PictureHandle handle = new PictureHandle(this);
            Bitmap bitmap = new MyImage().convertToBitmap(handle.getPicture(imgName).getPicture());
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public void onPostClicked(View view) {
        String msg = editMsg.getText().toString();
        if (msg.isEmpty() && bitmap == null) {
            return;
        }
        if (msg.isEmpty()) msg = "";

        btnPost.setClickable(false);
        btnImg.setClickable(false);
        if (!(new NetWork(this).isNetworkAvailiable())) {
            alert("เครือข่ายมีปัญหา! ไม่สามารถโพสต์ได้");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Timeline timeline = new Timeline();
        if (!key.isEmpty()){
            timeline.setId(id);
            timeline.setImgName(imgName);
            timeline.setMessage(msg);
            timeline.setDate(date);
            if (bitmap != null){
                if (imgName.isEmpty()) {
                    String d = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
                    String imgNewName = TIMELINE + "/" + uid.substring(0, 5) + "_" + d;
                    timeline.setImgName(imgNewName);
                    imgName = imgNewName;
                }
                uploadImage(timeline, imgName, true);
            }else {
                dbRef.child(TIMELINE).child(key).setValue(timeline);
                finish();
            }
            return;
        }

        String newDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        timeline.setDate(newDate);
        timeline.setMessage(msg);
        timeline.setId(uid);
        if (bitmap != null){
            String d = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
            String imgNewName = TIMELINE + "/" + uid.substring(0,5) + "_" + d;
            timeline.setImgName(imgNewName);
            uploadImage(timeline, imgNewName, false);
        }else{
            timeline.setImgName("");
            dbRef.child(TIMELINE).push().setValue(timeline);
            progressBar.setVisibility(View.GONE);
            finish();
        }
    }

    private void uploadImage(final Timeline timeline, final String image, final boolean edit){
        StorageReference mountainsRef = FirebaseStorage.getInstance().getReference().child(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        final byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("upload", exception.getMessage());
                progressBar.setVisibility(View.GONE);
                alert("การอัพโหลดรูปภาพมีปัญหา! โปรดลองอีกครั้ง");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("upload", "OK");
                PictureHandle handle = new PictureHandle(PostActivity.this);
                if (edit) {
                    if (handle.hasPicture(image)){
                        handle.updatePicture(new Pictures(image, data));
                    }else {
                        handle.addPicture(new Pictures(image, data));
                    }
                    dbRef.child(TIMELINE).child(key).setValue(timeline);
                }else {
                    handle.addPicture(new Pictures(image, data));
                    dbRef.child(TIMELINE).push().setValue(timeline);
                }
                alert("โพสต์เรียบร้อย");
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    public void onImageClicked(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    if (bitmap.getWidth() > 800)
                        bitmap = new MyImage().scaleBitmap(bitmap, 800);
                    else
                        bitmap = new MyImage().scaleBitmap(bitmap, bitmap.getWidth());
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void alert(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        btnPost.setClickable(true);
        btnImg.setClickable(true);
    }
}
