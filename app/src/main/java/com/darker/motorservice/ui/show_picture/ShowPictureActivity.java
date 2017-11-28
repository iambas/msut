package com.darker.motorservice.ui.show_picture;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.darker.motorservice.R;
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.database.PictureDatabse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import static com.darker.motorservice.utility.Constant.KEY_IMAGE;

public class ShowPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        String name = getIntent().getStringExtra(KEY_IMAGE);
        PictureDatabse handle = new PictureDatabse(this);
        final ImageView imageView = (ImageView) findViewById(R.id.img);
        try {
            byte[] bytes = handle.getPicture(name).getPicture();
            Bitmap bitmap = new ImageUtil().convertToBitmap(bytes);
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
