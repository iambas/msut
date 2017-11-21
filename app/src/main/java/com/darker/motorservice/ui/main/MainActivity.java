package com.darker.motorservice.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.ui.main.fragment.AddNewServiceFragment;
import com.darker.motorservice.ui.main.fragment.ChatFragment;
import com.darker.motorservice.ui.main.fragment.MotorcycleFragment;
import com.darker.motorservice.ui.main.fragment.ProfileFragment;
import com.darker.motorservice.ui.main.fragment.ReviewFragment;
import com.darker.motorservice.ui.main.fragment.StatisticsFragment;
import com.darker.motorservice.ui.main.fragment.TimelineFragment;
import com.darker.motorservice.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBackgroundService();
        initSharedPreferences();

        findViewById(R.id.load).setVisibility(View.VISIBLE);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        checkAdmin();
        if (new ServiceDatabase(this).getServiceCount() != 0) {
            setup();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.load).setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    setup();
                }
            }, 4000);
        }
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

    private void setup() {
        String status = loginSharedPref.getString(STATUS, "");
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        if (status.equals(USER)) {
            setupViewPagerUser(viewPager);
            tabLayout.setupWithViewPager(viewPager);
            tabUser();
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    setupTabIconsUser(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        } else {
            setupViewPagerService(viewPager);
            tabLayout.setupWithViewPager(viewPager);
            tabService();
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
            });
        }
    }

    private void tabService() {
        fisrtView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        firstImageView = (ImageView) fisrtView.findViewById(R.id.tab_icon);
        fisrtTextView = (TextView) fisrtView.findViewById(R.id.tab_text);
        fisrtTextView.setText("แชท");

        secondView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        secondImageView = (ImageView) secondView.findViewById(R.id.tab_icon);
        secondTextView = (TextView) secondView.findViewById(R.id.tab_text);
        secondTextView.setText("ไทม์ไลน์");

        thirdView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        thirdImageView = (ImageView) thirdView.findViewById(R.id.tab_icon);
        thirdTextView = (TextView) thirdView.findViewById(R.id.tab_text);
        thirdTextView.setText("สถิติ");

        fouthView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        fourthImageView = (ImageView) fouthView.findViewById(R.id.tab_icon);
        fourthTextView = (TextView) fouthView.findViewById(R.id.tab_text);
        fourthTextView.setText("รีวิว");

        fifthView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        fifthImageView = (ImageView) fifthView.findViewById(R.id.tab_icon);
        fifthTextView = (TextView) fifthView.findViewById(R.id.tab_text);
        fifthTextView.setText("โปรไฟล์");

        if (isAdmin) {
            sixthView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            sixthImageView = (ImageView) sixthView.findViewById(R.id.tab_icon);
            sixthTExtView = (TextView) sixthView.findViewById(R.id.tab_text);
            sixthTExtView.setText("เพิ่มร้าน");
        }

        setupTabIconsService(0);
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
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ChatFragment(), "แชท");
        adapter.addFrag(new TimelineFragment(), "ไทม์ไลน์");
        adapter.addFrag(new StatisticsFragment(), "สถิติ");
        adapter.addFrag(new ReviewFragment(), "รีวิว");
        adapter.addFrag(new ProfileFragment(), "โปรไฟล์");
        if (isAdmin) {
            adapter.addFrag(new AddNewServiceFragment(), "เพิ่มร้าน");
        }
        viewPager.setAdapter(adapter);
    }

    private void tabUser() {
        fisrtView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        firstImageView = (ImageView) fisrtView.findViewById(R.id.tab_icon);
        fisrtTextView = (TextView) fisrtView.findViewById(R.id.tab_text);
        fisrtTextView.setText("ร้านต่างๆ");

        secondView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        secondImageView = (ImageView) secondView.findViewById(R.id.tab_icon);
        secondTextView = (TextView) secondView.findViewById(R.id.tab_text);
        secondTextView.setText("แชท");

        thirdView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        thirdImageView = (ImageView) thirdView.findViewById(R.id.tab_icon);
        thirdTextView = (TextView) thirdView.findViewById(R.id.tab_text);
        thirdTextView.setText("ไทม์ไลน์");

        fouthView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        fourthImageView = (ImageView) fouthView.findViewById(R.id.tab_icon);
        fourthTextView = (TextView) fouthView.findViewById(R.id.tab_text);
        fourthTextView.setText("รีวิว");

        fifthView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        fifthImageView = (ImageView) fifthView.findViewById(R.id.tab_icon);
        fifthTextView = (TextView) fifthView.findViewById(R.id.tab_text);
        fifthTextView.setText("โปรไฟล์");

        setupTabIconsUser(0);
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
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MotorcycleFragment(), "ร้านต่างๆ");
        adapter.addFrag(new ChatFragment(), "แชท");
        adapter.addFrag(new TimelineFragment(), "ไทม์ไลน์");
        adapter.addFrag(new ReviewFragment(), "รีวิวร้าน");
        adapter.addFrag(new ProfileFragment(), "โปรไฟล์");
        viewPager.setAdapter(adapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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
}
