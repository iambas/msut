package com.darker.motorservice.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.darker.motorservice.R;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.main.callback.ImageUploadCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {

    public static final String URL_GRAPH_FACEBOOK = "https://graph.facebook.com/";
    public static final String PICTURE_SIZE = "/picture?height=50&width=50";
    public static final String KEY_IMAGE = "_i_m_a_g_e_";

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

        if (convertBitmapToByte(output).length / 1024 > 800 && wantedWidth > 50) {
            return scaleBitmap(bitmap, wantedWidth - 100);
        }

        return output;
    }

    public static int[] getStoreIcon() {
        return new int[]{
                R.drawable.ic_chat_white,
                R.drawable.ic_timeline_white,
                R.drawable.ic_equalizer_white,
                R.drawable.ic_star_white,
                R.drawable.ic_person_white
        };
    }

    public static int[] getUserIcon() {
        return new int[]{
                R.drawable.ic_motorcycle_white,
                R.drawable.ic_chat_white,
                R.drawable.ic_timeline_white,
                R.drawable.ic_star_white,
                R.drawable.ic_person_white
        };
    }

    public static void uploadImage(final String imageName, final Bitmap imageBitmap, final ImageUploadCallback callback) {
        UploadTask uploadTask = getUploadTask(imageName, imageBitmap);
        uploadTask
                .addOnFailureListener(exception ->
                        callback.onFailure(imageName))
                .addOnSuccessListener(taskSnapshot ->
                        callback.onSuccess(imageName, imageBitmap));
    }

    private static UploadTask getUploadTask(String imageName, Bitmap imageBitmap) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference mountainsRef = storageReference.child(imageName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        return mountainsRef.putBytes(data);
    }

    public static String getImageName(String uid) {
        String date = DateUtil.getDateFormat("-yyyy_MM_dd_HH_mm_ss");
        return "image/" + uid.substring(0, 5) + date + ".png";
    }

    public static Bitmap getBitmap(Context context, byte[] bytes, int resource) {
        Bitmap bitmap;
        try {
            bitmap = convertByteToBitmap(bytes);
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        }
        return bitmap;
    }

    public static Bitmap getImageMediaStore(Context context, Uri imageUri) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
    }

    public static String getUrlPictureFacebook(String path) {
        return URL_GRAPH_FACEBOOK + path + PICTURE_SIZE;
    }

    public static boolean isImageMessage(String message) {
        return message.contains(ImageUtil.KEY_IMAGE);
    }

    public static String getImagePath(String message){
        return message.replace(ImageUtil.KEY_IMAGE, "");
    }

    public static void setImageViewFromStorage(final Context context, final ImageView imageView, final String path) {
        StorageReference islandRef = FirebaseUtil.getChildStorage(path);
        islandRef.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(context)
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.bg_edit_white)
                                .error(R.drawable.bg_edit_white))
                        .into(imageView));
    }

    public static Bitmap getBitmapNotOver720px(Context context, Uri imageUri){
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtil.getImageMediaStore(context, imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }

        if (bitmap.getWidth() > 720)
            bitmap = ImageUtil.scaleBitmap(bitmap, 720);
        else
            bitmap = ImageUtil.scaleBitmap(bitmap, bitmap.getWidth());

        return bitmap;
    }
}