package com.darker.motorservice.ui.update_image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.darker.motorservice.R;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.utility.LoadServiceUtil;
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.darker.motorservice.utility.Constant.COVER;
import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.IMG;

public class UpdateImageActivity extends AppCompatActivity {
    private String id, image;
    private ImageView imageView;
    private Bitmap bitmap;
    private Button btnSel, btnSave;
    private boolean isCover;
    private int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_image);

        Intent intent = getIntent();
        id = intent.getStringExtra(ID);
        image = intent.getStringExtra(IMG);
        isCover = intent.getBooleanExtra(COVER, true);

        btnSel = (Button) findViewById(R.id.btn_select);
        btnSave = (Button) findViewById(R.id.btn_save);
        int rid;
        if (isCover){
            size = 800;
            rid = R.string.update_cover;
            imgCover();
        }
        else{
            size = 300;
            rid = R.string.update_profile;
            imgProfile();
        }

        getSupportActionBar().setTitle(getResources().getString(rid));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void imgCover() {
        Bitmap cover = ImageUtil.getImgCover(this, id);
        imageView = (ImageView) findViewById(R.id.img_cover);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(cover);
    }

    private void imgProfile() {
        Bitmap profile = ImageUtil.getImgProfile(this, id);
        imageView = (ImageView) findViewById(R.id.img_profile);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(profile);
    }

    public void onSelImgClicked(View view) {
        setBtn(false);
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
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                    if (bitmap.getWidth() > size)
                        bitmap = ImageUtil.scaleBitmap(bitmap, size);
                    else
                        bitmap = ImageUtil.scaleBitmap(bitmap, bitmap.getWidth());
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        setBtn(true);
    }

    public void onSaveClicked(View view){
        if (bitmap == null) return;

        setBtn(false);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        StorageReference mountainsRef = FirebaseStorage.getInstance().getReference().child(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("upload", exception.getMessage());
                progressBar.setVisibility(View.GONE);
                setBtn(true);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("upload", "OK");
                new LoadServiceUtil(UpdateImageActivity.this).loadData();
                updateDB();
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    private void updateDB(){
        ServiceDatabase handle = new ServiceDatabase(this);
        ServicesItem s = handle.getService(id);
        if (isCover) {
            s.setImgCover(ImageUtil.convertBitmapToByte(bitmap));
        }else{
            s.setImgProfile(ImageUtil.convertBitmapToByte(bitmap));
        }
        handle.updateService(s);
    }

    private void setBtn(boolean b){
        btnSel.setClickable(b);
        btnSave.setClickable(b);
    }
}
