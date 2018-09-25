package com.example.monil0206.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar uToolBar;
    private RecyclerView mUsers;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();

        //ActionBar
        uToolBar = (Toolbar) findViewById(R.id.uToolBar);
        setSupportActionBar(uToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Recycler View
        mUsers = (RecyclerView) findViewById(R.id.mUsers);
        mUsers.setHasFixedSize(true);
        mUsers.setLayoutManager(new LinearLayoutManager(this));

        //DatabaseReference
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    @Override
    protected void onStart() {
        super.onStart();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUsersRef.child("online").setValue(true);

        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UserViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Users model, int position) {
                viewHolder.setDisplayName(model.getName());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.setProfileImage(model.getImage(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("User_Id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mUsers.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDisplayName(String name){
            TextView dName = (TextView) mView.findViewById(R.id.dispName);
            dName.setText(name);
        }
        public void setUserStatus(String Status){
            TextView sName = (TextView) mView.findViewById(R.id.status);
            sName.setText(Status);
        }
        public void setProfileImage(String Image, Context ctx){
            CircleImageView sView = (CircleImageView) mView.findViewById(R.id.pImage);
            if(!Image.equals("Default")){
                Picasso.with(ctx).load(Image).placeholder(R.mipmap.defaultprofile).into(sView);
            }
        }
    }
}
