package com.darker.motorservice.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.darker.motorservice.R;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.database.ServiceDatabase;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    private int size = 100;

    public ImageUtils() {
    }

    public Bitmap getImgCover(Context context, String id) {
        ServicesItem servicesItem = new ServiceDatabase(context).getService(id);
        Bitmap bitmap;
        try {
            bitmap = convertToBitmap(servicesItem.getImgCover());
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cover);
        }
        return bitmap;
    }

    public Bitmap getImgProfile(Context context, String id) {
        ServicesItem servicesItem = new ServiceDatabase(context).getService(id);
        Bitmap bitmap;
        try {
            bitmap = convertToBitmap(servicesItem.getImgProfile());
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
        }
        return bitmap;
    }

    public Bitmap convertToBitmap(byte[] b) {
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public byte[] toByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth) {
        float rate = (float) bitmap.getHeight() / bitmap.getWidth();
        int wantedHeight = (int) (wantedWidth * rate);
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        //Log.d("size", "" + toByte(output).length / 1024);
        if (toByte(output).length / 1024 > 800 && wantedWidth > 50) {
            return scaleBitmap(bitmap, wantedWidth - 100);
        }

        return output;
    }
}