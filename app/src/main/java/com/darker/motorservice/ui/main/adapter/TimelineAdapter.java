package com.darker.motorservice.ui.main.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.post.PostActivity;
import com.darker.motorservice.model.TimelineItem;
import com.darker.motorservice.database.PictureDatabse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.darker.motorservice.utility.Constant.DATE;
import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.IMG;
import static com.darker.motorservice.utility.Constant.KEY;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.MESSAGE;
import static com.darker.motorservice.utility.Constant.SERVICE;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.TIMELINE;
import static com.darker.motorservice.utility.Constant.USER;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private List<TimelineItem> timelineItems;
    private Context context;
    private String status;
    private DatabaseReference dbRef;
    private StorageReference stRef;

    public TimelineAdapter(Context context, List<TimelineItem> timelineItems) {
        this.context = context;
        this.timelineItems = timelineItems;
        SharedPreferences sh = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        status = sh.getString(STATUS, USER);
        dbRef = FirebaseDatabase.getInstance().getReference();
        stRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final TimelineItem timelineItem = timelineItems.get(position);
        holder.profileView.setImageBitmap(timelineItem.getProfile());
        holder.nameView.setText(timelineItem.getName());
        holder.timeView.setText(timelineItem.getDate());
        if (timelineItem.getMessage().isEmpty()){
            holder.msgView.setVisibility(View.GONE);
        }else{
            holder.msgView.setVisibility(View.VISIBLE);
            holder.msgView.setText(timelineItem.getMessage());
        }

        if (timelineItem.getImgName().isEmpty()){
            holder.imageView.setVisibility(View.GONE);
        }else{
            holder.imageView.setImageBitmap(timelineItem.getImage());
            holder.imageView.setVisibility(View.VISIBLE);
        }

        if (status.equals(SERVICE)){
            holder.expand.setVisibility(View.VISIBLE);
            holder.option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] list = {"แก้ไข", "ลบ"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("จัดการโพสต์");
                    builder.setItems(list, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0){
                                Intent intent = new Intent(context, PostActivity.class);
                                intent.putExtra(KEY, timelineItem.getKey());
                                intent.putExtra(ID,  timelineItem.getId());
                                intent.putExtra(MESSAGE, timelineItem.getMessage());
                                intent.putExtra(IMG, timelineItem.getImgName());
                                intent.putExtra(DATE, timelineItem.getDate());
                                context.startActivity(intent);
                            }else if(position == 1){
                                if (!timelineItem.getImgName().isEmpty()) {
                                    stRef.child(timelineItem.getImgName()).delete();
                                    new PictureDatabse(context).deletePicture(timelineItem.getImgName());
                                }
                                dbRef.child(TIMELINE).child(timelineItem.getKey()).removeValue();
                            }
                        }
                    });
                    builder.create();
                    builder.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timelineItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView profileView;
        private ImageView imageView;
        private TextView nameView;
        private TextView msgView;
        private TextView timeView;
        private LinearLayout expand;
        private ImageButton option;

        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.recycler_item);
            profileView = (ImageView) view.findViewById(R.id.profile);
            imageView = (ImageView) view.findViewById(R.id.image);
            nameView = (TextView) view.findViewById(R.id.name);
            msgView = (TextView) view.findViewById(R.id.message);
            timeView = (TextView) view.findViewById(R.id.time);
            expand = (LinearLayout) view.findViewById(R.id.expand_option);
            option = (ImageButton) view.findViewById(R.id.option);
        }
    }
}
