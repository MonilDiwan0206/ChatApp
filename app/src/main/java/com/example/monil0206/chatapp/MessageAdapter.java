package com.example.monil0206.chatapp;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private DatabaseReference mUsersDatabase;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView display_name;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageText);

            display_name = (TextView) itemView.findViewById(R.id.display_name);
            messageImage = (ImageView) itemView.findViewById(R.id.sendImage);


        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        if(mAuth != null) {
            current_user_id = mAuth.getCurrentUser().getUid();
        }
        Messages c = mMessageList.get(position);


        String from_user = c.getFrom();
        String message_type = c.getType();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                holder.display_name.setText(name);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setVisibility(View.VISIBLE);

            Picasso.with(holder.messageImage.getContext()).load(c.getMessage())
                    .placeholder(R.mipmap.defaultprofile).into(holder.messageImage);

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
