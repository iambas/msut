package com.darker.motorservice.ui.main.adapter;

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
import com.darker.motorservice.ui.chat.ChatActivity;
import com.darker.motorservice.ui.details.DetailActivity;
import com.darker.motorservice.utility.ImageUtil;
import com.darker.motorservice.model.ServicesItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_ID;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.CHAT_WITH_NAME;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.ID;
import static com.darker.motorservice.utility.Constant.KEY_CHAT;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.PHOTO;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.sharedpreferences.SharedPreferencesUtil.TEL_NUM;
import static com.darker.motorservice.utility.Constant.USER;

public class MotorcycleAdapter extends RecyclerView.Adapter<MotorcycleAdapter.ViewHolder> {

    private List<ServicesItem> motors;
    private Context context;

    public MotorcycleAdapter(Context context, List<ServicesItem> motors) {
        this.context = context;
        this.motors = motors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ServicesItem servicesItem = motors.get(position);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(STATUS).child(servicesItem.getId());
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

        holder.txtName.setText(servicesItem.getName());
        holder.txtMessage.setText(servicesItem.getPos());
        Bitmap bitmap;
        try{
            bitmap = ImageUtil.convertByteToBitmap(servicesItem.getImgProfile());
        }catch (Exception e){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pro);
        }
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra(ID, servicesItem.getId());
                view.getContext().startActivity(intent);
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra(KEY_CHAT, "");
                intent.putExtra(CHAT_WITH_ID, servicesItem.getId());
                intent.putExtra(CHAT_WITH_NAME, servicesItem.getName());
                intent.putExtra(STATUS, USER);
                intent.putExtra(TEL_NUM, servicesItem.getTel());
                intent.putExtra(PHOTO, servicesItem.getPhoto());
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
