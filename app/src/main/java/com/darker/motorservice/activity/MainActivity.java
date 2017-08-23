package com.darker.motorservice.activity;

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
import com.darker.motorservice.database.AdminHandle;
import com.darker.motorservice.database.ServiceHandle;
import com.darker.motorservice.fragment.AddNewServiceFragment;
import com.darker.motorservice.fragment.ChatFragment;
import com.darker.motorservice.fragment.MotorcycleFragment;
import com.darker.motorservice.fragment.ProfileFragment;
import com.darker.motorservice.fragment.ReviewFragment;
import com.darker.motorservice.fragment.StatisticsFragment;
import com.darker.motorservice.fragment.TimelineFragment;
import com.darker.motorservice.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;

import static com.darker.motorservice.data.Constant.ALERT;
import static com.darker.motorservice.data.Constant.CHAT;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.USER;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    SharedPreferences shLogin;
    private SharedPreferences.Editor edChat;
    private View v1, v2, v3, v4, v5, v6;
    private ImageView i1, i2, i3, i4, i5, i6;
    private TextView t1, t2, t3, t4, t5, t6;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start service
        startService(new Intent(this, BackgroundService.class));

        shLogin = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        edChat = getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.commit();

        findViewById(R.id.load).setVisibility(View.VISIBLE);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        checkAdmin();
        if (new ServiceHandle(this).getServiceCount() != 0) {
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

    private void checkAdmin() {
        AdminHandle admin = new AdminHandle(this);
        isAdmin = admin.isAdmin(shLogin.getString(ID, ""));
    }

    private void setup() {
        String status = shLogin.getString(STATUS, "");
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
        v1 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i1 = (ImageView) v1.findViewById(R.id.tab_icon);
        t1 = (TextView) v1.findViewById(R.id.tab_text);
        t1.setText("แชท");

        v2 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i2 = (ImageView) v2.findViewById(R.id.tab_icon);
        t2 = (TextView) v2.findViewById(R.id.tab_text);
        t2.setText("ไทม์ไลน์");

        v3 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i3 = (ImageView) v3.findViewById(R.id.tab_icon);
        t3 = (TextView) v3.findViewById(R.id.tab_text);
        t3.setText("สถิติ");

        v4 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i4 = (ImageView) v4.findViewById(R.id.tab_icon);
        t4 = (TextView) v4.findViewById(R.id.tab_text);
        t4.setText("รีวิว");

        v5 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i5 = (ImageView) v5.findViewById(R.id.tab_icon);
        t5 = (TextView) v5.findViewById(R.id.tab_text);
        t5.setText("โปรไฟล์");

        if (isAdmin) {
            v6 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            i6 = (ImageView) v6.findViewById(R.id.tab_icon);
            t6 = (TextView) v6.findViewById(R.id.tab_text);
            t6.setText("เพิ่มร้าน");
        }

        setupTabIconsService(0);
    }

    private void setupTabIconsService(int tab) {
        if (tab == 0) {
            i1.setImageResource(R.drawable.ic_chat_dark);
            t1.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i1.setImageResource(R.drawable.ic_chat_white);
            t1.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 1) {
            i2.setImageResource(R.drawable.ic_timeline_dark);
            t2.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i2.setImageResource(R.drawable.ic_timeline_white);
            t2.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 2) {
            i3.setImageResource(R.drawable.ic_equalizer_dark);
            t3.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i3.setImageResource(R.drawable.ic_equalizer_white);
            t3.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 3) {
            i4.setImageResource(R.drawable.ic_star_dark);
            t4.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i4.setImageResource(R.drawable.ic_star_white);
            t4.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 4) {
            i5.setImageResource(R.drawable.ic_person_dark);
            t5.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i5.setImageResource(R.drawable.ic_person_white);
            t5.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (isAdmin) {
            if (tab == 5) {
                i6.setImageResource(R.drawable.ic_add_circle_dark);
                t6.setTextColor(getResources().getColor(R.color.teal));
            } else {
                i6.setImageResource(R.drawable.ic_add_circle_white);
                t6.setTextColor(getResources().getColor(R.color.iconTab));
            }
            tabLayout.getTabAt(5).setCustomView(v6);
        }

        tabLayout.getTabAt(0).setCustomView(v1);
        tabLayout.getTabAt(1).setCustomView(v2);
        tabLayout.getTabAt(2).setCustomView(v3);
        tabLayout.getTabAt(3).setCustomView(v4);
        tabLayout.getTabAt(4).setCustomView(v5);
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
        v1 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i1 = (ImageView) v1.findViewById(R.id.tab_icon);
        t1 = (TextView) v1.findViewById(R.id.tab_text);
        t1.setText("ร้านต่างๆ");

        v2 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i2 = (ImageView) v2.findViewById(R.id.tab_icon);
        t2 = (TextView) v2.findViewById(R.id.tab_text);
        t2.setText("แชท");

        v3 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i3 = (ImageView) v3.findViewById(R.id.tab_icon);
        t3 = (TextView) v3.findViewById(R.id.tab_text);
        t3.setText("ไทม์ไลน์");

        v4 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i4 = (ImageView) v4.findViewById(R.id.tab_icon);
        t4 = (TextView) v4.findViewById(R.id.tab_text);
        t4.setText("รีวิว");

        v5 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        i5 = (ImageView) v5.findViewById(R.id.tab_icon);
        t5 = (TextView) v5.findViewById(R.id.tab_text);
        t5.setText("โปรไฟล์");

        setupTabIconsUser(0);
    }

    private void setupTabIconsUser(int tab) {
        if (tab == 0) {
            i1.setImageResource(R.drawable.ic_motorcycle_dark);
            t1.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i1.setImageResource(R.drawable.ic_motorcycle_white);
            t1.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 1) {
            i2.setImageResource(R.drawable.ic_chat_dark);
            t2.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i2.setImageResource(R.drawable.ic_chat_white);
            t2.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 2) {
            i3.setImageResource(R.drawable.ic_timeline_dark);
            t3.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i3.setImageResource(R.drawable.ic_timeline_white);
            t3.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 3) {
            i4.setImageResource(R.drawable.ic_star_dark);
            t4.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i4.setImageResource(R.drawable.ic_star_white);
            t4.setTextColor(getResources().getColor(R.color.iconTab));
        }

        if (tab == 4) {
            i5.setImageResource(R.drawable.ic_person_dark);
            t5.setTextColor(getResources().getColor(R.color.teal));
        } else {
            i5.setImageResource(R.drawable.ic_person_white);
            t5.setTextColor(getResources().getColor(R.color.iconTab));
        }

        tabLayout.getTabAt(0).setCustomView(v1);
        tabLayout.getTabAt(1).setCustomView(v2);
        tabLayout.getTabAt(2).setCustomView(v3);
        tabLayout.getTabAt(3).setCustomView(v4);
        tabLayout.getTabAt(4).setCustomView(v5);
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
        edChat.putBoolean(ALERT, false);
        edChat.commit();
        super.onStart();
        Log.d("Main check", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        edChat.putBoolean(ALERT, false);
        edChat.commit();
        Log.d("Main check", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
        Log.d("Main check", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
        Log.d("Main check", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        edChat.putBoolean(ALERT, true);
        edChat.commit();
        Log.d("Main check", "onDestroy");
    }
}
