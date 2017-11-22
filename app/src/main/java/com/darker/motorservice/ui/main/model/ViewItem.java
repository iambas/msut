package com.darker.motorservice.ui.main.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Darker on 22/11/60.
 */

public class ViewItem {
    private View view;
    private ImageView imageView;
    private TextView textView;
    private int drawable;

    public ViewItem() {}

    public ViewItem(View view, ImageView imageView, TextView textView, int drawable) {
        this.view = view;
        this.imageView = imageView;
        this.textView = textView;
        this.drawable = drawable;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }
}
