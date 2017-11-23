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
import com.darker.motorservice.utils.ImageUtils;

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
        tabLayout.addOnTabSelectedListener(onTabSelectedListener());
    }

    private void setTabUser() {
        setupViewPagerUser(containerViewPager);
        tabLayout.setupWithViewPager(containerViewPager);
        tabUser();
        tabLayout.addOnTabSelectedListener(onTabSelectedListener());
    }

    @SuppressLint("InflateParams")
    private void tabSetter(String tabText, int tabIcon) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        textView.setText(tabText);
        addViewItemToList(view, imageView, textView, tabIcon);
    }

    private void setCustomView() {
        for (int i = 0; i < viewItemList.size(); i++){
            View view = viewItemList.get(i).getView();
            tabLayout.getTabAt(i).setCustomView(view);
        }
    }

    private void tabService() {
        setTextAndIconStoreTab();
        setTabIcon();
        setCustomView();
        setTabSelected(0);
    }

    private void setTextAndIconStoreTab() {
        int[] iconStores = ImageUtils.getStoreIcon();
        String[] tabStores = getResources().getStringArray(R.array.tab_store);

        for (int i = 0; i < iconStores.length; i++){
            tabSetter(tabStores[i], iconStores[i]);
        }

        if (isAdmin){
            tabSetter(getString(R.string.tab_add_store), R.drawable.ic_add_circle_white);
        }
    }

    private void setTabIcon() {
        for (ViewItem viewItem: viewItemList) {
            viewItem.getImageView().setImageResource(viewItem.getDrawable());
        }
    }

    private void addViewItemToList(View view, ImageView imageView, TextView textView, int tabIcon) {
        ViewItem viewItem = new ViewItem(view, imageView, textView, tabIcon);
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
        setTextAndIconUserTab();
        setTabIcon();
        setCustomView();
        setTabSelected(0);
    }

    private void setTextAndIconUserTab() {
        int[] iconUsers = ImageUtils.getUserIcon();
        String[] tabTexts = getResources().getStringArray(R.array.tab_user);

        for (int i = 0; i < iconUsers.length; i++){
            tabSetter(tabTexts[i], iconUsers[i]);
        }
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

    private void setTabSelected(int position) {
        int tealColor = getResources().getColor(R.color.teal);
        ViewItem viewItem = viewItemList.get(position);
        viewItem.getImageView().setColorFilter(tealColor);
        viewItem.getTextView().setTextColor(tealColor);
    }

    private void setTabUnSelected(int position) {
        int tabColor = getResources().getColor(R.color.iconTab);
        ViewItem viewItem = viewItemList.get(position);
        viewItem.getImageView().setColorFilter(tabColor);
        viewItem.getTextView().setTextColor(tabColor);
    }

    @NonNull
    private TabLayout.OnTabSelectedListener onTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabSelected(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabUnSelected(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
    }
}
