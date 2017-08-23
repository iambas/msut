package com.darker.motorservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.activity.ChatActivity;
import com.darker.motorservice.activity.DetailActivity;
import com.darker.motorservice.assets.MyImage;
import com.darker.motorservice.data.Services;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.darker.motorservice.data.Constant.CHAT_WITH_ID;
import static com.darker.motorservice.data.Constant.CHAT_WITH_NAME;
import static com.darker.motorservice.data.Constant.ID;
import static com.darker.motorservice.data.Constant.KEY_CHAT;
import static com.darker.motorservice.data.Constant.PHOTO;
import static com.darker.motorservice.data.Constant.STATUS;
import static com.darker.motorservice.data.Constant.TEL_NUM;
import static com.darker.motorservice.data.Constant.USER;

public class MotorcycleAdapter extends RecyclerView.Adapter<MotorcycleAdapter.ViewHolder> {

    private List<Services> motors;
    private Context context;

    public MotorcycleAdapter(Context context, List<Services> motors) {
        this.context = context;
        this.motors = motors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Services services = motors.get(position);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(services.getId());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean mStatus = (boolean) dataSnapshot.getValue();
                if (mStatus)
                    holder.online.setBackgroundResource(R.drawable.ic_cycle);
                else
                    holder.online.setBackgroundResource(R.color.white);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        holder.txtName.setText(services.getName());
        holder.txtMessage.setText(services.getPos());
        Bitmap bitmap;
        try{
            bitmap = new MyImage().convertToBitmap(services.getImgProfile());
        }catch (Exception e){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
        }
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(ID, services.getId());
                view.getContext().startActivity(intent);
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra(KEY_CHAT, "");
                intent.putExtra(CHAT_WITH_ID, services.getId());
                intent.putExtra(CHAT_WITH_NAME, services.getName());
                intent.putExtra(STATUS, USER);
                intent.putExtra(TEL_NUM, services.getTel());
                intent.putExtra(PHOTO, services.getPhoto());
                view.getContext().startActivity(intent);
            }
        });

        holder.timeView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return motors.size();
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
