package com.darker.motorservice.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.darker.motorservice.R;

import static com.darker.motorservice.data.Constant.TYPE;

public class AboutUseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getStringExtra(TYPE).equals("about")){
            setContentView(R.layout.activity_about_us);
            getSupportActionBar().setTitle(getResources().getString(R.string.about_us));
        }else{
            setContentView(R.layout.activity_how_to_use);
            getSupportActionBar().setTitle(getResources().getString(R.string.use));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
