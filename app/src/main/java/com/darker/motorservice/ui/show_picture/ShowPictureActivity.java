package com.darker.motorservice.ui.show_picture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.darker.motorservice.R;
import com.darker.motorservice.utility.ImageUtil;

import static com.darker.motorservice.utility.ImageUtil.KEY_IMAGE;

public class ShowPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        String pathImage = getIntent().getStringExtra(ImageUtil.KEY_IMAGE);
        final ImageView imageView = (ImageView) findViewById(R.id.img);
        ImageUtil.setImageViewFromStorage(this, imageView, pathImage);
    }
}
