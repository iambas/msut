package com.darker.motorservice.ui.main.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.details.DetailActivity;
import com.darker.motorservice.ui.main.adapter.ReviewAdapter;
import com.darker.motorservice.ui.main.fragment.spinner.SpinnerUtil;
import com.darker.motorservice.ui.main.model.ReviewItem;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.utility.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.REVIEW;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.USER;

public class ReviewFragment extends Fragment implements View.OnClickListener{
    private Context context;
    private View mView;
    private ReviewAdapter adapter;
    private ArrayList<ReviewItem> reviewItems;
    private String id, uid, status;
    private DatabaseReference dbRef;
    private ProgressBar progressBar;
    private TextView txtNull;
    private Bundle bundle;
    private float rate = 0.0f, myRate = 1.0f;
    private ReviewItem reviewItem;
    private RatingBar ratingBar;

    public ReviewFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;
        context = getContext();
        bundle = savedInstanceState;
        dbRef = FirebaseDatabase.getInstance().getReference();

        initReviewItem();
        SharedPreferences();
        initListView(view);
        bindView(view);
        checkStatus(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_edit:
                onEditClicked();
                break;
            case R.id.go_service:
                startDetailActivity();
                break;
            case R.id.txt_net_alert:
                updateReviewView();
                break;
            default: break;
        }
    }

    private void checkStatus(View view) {
        boolean isAdmin = new AdminDatabase(context).isAdmin(uid);
        if (isAdmin || status.equals(USER)){
            setUpSpinner(view);
        }else{
            setUpViewService(view);
        }
    }

    private void setUpViewService(View view) {
        id = uid;
        rating();
        view.findViewById(R.id.sel_service).setVisibility(View.GONE);
        view.findViewById(R.id.fab_edit).setVisibility(View.GONE);
        updateReviewView();
    }

    private void setUpSpinner(View view) {
        List<ServicesItem> svList = new ServiceDatabase(context).getAllSerivce();
        List<String> nameList = new ArrayList<String>();
        final List<String> idList = new ArrayList<String>();
        id = svList.get(0).getId();
        for (ServicesItem item : svList){
            nameList.add(item.getName());
            idList.add(item.getId());
        }
        storeSpinner(view, nameList, idList);
    }

    private void storeSpinner(View view, List<String> nameList, final List<String> idList) {
        Spinner spinner = SpinnerUtil.getSpinner(view, R.id.spinner, nameList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long idd) {
                id = idList.get(position);
                rating();
                updateReviewView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void SharedPreferences() {
        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        uid = sh.getString(ID, "");
        status = sh.getString(STATUS, USER);
    }

    private void initReviewItem() {
        reviewItem = new ReviewItem();
        reviewItem.setMsg("");
        reviewItem.setRate(myRate);
        reviewItem.setDate("");
    }

    private void bindView(View view) {
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtNull = (TextView) view.findViewById(R.id.txt_null);
        ratingBar = (RatingBar) view.findViewById(R.id.rating);
    }

    private void startDetailActivity() {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(ID, id);
        context.startActivity(intent);
    }

    private void initListView(View view) {
        reviewItems = new ArrayList<>();
        adapter = new ReviewAdapter(context, R.layout.review_item, reviewItems);
        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    private void rating(){
        DatabaseReference dbReview = dbRef.child(REVIEW).child(id);
        dbReview.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rate = 0.0f;
                setReviewItem(dataSnapshot);
                calculateRate(dataSnapshot);
                ratingBar.setRating(rate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void calculateRate(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            ReviewItem myRev = ds.getValue(ReviewItem.class);
            rate += myRev.getRate();
        }

        if (dataSnapshot.getChildrenCount() > 0)
            rate = rate / dataSnapshot.getChildrenCount();
    }

    public void setReviewItem(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild(uid)){
            ReviewItem item = dataSnapshot.child(uid).getValue(ReviewItem.class);
            reviewItem.setDate(item.getDate());
            reviewItem.setRate(item.getRate());
            reviewItem.setMsg(item.getMsg());
        }
    }

    private void checkNetwork() {
        if (NetworkUtil.disable(getContext())) {
            setViewNetworkDisable();
        } else {
            mView.findViewById(R.id.txt_net_alert).setVisibility(View.GONE);
        }
    }

    private void setViewNetworkDisable() {
        mView.findViewById(R.id.txt_net_alert).setVisibility(View.VISIBLE);
        txtNull.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    public void onEditClicked(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        @SuppressLint("RestrictedApi")
        LayoutInflater inflater = onGetLayoutInflater(bundle);
        @SuppressLint("InflateParams")
        View inflateView = inflater.inflate(R.layout.rating, null);
        builder.setView(inflateView);

        final EditText editRev = (EditText) inflateView.findViewById(R.id.edit_rev);
        editRev.setText(reviewItem.getMsg());
        ratingBar(inflateView);
        setDataBuilder(builder, editRev);
    }

    public void setDataBuilder(AlertDialog.Builder builder, final EditText editRev) {
        builder.setTitle("เขียนรีวิวและให้ดาว");
        builder.setPositiveButton("บันทึก", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = editRev.getText().toString();
                if (!StringUtil.stringOk(msg)) {
                    Toast.makeText(context, "คุณยังไม่ได้เขียนรีวิว!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (NetworkUtil.disable(getContext())) {
                    Toast.makeText(context, "ข้อผิดพลาดเครือข่าย! ไม่สามารถบันทึกได้", Toast.LENGTH_LONG).show();
                    return;
                }

                setValueReview(msg);
                editRev.setText("");
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    public void setValueReview(String msg) {
        String date = StringUtil.getDateFormate("dd/MM/yyyy HH:mm:ss");
        ReviewItem newRev = new ReviewItem(msg, date, myRate);

        DatabaseReference dbReview = dbRef.child(REVIEW).child(id);
        dbReview.child(uid).setValue(newRev);
    }

    public void ratingBar(View inflateView) {
        final RatingBar myRatingBar = (RatingBar) inflateView.findViewById(R.id.rating);
        myRatingBar.setRating(reviewItem.getRate());
        myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                myRate = rating;
                Log.d("rate", String.valueOf(rating));
                Log.d("rate", String.valueOf(myRate));
            }
        });
    }

    private void updateReviewView() {
        progressBar.setVisibility(View.VISIBLE);
        checkNetwork();
        queryReview();
    }

    private void queryReview() {
        DatabaseReference dbReview = dbRef.child(REVIEW).child(id);
        dbReview.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewItems.clear();
                addReviewToList(dataSnapshot);
                setViewVisible();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void addReviewToList(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            ReviewItem reviewItem = ds.getValue(ReviewItem.class);
            reviewItems.add(reviewItem);
            sortReviewItems();
        }
        adapter.notifyDataSetChanged();
    }

    public void setViewVisible() {
        if (reviewItems.size() == 0) {
            txtNull.setVisibility(View.VISIBLE);
        } else {
            txtNull.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    public void sortReviewItems() {
        Collections.sort(reviewItems, new Comparator<ReviewItem>() {
            @Override
            public int compare(ReviewItem r1, ReviewItem r2) {
                return r1.toString().compareToIgnoreCase(r2.toString());
            }
        });
        adapter.notifyDataSetChanged();
    }
}
