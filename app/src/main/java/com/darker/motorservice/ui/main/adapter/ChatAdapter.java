package com.darker.motorservice.ui.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.darker.motorservice.R;
import com.darker.motorservice.ui.chat.ChatActivity;
import com.darker.motorservice.ui.main.model.ChatItem;
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.database.ServiceDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_ID;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_NAME;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.PHOTO;
import static com.darker.motorservice.utility.Constant.SERVICE;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.TEL_NUM;
import static com.darker.motorservice.utility.Constant.USER;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatItem> items;
    private Context context;

    public ChatAdapter(Context context, List<ChatItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        final ChatItem chatItem = items.get(position);
        final boolean isUser = sh.getString(STATUS, "").equals(USER);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(chatItem.getChatWithId());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isUser) {
                    boolean mStatus = (boolean) dataSnapshot.getValue();
                    if (mStatus)
                        holder.online.setBackgroundResource(R.drawable.ic_cycle);
                    else
                        holder.online.setBackgroundResource(R.color.white);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (isUser) {
            Bitmap bitmap;
            try {
                bitmap = ImageUtil.getImgProfile(context, chatItem.getChatWithId());
            } catch (Exception e) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
            }
            holder.imageView.setImageBitmap(bitmap);
        } else {
            String url = "https://graph.facebook.com/" + chatItem.getPhoto() + "/picture?height=70&width=70";
            Picasso.with(context).load(url).into(holder.imageView);
            Glide.with(context).load(url).into(holder.imageView);
        }

        holder.txtName.setText(chatItem.getChatWithName());
        holder.txtMessage.setText(chatItem.getMessage());

        String strDate = items.get(position).getDate();
        int hour = 0, minute = 0;

        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = df.parse(strDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String h = String.valueOf(hour), m = String.valueOf(minute);
        if (hour < 10)
            h = "0" + hour;
        if (minute < 10)
            m = "0" + minute;
        String time = h + ":" + m;
        holder.timeView.setText(time);

        if (chatItem.getRead().equals("") && !sh.getString(STATUS, "").equals(chatItem.getStatus())) {
            holder.txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
            holder.txtMessage.setTextColor(Color.BLACK);
            holder.txtName.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            holder.txtMessage.setTypeface(Typeface.DEFAULT);
            holder.txtMessage.setTextColor(Color.parseColor("#777777"));
            holder.txtName.setTypeface(Typeface.DEFAULT);
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = "";
                String telNum = "";
                if (isUser){
                    status = USER;
                    telNum = new ServiceDatabase(context).getService(chatItem.getChatWithId()).getTel();
                }else{
                    status = SERVICE;
                }
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(KEY_CHAT, chatItem.getKeyChat());
                intent.putExtra(CHAT_WITH_ID, chatItem.getChatWithId());
                intent.putExtra(CHAT_WITH_NAME, chatItem.getChatWithName());
                intent.putExtra(STATUS, status);
                intent.putExtra(TEL_NUM, telNum);
                intent.putExtra(PHOTO, chatItem.getPhoto());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView card;
        private View online;
        private TextView txtName;
        private TextView txtMessage;
        private TextView timeView;
        private ImageView imageView;

        ViewHolder(View view) {
            super(view);
            card = (CardView) view.findViewById(R.id.recycler_item);
            online = view.findViewById(R.id.status);
            txtName = (TextView) view.findViewById(R.id.name);
            txtMessage = (TextView) view.findViewById(R.id.message);
            timeView = (TextView) view.findViewById(R.id.time);
            imageView = (ImageView) view.findViewById(R.id.image);
        }
    }
}