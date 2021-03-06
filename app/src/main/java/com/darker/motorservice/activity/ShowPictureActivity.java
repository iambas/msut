package com.darker.motorservice.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.darker.motorservice.R;
import com.darker.motorservice.utils.MyImage;
import com.darker.motorservice.database.PictureHandle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import static com.darker.motorservice.data.Constant.KEY_IMAGE;

public class ShowPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        String name = getIntent().getStringExtra(KEY_IMAGE);
        PictureHandle handle = new PictureHandle(this);
        final ImageView imageView = (ImageView) findViewById(R.id.img);
        try {
            byte[] bytes = handle.getPicture(name).getPicture();
            Bitmap bitmap = new MyImage().convertToBitmap(bytes);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d("excep showAct", e.getMessage());

            StorageReference islandRef = FirebaseStorage.getInstance().getReference().child(name);
            islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(ShowPictureActivity.this).load(uri).into(imageView);
                }
            });
        }
    }
}
