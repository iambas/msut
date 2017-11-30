package com.darker.motorservice.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
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
import java.io.IOException;

public class ImageUtil {

    public static final String URL_GRAPH_FACEBOOK = "https://graph.facebook.com/";
    public static final String PICTURE_SIZE = "/picture?height=50&width=50";

    public static Bitmap getImgCover(Context context, String id) {
        ServicesItem servicesItem = new ServiceDatabase(context).getService(id);
        Bitmap bitmap;
        try {
            bitmap = convertByteToBitmap(servicesItem.getImgCover());
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cover);
        }
        return bitmap;
    }

    public static Bitmap getImgProfile(Context context, String id) {
        ServicesItem servicesItem = new ServiceDatabase(context).getService(id);
        Bitmap bitmap;
        try {
            bitmap = convertByteToBitmap(servicesItem.getImgProfile());
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
        }
        return bitmap;
    }

    public static Bitmap convertByteToBitmap(byte[] b) {
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public static byte[] convertBitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth) {
        float rate = (float) bitmap.getHeight() / bitmap.getWidth();
        int wantedHeight = (int) (wantedWidth * rate);
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        //Log.d("size", "" + convertBitmapToByte(output).length / 1024);
        if (convertBitmapToByte(output).length / 1024 > 800 && wantedWidth > 50) {
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        return mountainsRef.putBytes(data);
    }

    public static String getImageName(String uid){
        String date = DateUtil.getDateFormat("-yyyy_MM_dd_HH_mm_ss");
        return  "image/" + uid.substring(0, 5) + date + ".png";
    }

    public static Bitmap getBitmap(Context context, byte[] bytes, int resource){
        Bitmap bitmap;
        try{
            bitmap = convertByteToBitmap(bytes);
        } catch (Exception e){
            bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        }
        return bitmap;
    }

    public static Bitmap getImageMediaStore(Context context, Uri imageUri) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
    }

    public static String getUrlPictureFacebook(String path){
        return URL_GRAPH_FACEBOOK + path + PICTURE_SIZE;
    }
}