package com.darker.motorservice.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.darker.motorservice.R;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.main.callback.ImageUploadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ImageUtil {

    private int size = 100;

    public ImageUtil() {
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

    public static Bitmap convertToBitmap(byte[] b) {
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

    public static int[] getStoreIcon(){
        return new int[]{
                R.drawable.ic_chat_white,
                R.drawable.ic_timeline_white,
                R.drawable.ic_equalizer_white,
                R.drawable.ic_star_white,
                R.drawable.ic_person_white
        };
    }

    public static int[] getUserIcon(){
        return new int[]{
                R.drawable.ic_motorcycle_white,
                R.drawable.ic_chat_white,
                R.drawable.ic_timeline_white,
                R.drawable.ic_star_white,
                R.drawable.ic_person_white
        };
    }

    public static void uploadImage(final String imageName, final Bitmap imageBitmap, final ImageUploadCallback callback){
        UploadTask uploadTask = getUploadTask(imageName, imageBitmap);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailure(imageName);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onSuccess(imageName, imageBitmap);
            }
        });
    }

    private static UploadTask getUploadTask(String imageName, Bitmap imageBitmap){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference mountainsRef = storageReference.child(imageName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        return mountainsRef.putBytes(data);
    }

    public static Bitmap getBitmap(Context context, byte[] bytes, int resource){
        Bitmap bitmap;
        try{
            bitmap = convertToBitmap(bytes);
        } catch (Exception e){
            bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        }
        return bitmap;
    }
}