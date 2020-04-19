package com.example.chatterbox.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatterbox.MessageActivity;
import com.example.chatterbox.Model.Chat;
import com.example.chatterbox.Model.User;
import com.example.chatterbox.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;
    private boolean isChat;

    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUser, boolean isChat){
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profileImage;
        public ImageView isOnline;
        public ImageView isOffline;
        public TextView lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            isOnline = itemView.findViewById(R.id.isOnline);
            isOffline = itemView.findViewById(R.id.isOffline);
            lastMessage = itemView.findViewById(R.id.lastMessage);

        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        String layoutViewName = view.getClass().getName();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUser.get(position);
        holder.username.setText(user.getUsername());

        if(isChat){
            lastMessage(user.getId(), holder.lastMessage);
        }else{
            holder.lastMessage.setVisibility(View.GONE);
        }

        if(isChat){
            if(user.getStatus().equals("online")){
                holder.isOnline.setVisibility(View.VISIBLE);
                holder.isOffline.setVisibility(View.GONE);

            }else{
                holder.isOnline.setVisibility(View.GONE);
                holder.isOffline.setVisibility(View.VISIBLE);
            }
        }else{
            holder.isOnline.setVisibility(View.GONE);
            holder.isOffline.setVisibility(View.GONE);
        }

        if(user.getImageUrl().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    private void lastMessage(final String userId, final TextView lastMessage){
        theLastMessage = "Default";

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if((chat.getReciever().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) || (chat.getReciever().equals(userId) && chat.getSender().equals(firebaseUser.getUid()))){
                        theLastMessage = chat.getMessage();
                    }
                }

                switch (theLastMessage){
                    case "Default":
                        lastMessage.setText("No Message");
                        break;

                    default:
                        lastMessage.setText(theLastMessage);
                        break;
                }

                theLastMessage = "Default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
