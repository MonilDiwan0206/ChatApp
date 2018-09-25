package com.example.monil0206.chatapp;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar cToolBar;
    private String user_id;
    private String user_name;
    private TextView dName;
    private TextView lastSeen;
    private CircleImageView customImage;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    private EditText chatMessage;
    private ImageButton addBtn;
    private ImageButton sendBtn;
    private RecyclerView mMessageList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefresh;
    private static int Total_items_to_load = 10;
    private int mCurrentPage = 1;
    private int item_pos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user_id = getIntent().getStringExtra("User_Id");
        user_name = getIntent().getStringExtra("name");

        rootRef = FirebaseDatabase.getInstance().getReference();

        chatMessage = (EditText) findViewById(R.id.chatMessage);
        addBtn = (ImageButton) findViewById(R.id.addBtn);
        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        if(mAuth != null) {
            mCurrentUser = mAuth.getCurrentUser().getUid();
        }

        mMessageList = (RecyclerView) findViewById(R.id.mMessageList);
        mLinearLayout  = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mAdapter = new MessageAdapter(messageList);
        mMessageList.setAdapter(mAdapter);
        loadMessages();

        mRefresh = (SwipeRefreshLayout) findViewById(R.id.mRefresh);

        cToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.cToolBar);
        setSupportActionBar(cToolBar);

        ActionBar actionBar = getSupportActionBar();


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setTitle(user_name);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        //------Action bar items-----

        dName = (TextView) findViewById(R.id.dispName);
        lastSeen = (TextView) findViewById(R.id.lastSeen);
        customImage = (CircleImageView) findViewById(R.id.customImage);
        dName.setText(user_name);

        rootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){

                    lastSeen.setText("Online");
                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastseentime = GetTimeAgo.getTimeAgo(lastTime,getApplicationContext());

                    lastSeen.setText(lastseentime);
                }
                Picasso.with(ChatActivity.this).load(image).placeholder(R.mipmap.defaultprofile).into(customImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(user_id)){

                    Map ChatAddMap = new HashMap();
                    ChatAddMap.put("seen",false);
                    ChatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map ChatUserMap = new HashMap();
                    ChatUserMap.put("Chat/" + mCurrentUser + "/" + user_id,ChatAddMap);
                    ChatUserMap.put("Chat/" + user_id + "/" + mCurrentUser,ChatAddMap);

                    rootRef.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat_Log",databaseError.getMessage().toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                item_pos = 0;
                loadMoreMessages();

            }
        });
    }

    private void loadMoreMessages(){
        DatabaseReference messageRef = rootRef.child("messages").child(mCurrentUser).child(user_id);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messageList.add(item_pos++,message);
                } else {
                    mPrevKey = mLastKey;
                }

                if(item_pos == 1){
                    mLastKey = messageKey;

                }



                Log.d("TOTALKEYS","Last Key : " + mLastKey + "|Prev Key " + mPrevKey + "|Current Key" + messageKey);

                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messageList.size() - 1);
                mRefresh.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(mCurrentUser).child(user_id);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * Total_items_to_load);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                item_pos++;

                if(item_pos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messageList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messageList.size() - 1);
                mRefresh.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String message = chatMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + mCurrentUser + "/" + user_id;
            String chat_user_ref = "messages/" + user_id + "/" + mCurrentUser;

            DatabaseReference user_message_push = rootRef.child("messages").child(mCurrentUser).child(user_id).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUser);
            chatMessage.setText("");

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }
                }
            });

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageuri = data.getData();

            final String currentUser = "messages/" + mCurrentUser + "/" + user_id;
            final String chatUser = "messages/" + user_id + "/" + mCurrentUser;

            DatabaseReference user_message_push = rootRef.child("messages").child(mCurrentUser).child(user_id).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("messages_images").child(push_id + ".jpg");
            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUser);
                        chatMessage.setText("");

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUser + "/" + push_id,messageMap);
                        messageUserMap.put(chatUser + "/" + push_id,messageMap);

                        rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                }
                            }
                        });
                    }

                }
            });

        }
    }
}
