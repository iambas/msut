package com.darker.motorservice.adapter;

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
import com.darker.motorservice.activity.PostActivity;
import com.darker.motorservice.data.Timeline;
import com.darker.motorservice.database.PictureHandle;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.darker.motorservice.data.Constant.DATE;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.IMG;
import static com.darker.motorservice.data.Constant.KEY;
import static com.darker.motorservice.data.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.data.Constant.MESSAGE;
import static com.darker.motorservice.data.Constant.SERVICE;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.TIMELINE;
import static com.darker.motorservice.data.Constant.USER;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private List<Timeline> timelines;
    private Context context;
    private String status;
    private DatabaseReference dbRef;
    private StorageReference stRef;

    public TimelineAdapter(Context context, List<Timeline> timelines) {
        this.context = context;
        this.timelines = timelines;
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
        final Timeline timeline = timelines.get(position);
        holder.profileView.setImageBitmap(timeline.getProfile());
        holder.nameView.setText(timeline.getName());
        holder.timeView.setText(timeline.getDate());
        if (timeline.getMessage().isEmpty()){
            holder.msgView.setVisibility(View.GONE);
        }else{
            holder.msgView.setVisibility(View.VISIBLE);
            holder.msgView.setText(timeline.getMessage());
        }

        if (timeline.getImgName().isEmpty()){
            holder.imageView.setVisibility(View.GONE);
        }else{
            holder.imageView.setImageBitmap(timeline.getImage());
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
                                intent.putExtra(KEY, timeline.getKey());
                                intent.putExtra(ID,  timeline.getId());
                                intent.putExtra(MESSAGE, timeline.getMessage());
                                intent.putExtra(IMG, timeline.getImgName());
                                intent.putExtra(DATE, timeline.getDate());
                                context.startActivity(intent);
                            }else if(position == 1){
                                if (!timeline.getImgName().isEmpty()) {
                                    stRef.child(timeline.getImgName()).delete();
                                    new PictureHandle(context).deletePicture(timeline.getImgName());
                                }
                                dbRef.child(TIMELINE).child(timeline.getKey()).removeValue();
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
        return timelines.size();
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
