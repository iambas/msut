package com.darker.motorservice.ui.main.callback;

import android.graphics.Bitmap;

/**
 * Created by Darker on 23/11/60.
 */

public interface ImageUploadCallback {
    void onSuccess(String imageName, Bitmap bitmap);
    void onFailure(String imageName);
}
