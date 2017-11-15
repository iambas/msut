package com.darker.motorservice.fragment;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.activity.DetailActivity;
import com.darker.motorservice.adapter.ReviewAdapter;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.model.Review;
import com.darker.motorservice.model.Services;
import com.darker.motorservice.database.AdminDatabase;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.darker.motorservice.Constant.ID;
import static com.darker.motorservice.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.Constant.REVIEW;
import static com.darker.motorservice.Constant.STATUS;
import static com.darker.motorservice.Constant.USER;

public class ReviewFragment extends Fragment {

    private Context context;
    private View mView;
    private ReviewAdapter adapter;
    private ArrayList<Review> reviews;
    private String id, uid;
    private DatabaseReference dbRef;
    private ProgressBar progressBar;
    private TextView txtNull;
    private Bundle bundle;
    private float rate = 0.0f, myRate = 1.0f;
    private Review review;
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
        review = new Review();
        review.setMsg("");
        review.setRate(myRate);
        review.setDate("");

        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        uid = sh.getString(ID, "");
        String status = sh.getString(STATUS, USER);
        boolean isAdmin = new AdminDatabase(context).isAdmin(uid);

        dbRef = FirebaseDatabase.getInstance().getReference();
        reviews = new ArrayList<>();
        adapter = new ReviewAdapter(context, R.layout.review_item, reviews);
        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(adapter);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtNull = (TextView) view.findViewById(R.id.txt_null);
        ratingBar = (RatingBar) view.findViewById(R.id.rating);

        if (isAdmin || status.equals(USER)){
            List<Services> svList = new ServiceDatabase(context).getAllSerivce();
            List<String> nameList = new ArrayList<String>();
            final List<String> idList = new ArrayList<String>();
            id = svList.get(0).getId();
            for (Services s : svList){
                nameList.add(s.getName());
                idList.add(s.getId());
            }
            view.findViewById(R.id.fab_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditClicked();
                }
            });

            view.findViewById(R.id.go_service).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(ID, id);
                    context.startActivity(intent);
                }
            });

            Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
            ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nameList);
            areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(areasAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long idd) {
                    id = idList.get(position);
                    rating();
                    update();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }else{
            id = uid;
            rating();
            view.findViewById(R.id.sel_service).setVisibility(View.GONE);
            view.findViewById(R.id.fab_edit).setVisibility(View.GONE);
            update();
        }
    }

    private void rating(){
        DatabaseReference db = dbRef.child(REVIEW).child(id);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rate = 0.0f;
                if (dataSnapshot.hasChild(uid)){
                    Review r = dataSnapshot.child(uid).getValue(Review.class);
                    review.setDate(r.getDate());
                    review.setRate(r.getRate());
                    review.setMsg(r.getMsg());
                }

                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Review myRev = ds.getValue(Review.class);
                    rate += myRev.getRate();
                }

                if (dataSnapshot.getChildrenCount() > 0)
                    rate = rate / dataSnapshot.getChildrenCount();

                ratingBar.setRating(rate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void check() {
        if (NetWorkUtils.disable(getContext())) {
            mView.findViewById(R.id.txt_net_alert).setVisibility(View.VISIBLE);
            txtNull.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        } else {
            mView.findViewById(R.id.txt_net_alert).setVisibility(View.GONE);
        }

        mView.findViewById(R.id.txt_net_alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });
    }

    public void onEditClicked(){
        final DatabaseReference db = dbRef.child(REVIEW).child(id);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        @SuppressLint("RestrictedApi") LayoutInflater inflater = getLayoutInflater(bundle);
        View vb = inflater.inflate(R.layout.rating, null);
        builder.setView(vb);

        final EditText editRev = (EditText) vb.findViewById(R.id.edit_rev);
        final RatingBar myRatingBar = (RatingBar) vb.findViewById(R.id.rating);
        myRatingBar.setRating(review.getRate());
        editRev.setText(review.getMsg());

        myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                myRate = rating;
                Log.d("rate", String.valueOf(rating));
                Log.d("rate", String.valueOf(myRate));
            }
        });

        builder.setTitle("เขียนรีวิวและให้ดาว");
        builder.setPositiveButton("บันทึก", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = editRev.getText().toString();
                if (msg.equals("")) {
                    Toast.makeText(context, "คุณยังไม่ได้เขียนรีวิว!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (NetWorkUtils.disable(getContext())) {
                    Toast.makeText(context, "ข้อผิดพลาดเครือข่าย! ไม่สามารถบันทึกได้", Toast.LENGTH_LONG).show();
                    return;
                }

                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                Review newRev = new Review(msg, date, myRate);
                db.child(uid).setValue(newRev);
                editRev.setText("");
            }
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void update() {
        progressBar.setVisibility(View.VISIBLE);
        check();
        DatabaseReference db = dbRef.child(REVIEW).child(id);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviews.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Review review = ds.getValue(Review.class);
                    reviews.add(review);
                    Collections.sort(reviews, new Comparator<Review>() {
                        @Override
                        public int compare(Review r1, Review r2) {
                            return r1.toString().compareToIgnoreCase(r2.toString());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }
                adapter.notifyDataSetChanged();
                if (reviews.size() == 0) {
                    txtNull.setVisibility(View.VISIBLE);
                } else {
                    txtNull.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
