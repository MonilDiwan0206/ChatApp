package com.example.monil0206.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.data.DataBufferObserverSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar pToolbar;
    private DatabaseReference mDatabase;
    private ImageView profileImage;
    private TextView displayName;
    private TextView mstatus;
    private Button sendRequest, declineReq;
    private String current_status;
    private DatabaseReference mRequests;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriends;
    private DatabaseReference mNotifications;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String from_user_id = getIntent().getStringExtra("User_Id");
        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();

        //Database Reference for Requests
        mRequests = FirebaseDatabase.getInstance().getReference().child("Requests");

        //Database Reference for Friends
        mFriends = FirebaseDatabase.getInstance().getReference().child("Friends");

        //Database Reference for Notifications
        mNotifications = FirebaseDatabase.getInstance().getReference().child("notifications");

        //Firebase Auth
          mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();



        //Firebase Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user_id);

        //References for Image and Text View
        displayName = (TextView) findViewById(R.id.displayName);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        mstatus = (TextView) findViewById(R.id.mstatus);


        //Action Bar
        pToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.pToolbar);

        //Friend Request Button, Decline Request Button
        sendRequest = (Button) findViewById(R.id.sendRequest);
        declineReq = (Button) findViewById(R.id.declineReq);

        declineReq.setVisibility(View.INVISIBLE);
        declineReq.setEnabled(false);

        current_status = "not_friends";



        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                setSupportActionBar(pToolbar);
                getSupportActionBar().setTitle(" ");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                displayName.setText(display_name);
                mstatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.mipmap.defaultprofile).into(profileImage);

                mRequests.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(from_user_id)){
                            String request = (String) dataSnapshot.child(from_user_id).child("request_type").getValue();

                            if(request.equals("received")){
                                current_status = "request_received";
                                sendRequest.setText("Accept Friend Request");

                                declineReq.setVisibility(View.VISIBLE);
                                declineReq.setEnabled(true);
                            } else if(request.equals("sent")){
                                current_status = "request_sent";
                                sendRequest.setText("Cancel Friend Request");

                                declineReq.setVisibility(View.INVISIBLE);
                                declineReq.setEnabled(false);
                            }
                        } else {
                            mFriends.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(from_user_id)){
                                        current_status = "friends";
                                        sendRequest.setText("Unfriend");

                                        declineReq.setVisibility(View.INVISIBLE);
                                        declineReq.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Send Request Button On Click Listener
        //----------Not Friends State---------
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest.setEnabled(false);
                if(current_status.equals("not_friends")){

                    DatabaseReference newNotificationref = rootRef.child("notifications").child(from_user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notifData = new HashMap<>();
                    notifData.put("from",mCurrentUser.getUid());
                    notifData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Requests/" + mCurrentUser.getUid() + "/" + from_user_id + "/request_type","sent");
                    requestMap.put("Requests/" + from_user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("notifications/" + from_user_id + "/" + newNotificationId, notifData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                            sendRequest.setEnabled(true);
                            current_status = "request_sent";
                            sendRequest.setText("Cancel Friend Request");
                        }
                    });
                }
                //-------Cancel Friend Request-------
                if(current_status.equals("request_sent")){
                    Map cancelRequest = new HashMap();
                    cancelRequest.put("Requests/" + mCurrentUser.getUid() + "/" + from_user_id,null);
                    cancelRequest.put("Requests/" + from_user_id + "/" + mCurrentUser.getUid(),null);

                    rootRef.updateChildren(cancelRequest, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                current_status = "not_friends";
                                sendRequest.setText("Send Friend Request");

                                declineReq.setEnabled(false);
                                declineReq.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                            sendRequest.setEnabled(true);
                        }
                    });
                }

                //------Received Friend Request------

                if(current_status.equals("request_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + from_user_id + "/date",currentDate);
                    friendsMap.put("Friends/" + from_user_id + "/" + mCurrentUser.getUid() + "/date",currentDate);

                    friendsMap.put("Requests/" + mCurrentUser.getUid() + "/" + from_user_id,null);
                    friendsMap.put("Requests/" + from_user_id + "/" + mCurrentUser.getUid(),null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                sendRequest.setEnabled(true);
                                current_status = "friends";
                                sendRequest.setText("Unfriend");
                                declineReq.setVisibility(View.INVISIBLE);
                                declineReq.setEnabled(false);
                            }

                        }
                    });
                }

                //------Unfriend------
                if(current_status.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + from_user_id,null);
                    unfriendMap.put("Friends/" + from_user_id + "/" + mCurrentUser.getUid(),null);

                    rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                current_status = "not_friends";
                                sendRequest.setText("Send Friend Request");

                                declineReq.setEnabled(false);
                                declineReq.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                            sendRequest.setEnabled(true);

                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference mRef = rootRef.child("Users").child(mAuth.getCurrentUser().getUid());
        mRef.child("online").setValue("true");

    }


}
