package com.darker.motorservice.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.service.BackgroundService;
import com.darker.motorservice.ui.main.fragment.AddNewServiceFragment;
import com.darker.motorservice.ui.main.fragment.ChatFragment;
import com.darker.motorservice.ui.main.fragment.MotorcycleFragment;
import com.darker.motorservice.ui.main.fragment.ProfileFragment;
import com.darker.motorservice.ui.main.fragment.ReviewFragment;
import com.darker.motorservice.ui.main.fragment.StatisticsFragment;
import com.darker.motorservice.ui.main.fragment.TimelineFragment;

import static com.darker.motorservice.utils.Constant.ALERT;
import static com.darker.motorservice.utils.Constant.CHAT;
import static com.darker.motorservice.utils.Constant.ID;
import static com.darker.motorservice.utils.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.USER;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private SharedPreferences loginSharedPref;
    private SharedPreferences.Editor chatEditor;
    private View fisrtView, secondView, thirdView, fouthView, fifthView, sixthView;
    private ImageView firstImageView, secondImageView, thirdImageView, fourthImageView, fifthImageView, sixthImageView;
    private TextView fisrtTextView, secondTextView, thirdTextView, fourthTextView, fifthTextView, sixthTExtView;
    private boolean isAdmin = false;
    private ProgressBar progressBar;
    private RelativeLayout loadLayout;
    private ViewPager containerViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBackgroundService();
        initSharedPreferences();
        bindView();
        checkAdmin();
        loadView();
    }

    private void loadView() {
        if (new ServiceDatabase(this).getServiceCount() != 0) {
            setUpByStatus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            handlerSetup();
        }
    }

    private void handlerSetup() {
        int delayMillis = 4000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                setUpByStatus();
            }
        }, delayMillis);
    }

    private void bindView() {
        loadLayout = (RelativeLayout) findViewById(R.id.load);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        containerViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        loadLayout.setVisibility(View.VISIBLE);
    }

    private void initSharedPreferences() {
        loginSharedPref = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        chatEditor = getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        chatEditor.putBoolean(ALERT, false);
        chatEditor.apply();
    }

    private void startBackgroundService() {
        startService(new Intent(this, BackgroundService.class));
    }

    private void checkAdmin() {
        AdminDatabase admin = new AdminDatabase(this);
        String id = loginSharedPref.getString(ID, "");
        isAdmin = admin.isAdmin(id);
    }

    private void setUpByStatus() {
        String status = loginSharedPref.getString(STATUS, "");
        if (status.equals(USER)) {
            setTabUser();
        } else {
            setTabServices();
        }
    }

    private void setTabServices() {
        setupViewPagerService(containerViewPager);
        tabLayout.setupWithViewPager(containerViewPager);
        tabService();
        tabLayout.addOnTabSelectedListener(onTabServicesSelectedListener());
    }

    private void setTabUser() {
        setupViewPagerUser(containerViewPager);
        tabLayout.setupWithViewPager(containerViewPager);
        tabUser();
        tabLayout.addOnTabSelectedListener(onTabUserListener());
    }

    private void tabService() {
        fisrtView = getInflate();
        firstImageView = (ImageView) fisrtView.findViewById(R.id.tab_icon);
        fisrtTextView = (TextView) fisrtView.findViewById(R.id.tab_text);
        fisrtTextView.setText(R.string.tab_chat);

        secondView = getInflate();
        secondImageView = (ImageView) secondView.findViewById(R.id.tab_icon);
        secondTextView = (TextView) secondView.findViewById(R.id.tab_text);
        secondTextView.setText(R.string.tab_timeline);

        getStatisticTab();

        getReviewTab();

        fifthView = getInflate();
        fifthImageView = (ImageView) fifthView.findViewById(R.id.tab_icon);
        fifthTextView = (TextView) fifthView.findViewById(R.id.tab_text);
        fifthTextView.setText(R.string.tab_profile);

        if (isAdmin) {
            sixthView = getInflate();
            sixthImageView = (ImageView) sixthView.findViewById(R.id.tab_icon);
            sixthTExtView = (TextView) sixthView.findViewById(R.id.tab_text);
            sixthTExtView.setText(R.string.tab_add_store);
        }

        setupTabIconsService(0);
    }

    @SuppressLint("InflateParams")
    private View getInflate(){
        return LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    }

    private void getStatisticTab() {
        thirdView = getInflate();
        thirdImageView = (ImageView) thirdView.findViewById(R.id.tab_icon);
        thirdTextView = (TextView) thirdView.findViewById(R.id.tab_text);
        thirdTextView.setText(R.string.tab_stats);
    }

    private void setupTabIconsService(int tab) {
        if (tab == 0) {
            firstImageView.setImageResource(R.drawable.ic_chat_dark);
            fisrtTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            firstImageView.setImageResource(R.drawable.ic_chat_white);
            fisrtTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 1) {
            secondImageView.setImageResource(R.drawable.ic_timeline_dark);
            secondTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            secondImageView.setImageResource(R.drawable.ic_timeline_white);
            secondTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 2) {
            thirdImageView.setImageResource(R.drawable.ic_equalizer_dark);
            thirdTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            thirdImageView.setImageResource(R.drawable.ic_equalizer_white);
            thirdTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 3) {
            fourthImageView.setImageResource(R.drawable.ic_star_dark);
            fourthTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            fourthImageView.setImageResource(R.drawable.ic_star_white);
            fourthTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 4) {
            fifthImageView.setImageResource(R.drawable.ic_person_dark);
            fifthTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            fifthImageView.setImageResource(R.drawable.ic_person_white);
            fifthTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (isAdmin) {
            if (tab == 5) {
                sixthImageView.setImageResource(R.drawable.ic_add_circle_dark);
                sixthTExtView.setTextColor(getResources().getColor(R.color.teal));
            } else {
                sixthImageView.setImageResource(R.drawable.ic_add_circle_white);
                sixthTExtView.setTextColor(getResources().getColor(R.color.iconTab));
            }
            tabLayout.getTabAt(5).setCustomView(sixthView);
        }

        tabLayout.getTabAt(0).setCustomView(fisrtView);
        tabLayout.getTabAt(1).setCustomView(secondView);
        tabLayout.getTabAt(2).setCustomView(thirdView);
        tabLayout.getTabAt(3).setCustomView(fouthView);
        tabLayout.getTabAt(4).setCustomView(fifthView);
    }

    private void setupViewPagerService(ViewPager viewPager) {
        String tabChat = getStringId(R.string.tab_chat);
        String tabTimeLine = getStringId(R.string.tab_timeline);
        String tabStats = getStringId(R.string.tab_stats);
        String tabReview = getStringId(R.string.tab_review);
        String tabProfile = getStringId(R.string.tab_profile);
        String tabAddStore = getStringId(R.string.tab_add_store);

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ChatFragment(), tabChat);
        adapter.addFrag(new TimelineFragment(), tabTimeLine);
        adapter.addFrag(new StatisticsFragment(), tabStats);
        adapter.addFrag(new ReviewFragment(), tabReview);
        adapter.addFrag(new ProfileFragment(), tabProfile);
        if (isAdmin) {
            adapter.addFrag(new AddNewServiceFragment(), tabAddStore);
        }
        viewPager.setAdapter(adapter);
    }

    private String getStringId(int id){
        return getResources().getString(id);
    }

    private void tabUser() {
        fisrtView = getInflate();
        firstImageView = (ImageView) fisrtView.findViewById(R.id.tab_icon);
        fisrtTextView = (TextView) fisrtView.findViewById(R.id.tab_text);
        fisrtTextView.setText(R.string.tab_store);

        secondView = getInflate();
        secondImageView = (ImageView) secondView.findViewById(R.id.tab_icon);
        secondTextView = (TextView) secondView.findViewById(R.id.tab_text);
        secondTextView.setText(R.string.tab_chat);

        thirdView = getInflate();
        thirdImageView = (ImageView) thirdView.findViewById(R.id.tab_icon);
        thirdTextView = (TextView) thirdView.findViewById(R.id.tab_text);
        thirdTextView.setText(R.string.tab_timeline);

        getReviewTab();

        fifthView = getInflate();
        fifthImageView = (ImageView) fifthView.findViewById(R.id.tab_icon);
        fifthTextView = (TextView) fifthView.findViewById(R.id.tab_text);
        fifthTextView.setText(R.string.tab_profile);

        setupTabIconsUser(0);
    }

    private void getReviewTab() {
        fouthView = getInflate();
        fourthImageView = (ImageView) fouthView.findViewById(R.id.tab_icon);
        fourthTextView = (TextView) fouthView.findViewById(R.id.tab_text);
        fourthTextView.setText(R.string.tab_review);
    }

    private void setupTabIconsUser(int tab) {
        if (tab == 0) {
            firstImageView.setImageResource(R.drawable.ic_motorcycle_dark);
            fisrtTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            firstImageView.setImageResource(R.drawable.ic_motorcycle_white);
            fisrtTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 1) {
            secondImageView.setImageResource(R.drawable.ic_chat_dark);
            secondTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            secondImageView.setImageResource(R.drawable.ic_chat_white);
            secondTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 2) {
            thirdImageView.setImageResource(R.drawable.ic_timeline_dark);
            thirdTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            thirdImageView.setImageResource(R.drawable.ic_timeline_white);
            thirdTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 3) {
            fourthImageView.setImageResource(R.drawable.ic_star_dark);
            fourthTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            fourthImageView.setImageResource(R.drawable.ic_star_white);
            fourthTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 4) {
            fifthImageView.setImageResource(R.drawable.ic_person_dark);
            fifthTextView.setTextColor(getResources().getColor(R.color.teal));
        } else {
            fifthImageView.setImageResource(R.drawable.ic_person_white);
            fifthTextView.setTextColor(getResources().getColor(R.color.iconTab));
        }

        tabLayout.getTabAt(0).setCustomView(fisrtView);
        tabLayout.getTabAt(1).setCustomView(secondView);
        tabLayout.getTabAt(2).setCustomView(thirdView);
        tabLayout.getTabAt(3).setCustomView(fouthView);
        tabLayout.getTabAt(4).setCustomView(fifthView);
    }

    private void setupViewPagerUser(ViewPager viewPager) {
        String tabChat = getStringId(R.string.tab_chat);
        String tabTimeLine = getStringId(R.string.tab_timeline);
        String tabReview = getStringId(R.string.tab_review);
        String tabProfile = getStringId(R.string.tab_profile);
        String tabStore = getStringId(R.string.tab_store);

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MotorcycleFragment(), tabStore);
        adapter.addFrag(new ChatFragment(), tabChat);
        adapter.addFrag(new TimelineFragment(), tabTimeLine);
        adapter.addFrag(new ReviewFragment(), tabReview);
        adapter.addFrag(new ProfileFragment(), tabProfile);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        chatEditor.putBoolean(ALERT, false);
        chatEditor.commit();
        super.onStart();
        Log.d("Main check", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        chatEditor.putBoolean(ALERT, false);
        chatEditor.commit();
        Log.d("Main check", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        chatEditor.putBoolean(ALERT, true);
        chatEditor.commit();
        Log.d("Main check", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        chatEditor.putBoolean(ALERT, true);
        chatEditor.commit();
        Log.d("Main check", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatEditor.putBoolean(ALERT, true);
        chatEditor.commit();
        Log.d("Main check", "onDestroy");
    }

    @NonNull
    private TabLayout.OnTabSelectedListener onTabUserListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setupTabIconsUser(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
    }

    @NonNull
    private TabLayout.OnTabSelectedListener onTabServicesSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setupTabIconsService(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
    }
}
