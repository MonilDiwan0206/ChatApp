package com.example.monil0206.chatapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_friends, container, false);

        //Recycler View Reference
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.mFriendsList);

        //FirebaseAuth Reference
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        //Friends Database Reference
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        String online = dataSnapshot.child("online").getValue().toString();
                        viewHolder.setName(name);
                        viewHolder.setImage(image, getContext());
                        viewHolder.setStatus(online);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(i == 0){
                                            Intent profileIntent =new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("User_Id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(i==1){
                                            Intent chatIntent =new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("User_Id",list_user_id);
                                            chatIntent.putExtra("name",name);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        mFriendsList.setAdapter(friendsRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date){
            TextView userNameView = (TextView) mView.findViewById(R.id.status);
            userNameView.setText(date);

        }
        public void setName(String name){
            TextView userName = (TextView) mView.findViewById(R.id.dispName);
            userName.setText(name);
        }
        public void setImage(String image, Context ctx){
            CircleImageView pImage = (CircleImageView) mView.findViewById(R.id.pImage);
            Picasso.with(ctx).load(image).placeholder(R.mipmap.defaultprofile).into(pImage);
        }
        public void setStatus(String status){
            CircleImageView onlineImage = (CircleImageView) mView.findViewById(R.id.onlineImage);
            if(status.equals("true")){
                onlineImage.setVisibility(View.VISIBLE);
            } else {
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }
    }
}
