package com.darker.motorservice.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.ui.map.MapsActivity;
import com.darker.motorservice.ui.show_picture.ShowPictureActivity;
import com.darker.motorservice.utility.ImageUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.darker.motorservice.R.id.img;
import static com.darker.motorservice.utility.Constant.CHAT;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.utility.Constant.DATA;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.utility.Constant.KEY_IMAGE;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.LATLNG;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.USER;

public class MessageAdapter extends ArrayAdapter {
    private List<ChatMessageItem> items;
    private Context context;
    private int bYear, bMonth, bDay;
    private SharedPreferences sh;
    private int resource;

    public MessageAdapter(Context context, int resource, List<ChatMessageItem> items) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.items = items;
        sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        view = inflater.inflate(resource, parent, false);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout);
        ImageView imageView = (ImageView) view.findViewById(img);
        TextView timeView = (TextView) view.findViewById(R.id.time);
        TextView msgLeft = (TextView) view.findViewById(R.id.message_left);
        TextView msgRight = (TextView) view.findViewById(R.id.message_right);
        TextView lTime = (TextView) view.findViewById(R.id.ltime);
        TextView rTime = (TextView) view.findViewById(R.id.rtime);
        ImageView imgLeft = (ImageView) view.findViewById(R.id.img_left);
        ImageView imgRight = (ImageView) view.findViewById(R.id.img_right);
        ImageView imgMsg;
        TextView textView;

        final ChatMessageItem chat = items.get(position);
        if (position == getCount() - 1) {
            layout.setPadding(32, 4, 32, 12);
        }

        String strDate = chat.getDate();
        int year, month, day, hour, minute;
        year = month = day = hour = minute = 0;
        String Months[] = {"ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.",
                "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.",
                "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."};

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = df.parse(strDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DATE);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String h = String.valueOf(hour), m = String.valueOf(minute);
        if (hour < 10) h = "0" + hour;
        if (minute < 10) m = "0" + minute;
        String dtime = h + ":" + m;
        String time = day + " " + Months[month] + " " + (year + 543);

        if (position == 0) {
            timeView.setText(time);
            timeView.setVisibility(View.VISIBLE);
        } else {
            String sDate = items.get(position - 1).getDate();
            try {
                Date date = df.parse(sDate);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                bYear = c.get(Calendar.YEAR);
                bMonth = c.get(Calendar.MONTH);
                bDay = c.get(Calendar.DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (bYear != year || bMonth != month || bDay != day) {
                timeView.setText(time);
                timeView.setVisibility(View.VISIBLE);
            }
        }

        String msg = chat.getMessage();
        final String[] arrMsg = msg.split(": ");
        boolean isImg = msg.contains(KEY_IMAGE);
        String imgName = "";
        if (isImg) imgName = msg.replace(KEY_IMAGE, "");

        if (chat.getStatus().equals(sh.getString(STATUS, ""))) {
            if (!chat.getRead().equals(""))
                dtime += "\nอ่านแล้ว";

            imgMsg = imgRight;
            textView = msgRight;
            /*if (isImg){
                imgRTime.setText(dtime);
                imgRTime.setVisibility(View.VISIBLE);
            }else {
                rTime.setText(dtime);
                rTime.setVisibility(View.VISIBLE);
            }*/
            rTime.setText(dtime);
            rTime.setVisibility(View.VISIBLE);
        } else {
            imgMsg = imgLeft;
            textView = msgLeft;
            /*if (isImg){
                imgLTime.setText(dtime);
                imgLTime.setVisibility(View.VISIBLE);
            }else {
                lTime.setText(dtime);
                lTime.setVisibility(View.VISIBLE);
            }*/
            lTime.setText(dtime);
            lTime.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);

            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(CHAT);
            db = db.child(sh.getString(KEY_CHAT, "")).child(DATA);
            final DatabaseReference finalDb = db;
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.child(STATUS).getValue().equals(sh.getString(STATUS, ""))) {
                            finalDb.child(ds.getKey()).child("read").setValue("อ่านแล้ว");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            if (position > 0) {
                String bs = items.get(position-1).getStatus();
                if (!bs.equals(chat.getStatus())){
                    setImg(imageView);
                }
            }else {
                setImg(imageView);
            }
        }

        if (arrMsg[0].equals("lat/lng")) {
            msg = chat.getSender() + "\nได้ส่งตำแหน่ง GPS\nกดเพื่อดูตำแหน่ง";
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra(NAME, chat.getSender());
                    intent.putExtra(LATLNG, arrMsg[1]);
                    context.startActivity(intent);
                }
            });
        }

        if (isImg){
            textView.setVisibility(View.GONE);
            try{
                imgMsg.setImageBitmap(chat.getBitmap());
            }catch (Exception e){
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_edit_white);
                imgMsg.setImageBitmap(bitmap);
                loadImg(imgMsg, imgName);
            }
            imgMsg.setVisibility(View.VISIBLE);
            final String finalImgName = imgName;
            imgMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowPictureActivity.class);
                    intent.putExtra(KEY_IMAGE, finalImgName);
                    context.startActivity(intent);
                }
            });
        }else {
            imgMsg.setVisibility(View.GONE);
            textView.setText(msg);
            textView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void setImg(ImageView imageView) {
        // set image each store
        if (sh.getString(STATUS, "").equals(USER)) {
            Bitmap bitmap = ImageUtil.getImgProfile(context, sh.getString(CHAT_WITH_ID, ""));
            imageView.setImageBitmap(bitmap);
        } else {
            String img = sh.getString(IMG, "");
            String url = "https://graph.facebook.com/" + img + "/picture?height=50&width=50";
            Picasso.with(context).load(url).into(imageView);
        }
    }

    private void loadImg(final ImageView imageView, final String path) {
        StorageReference islandRef = FirebaseStorage.getInstance().getReference().child(path);
        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).into(imageView);
            }
        });
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
