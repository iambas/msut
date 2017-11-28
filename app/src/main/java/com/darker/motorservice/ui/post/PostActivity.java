package com.darker.motorservice.ui.post;

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
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.ui.main.model.PictureItem;
import com.darker.motorservice.model.TimelineItem;
import com.darker.motorservice.database.PictureDatabase;
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

import static com.darker.motorservice.utility.Constant.DATE;
import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.KEY;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.MESSAGE;
import static com.darker.motorservice.utility.Constant.TIMELINE;

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
            PictureDatabase handle = new PictureDatabase(this);
            Bitmap bitmap = ImageUtil.convertByteToBitmap(handle.getPicture(imgName).getPicture());
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
        if (!NetworkUtil.isNetworkAvailable(this)) {
            alert("เครือข่ายมีปัญหา! ไม่สามารถโพสต์ได้");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        TimelineItem timelineItem = new TimelineItem();
        if (!key.isEmpty()){
            timelineItem.setId(id);
            timelineItem.setImgName(imgName);
            timelineItem.setMessage(msg);
            timelineItem.setDate(date);
            if (bitmap != null){
                if (imgName.isEmpty()) {
                    String d = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
                    String imgNewName = TIMELINE + "/" + uid.substring(0, 5) + "_" + d;
                    timelineItem.setImgName(imgNewName);
                    imgName = imgNewName;
                }
                uploadImage(timelineItem, imgName, true);
            }else {
                dbRef.child(TIMELINE).child(key).setValue(timelineItem);
                finish();
            }
            return;
        }

        String newDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        timelineItem.setDate(newDate);
        timelineItem.setMessage(msg);
        timelineItem.setId(uid);
        if (bitmap != null){
            String d = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
            String imgNewName = TIMELINE + "/" + uid.substring(0,5) + "_" + d;
            timelineItem.setImgName(imgNewName);
            uploadImage(timelineItem, imgNewName, false);
        }else{
            timelineItem.setImgName("");
            dbRef.child(TIMELINE).push().setValue(timelineItem);
            progressBar.setVisibility(View.GONE);
            finish();
        }
    }

    private void uploadImage(final TimelineItem timelineItem, final String image, final boolean edit){
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
                PictureDatabase handle = new PictureDatabase(PostActivity.this);
                if (edit) {
                    if (handle.hasPicture(image)){
                        handle.updatePicture(new PictureItem(image, data));
                    }else {
                        handle.addPicture(new PictureItem(image, data));
                    }
                    dbRef.child(TIMELINE).child(key).setValue(timelineItem);
                }else {
                    handle.addPicture(new PictureItem(image, data));
                    dbRef.child(TIMELINE).push().setValue(timelineItem);
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
                        bitmap = ImageUtil.scaleBitmap(bitmap, 800);
                    else
                        bitmap = ImageUtil.scaleBitmap(bitmap, bitmap.getWidth());
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
