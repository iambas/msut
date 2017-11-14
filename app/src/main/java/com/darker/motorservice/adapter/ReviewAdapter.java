package com.darker.motorservice.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.model.Review;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private List<Review> reviews;

    public ReviewAdapter(Context context, int resource, List<Review> reviews) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        view = inflater.inflate(resource, parent, false);

        View topView = view.findViewById(R.id.top_view);
        View bottomView = view.findViewById(R.id.bottom_view);
        View lineView = view.findViewById(R.id.line_view);
        TextView txtDate = (TextView) view.findViewById(R.id.date);
        TextView txtMessage = (TextView) view.findViewById(R.id.message);

        Review review = reviews.get(position);
        if (position == 0) {
            topView.setVisibility(View.VISIBLE);
        }
        if (position == getCount() - 1 && position != 0) {
            lineView.setVisibility(View.GONE);
            bottomView.setVisibility(View.VISIBLE);
        }
        txtDate.setText(review.getDate().split(" ")[0]);
        txtMessage.setText(review.getMsg());
        ((RatingBar) view.findViewById(R.id.rating)).setRating(review.getRate());

        return view;
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
