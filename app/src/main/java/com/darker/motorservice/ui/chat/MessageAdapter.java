package com.darker.motorservice.ui.chat;

import android.annotation.SuppressLint;
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

import com.bumptech.glide.Glide;
import com.darker.motorservice.R;
import com.darker.motorservice.sharedpreferences.AccountType;
import com.darker.motorservice.sharedpreferences.SharedPreferencesUtil;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.ui.map.MapsActivity;
import com.darker.motorservice.ui.show_picture.ShowPictureActivity;
import com.darker.motorservice.utility.DateUtil;
import com.darker.motorservice.utility.ImageUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.darker.motorservice.R.id.img;
import static com.darker.motorservice.utility.Constant.CHAT;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.utility.Constant.DATA;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.utility.Constant.KEY_IMAGE;
import static com.darker.motorservice.utility.Constant.LATLNG;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.STATUS;

public class MessageAdapter extends ArrayAdapter {
    private List<ChatMessageItem> items;
    private Context context;
    private int bYear;
    private int bMonth;
    private int bDay;
    private SharedPreferences prefs;
    private int resource;

    private LinearLayout layout;
    private ImageView imageView;
    private TextView tvTime;
    private TextView tvMessageLeft;
    private TextView tvMessageRight;
    private TextView tvLeftTime;
    private TextView tvRightTime;
    private ImageView ivLeft;
    private ImageView ivRight;
    private ImageView ivMessage;
    private TextView textView;

    public MessageAdapter(Context context, int resource, List<ChatMessageItem> items) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.items = items;
        prefs = SharedPreferencesUtil.getLoginPreferences(context);
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        view = inflater.inflate(resource, parent, false);

        bindView(view);
        setPaddingForLastView(position);

        final ChatMessageItem chat = items.get(position);

        String strDate = chat.getDate();
        String timeChat = DateUtil.getTimeChat(strDate);
        String dateTime = DateUtil.getDateTime(context, strDate);

        setTvTimeWithDateTime(position, strDate, dateTime);

        String msg = chat.getMessage();
        final String[] arrMsg = msg.split(": ");
        boolean isImg = msg.contains(KEY_IMAGE);
        String imgName = "";
        if (isImg) imgName = msg.replace(KEY_IMAGE, "");

        if (chat.getStatus().equals(prefs.getString(STATUS, ""))) {
            if (!chat.getRead().equals(""))
                timeChat += "\nอ่านแล้ว";

            ivMessage = ivRight;
            textView = tvMessageRight;
            tvRightTime.setText(timeChat);
            tvRightTime.setVisibility(View.VISIBLE);
        } else {
            ivMessage = ivLeft;
            textView = tvMessageLeft;
            tvLeftTime.setText(timeChat);
            tvLeftTime.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);

            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(CHAT);
            db = db.child(prefs.getString(KEY_CHAT, "")).child(DATA);
            final DatabaseReference finalDb = db;
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.child(STATUS).getValue().equals(prefs.getString(STATUS, ""))) {
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
                    setImageView(imageView);
                }
            }else {
                setImageView(imageView);
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
                ivMessage.setImageBitmap(chat.getBitmap());
            }catch (Exception e){
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_edit_white);
                ivMessage.setImageBitmap(bitmap);
                setImageViewFromStorage(ivMessage, imgName);
            }
            ivMessage.setVisibility(View.VISIBLE);
            final String finalImgName = imgName;
            ivMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowPictureActivity.class);
                    intent.putExtra(KEY_IMAGE, finalImgName);
                    context.startActivity(intent);
                }
            });
        }else {
            textView.setText(msg);
            ivMessage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void setTvTimeWithDateTime(int position, String strDate, String dateTime) {
        if (position == 0) {
            tvTime.setText(dateTime);
            tvTime.setVisibility(View.VISIBLE);
        } else {
            String strDate2 = items.get(position - 1).getDate();
            if (DateUtil.isDayDiffer(strDate, strDate2)){
                tvTime.setText(dateTime);
                tvTime.setVisibility(View.VISIBLE);
            }
        }
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

    private void setPaddingForLastView(int position) {
        if (position == getCount() - 1) {
            layout.setPadding(32, 4, 32, 12);
        }
    }

    private void bindView(View view) {
        layout = (LinearLayout) view.findViewById(R.id.layout);
        imageView = (ImageView) view.findViewById(img);
        tvTime = (TextView) view.findViewById(R.id.time);
        tvMessageLeft = (TextView) view.findViewById(R.id.message_left);
        tvMessageRight = (TextView) view.findViewById(R.id.message_right);
        tvLeftTime = (TextView) view.findViewById(R.id.ltime);
        tvRightTime = (TextView) view.findViewById(R.id.rtime);
        ivLeft = (ImageView) view.findViewById(R.id.img_left);
        ivRight = (ImageView) view.findViewById(R.id.img_right);
    }

    private void setImageView(ImageView imageView) {
        if (AccountType.isCustomer(context)) {
            Bitmap bitmap = ImageUtil.getImgProfile(context, prefs.getString(CHAT_WITH_ID, ""));
            imageView.setImageBitmap(bitmap);
        } else {
            String img = prefs.getString(IMG, "");
            String url = ImageUtil.getUrlPictureFacebook(img);
            Glide.with(context)
                    .load(url)
                    .into(imageView);
        }
    }

    private void setImageViewFromStorage(final ImageView imageView, final String path) {
        StorageReference islandRef = FirebaseStorage.getInstance().getReference().child(path);
        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .into(imageView);
            }
        });
    }
}
