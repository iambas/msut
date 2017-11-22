package com.darker.motorservice.ui.main.model;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Darker on 22/11/60.
 */

public class ViewItem {
    private ImageView imageView;
    private TextView textView;

    public ViewItem() {}

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ViewItem(ImageView imageView, TextView textView) {
        this.imageView = imageView;
        this.textView = textView;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}
