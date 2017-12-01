package com.darker.motorservice.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.darker.motorservice.firebase.FirebaseUtil;
import com.darker.motorservice.sharedpreferences.AccountType;
import com.darker.motorservice.sharedpreferences.SharedPreferencesUtil;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.ui.map.MapsActivity;
import com.darker.motorservice.ui.show_picture.ShowPictureActivity;
import com.darker.motorservice.utility.DateUtil;
import com.darker.motorservice.utility.GPSUtil;
import com.darker.motorservice.utility.ImageUtil;

import java.util.List;

import static com.darker.motorservice.R.id.img;
import static com.darker.motorservice.utility.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.LATLNG;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.ImageUtil.KEY_IMAGE;

public class MessageAdapter extends ArrayAdapter {
    private TextView tvTime;
    private TextView tvMessageLeft;
    private TextView tvMessageRight;
    private TextView tvLeftTime;
    private TextView tvRightTime;
    private TextView textView;
    private ImageView ivLeft;
    private ImageView ivRight;
    private ImageView ivMessage;
    private ImageView ivAccount;
    private LinearLayout layout;

    private List<ChatMessageItem> items;
    private Context context;
    private SharedPreferences prefs;
    private int resource;

    public MessageAdapter(Context context, int resource, List<ChatMessageItem> items) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.items = items;
        prefs = SharedPreferencesUtil.getLoginPreferences(context);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        view = getInflateView(parent);
        bindView(view);
        setPaddingForLastView(position);

        ChatMessageItem chat = items.get(position);
        String strDate = chat.getDate();
        String timeChat = DateUtil.getTimeChat(strDate);
        String dateTime = DateUtil.getDateTime(context, strDate);
        String chatMessage = chat.getMessage();

        setTvTimeWithDateTime(position, strDate, dateTime);
        manageStatusMessage(position, chat, timeChat);
        chatMessage = ifGPSMessageSetData(chat, chatMessage);
        setMessageOrImageView(chatMessage);

        return view;
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

    private void manageStatusMessage(int position, ChatMessageItem chat, String timeChat) {
        if (SharedPreferencesUtil.isStatusMessageEqualAccountLogin(context, chat.getStatus())) {
            if (!chat.getRead().equals("")) {
                timeChat += "\nอ่านแล้ว";
            }

            ivMessage = ivRight;
            textView = tvMessageRight;
            tvRightTime.setText(timeChat);
            tvRightTime.setVisibility(View.VISIBLE);
        } else {
            ivMessage = ivLeft;
            textView = tvMessageLeft;
            tvLeftTime.setText(timeChat);
            tvLeftTime.setVisibility(View.VISIBLE);
            ivAccount.setVisibility(View.VISIBLE);

            FirebaseUtil.updateMessageToReaded(context);

            if (position > 0) {
                String bs = items.get(position-1).getStatus();
                if (!bs.equals(chat.getStatus())){
                    setImageAccountView(ivAccount);
                }
            }else {
                setImageAccountView(ivAccount);
            }
        }
    }

    private String ifGPSMessageSetData(final ChatMessageItem chat, String chatMessage) {
        if (GPSUtil.isGPSMessage(chatMessage)) {
            final String latLng = GPSUtil.getLatLngFromMessage(chatMessage);
            chatMessage = chat.getSender() + "\nได้ส่งตำแหน่ง GPS\nกดเพื่อดูตำแหน่ง";
            textView.setOnClickListener(view -> {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(NAME, chat.getSender());
                intent.putExtra(LATLNG, latLng);
                context.startActivity(intent);
            });
        }
        return chatMessage;
    }

    private void setMessageOrImageView(String chatMessage) {
        if (ImageUtil.isImageMessage(chatMessage)){
            final String pathImage = chatMessage.replace(KEY_IMAGE, "");
            ImageUtil.setImageViewFromStorage(context, ivMessage, pathImage);
            hideMessageView();
            showImageView();
            ivMessage.setOnClickListener(v -> {
                Intent intent = new Intent(context, ShowPictureActivity.class);
                intent.putExtra(KEY_IMAGE, pathImage);
                context.startActivity(intent);
            });
        }else {
            textView.setText(chatMessage);
            hideImageView();
            showMessageView();
        }
    }

    private void showMessageView() {
        textView.setVisibility(View.VISIBLE);
    }

    private void hideMessageView() {
        textView.setVisibility(View.GONE);
    }

    private void hideImageView() {
        ivMessage.setVisibility(View.GONE);
    }


    private void showImageView() {
        ivMessage.setVisibility(View.VISIBLE);
    }

    private View getInflateView(@NonNull ViewGroup parent) {
        View view;LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        view = inflater.inflate(resource, parent, false);
        return view;
    }

    private void setTvTimeWithDateTime(int position, String strDate, String dateTime) {
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

    private void setPaddingForLastView(int position) {
        if (position == getCount() - 1) {
            layout.setPadding(32, 4, 32, 12);
        }
    }

    private void bindView(View view) {
        layout = (LinearLayout) view.findViewById(R.id.layout);
        ivAccount = (ImageView) view.findViewById(img);
        tvTime = (TextView) view.findViewById(R.id.time);
        tvMessageLeft = (TextView) view.findViewById(R.id.message_left);
        tvMessageRight = (TextView) view.findViewById(R.id.message_right);
        tvLeftTime = (TextView) view.findViewById(R.id.ltime);
        tvRightTime = (TextView) view.findViewById(R.id.rtime);
        ivLeft = (ImageView) view.findViewById(R.id.img_left);
        ivRight = (ImageView) view.findViewById(R.id.img_right);
    }

    private void setImageAccountView(ImageView imageView) {
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
}
