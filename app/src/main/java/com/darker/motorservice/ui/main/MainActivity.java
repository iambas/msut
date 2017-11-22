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
import com.darker.motorservice.ui.main.model.ViewItem;

import java.util.ArrayList;
import java.util.List;

import static com.darker.motorservice.utils.Constant.ALERT;
import static com.darker.motorservice.utils.Constant.CHAT;
import static com.darker.motorservice.utils.Constant.ID;
import static com.darker.motorservice.utils.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.USER;

public class MainActivity extends AppCompatActivity {
    private View fisrtView, secondView, thirdView, fouthView, fifthView, sixthView;
    private ImageView firstImageView, secondImageView, thirdImageView, fourthImageView, fifthImageView, sixthImageView;
    private TextView fisrtTextView, secondTextView, thirdTextView, fourthTextView, fifthTextView, sixthTExtView;

    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private RelativeLayout loadLayout;
    private ViewPager containerViewPager;

    private SharedPreferences loginSharedPref;
    private SharedPreferences.Editor chatEditor;
    private boolean isAdmin = false;
    private List<ViewItem> viewItemList = new ArrayList<>();

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

        addViewItem();
        setupForServicesIcon();
        setupServicesAdmin();
        setCustomView();
    }

    @SuppressLint("InflateParams")
    private View getInflate() {
        return LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    }

    private void getStatisticTab() {
        thirdView = getInflate();
        thirdImageView = (ImageView) thirdView.findViewById(R.id.tab_icon);
        thirdTextView = (TextView) thirdView.findViewById(R.id.tab_text);
        thirdTextView.setText(R.string.tab_stats);
    }

    private void addViewItem() {
        addViewItemToList(firstImageView, fisrtTextView);
        addViewItemToList(secondImageView, secondTextView);
        addViewItemToList(thirdImageView, thirdTextView);
        addViewItemToList(fourthImageView, fourthTextView);
        addViewItemToList(fifthImageView, fifthTextView);
        if (isAdmin)
            addViewItemToList(sixthImageView, sixthTExtView);
    }

    private void setupForServicesIcon() {
        firstImageView.setImageResource(R.drawable.ic_chat_white);
        secondImageView.setImageResource(R.drawable.ic_timeline_white);
        thirdImageView.setImageResource(R.drawable.ic_equalizer_white);
        fourthImageView.setImageResource(R.drawable.ic_star_white);
        fifthImageView.setImageResource(R.drawable.ic_person_white);
    }

    private void setupServicesAdmin() {
        if (isAdmin) {
            sixthImageView.setImageResource(R.drawable.ic_add_circle_white);
            sixthTExtView.setTextColor(getResources().getColor(R.color.iconTab));
            tabLayout.getTabAt(5).setCustomView(sixthView);
        }
    }

    private void addViewItemToList(ImageView imageView, TextView textView) {
        ViewItem viewItem = new ViewItem(imageView, textView);
        viewItemList.add(viewItem);
    }

    private void setupViewPagerService(ViewPager viewPager) {
        String tabChat = getString(R.string.tab_chat);
        String tabTimeLine = getString(R.string.tab_timeline);
        String tabStats = getString(R.string.tab_stats);
        String tabReview = getString(R.string.tab_review);
        String tabProfile = getString(R.string.tab_profile);
        String tabAddStore = getString(R.string.tab_add_store);

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

        addViewItem();
        setupForUserIcon();
        setCustomView();
    }

    private void getReviewTab() {
        fouthView = getInflate();
        fourthImageView = (ImageView) fouthView.findViewById(R.id.tab_icon);
        fourthTextView = (TextView) fouthView.findViewById(R.id.tab_text);
        fourthTextView.setText(R.string.tab_review);
    }

    private void setupForUserIcon() {
        firstImageView.setImageResource(R.drawable.ic_motorcycle_white);
        secondImageView.setImageResource(R.drawable.ic_chat_white);
        thirdImageView.setImageResource(R.drawable.ic_timeline_white);
        fourthImageView.setImageResource(R.drawable.ic_star_white);
        fifthImageView.setImageResource(R.drawable.ic_person_white);
    }

    private void setCustomView() {
        tabLayout.getTabAt(0).setCustomView(fisrtView);
        tabLayout.getTabAt(1).setCustomView(secondView);
        tabLayout.getTabAt(2).setCustomView(thirdView);
        tabLayout.getTabAt(3).setCustomView(fouthView);
        tabLayout.getTabAt(4).setCustomView(fifthView);
    }

    private void setupViewPagerUser(ViewPager viewPager) {
        String tabChat = getString(R.string.tab_chat);
        String tabTimeLine = getString(R.string.tab_timeline);
        String tabReview = getString(R.string.tab_review);
        String tabProfile = getString(R.string.tab_profile);
        String tabStore = getString(R.string.tab_store);

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

    private void setTanSelected(TabLayout.Tab tab) {
        int tealColor = getResources().getColor(R.color.teal);
        ViewItem viewItem = viewItemList.get(tab.getPosition());
        viewItem.getImageView().setColorFilter(tealColor);
        viewItem.getTextView().setTextColor(tealColor);
    }

    private void setTabUnSelected(TabLayout.Tab tab) {
        int tabColor = getResources().getColor(R.color.iconTab);
        ViewItem viewItem = viewItemList.get(tab.getPosition());
        viewItem.getImageView().setColorFilter(tabColor);
        viewItem.getTextView().setTextColor(tabColor);
    }

    @NonNull
    private TabLayout.OnTabSelectedListener onTabUserListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTanSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabUnSelected(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
    }

    @NonNull
    private TabLayout.OnTabSelectedListener onTabServicesSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTanSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabUnSelected(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
    }
}
