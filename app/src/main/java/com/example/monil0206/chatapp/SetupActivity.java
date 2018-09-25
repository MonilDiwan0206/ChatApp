package com.example.monil0206.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private Toolbar sToolBar;
    private TextView mName;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private Button statusBtn;
    private FirebaseAuth mImageAuth;
    private TextView statusText;
    private CircleImageView profileImage;
    private StorageReference mThumbs;
    private StorageReference mStorage;
    private Button imgBtn;
    private DatabaseReference mImageStorage;
    private static final int GALLERY_PICK = 1;
    private ProgressDialog mProgress;
    private DatabaseReference rootref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Storage Reference for thumbs
        mThumbs = FirebaseStorage.getInstance().getReference();

        //Progress Dialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Changing Profile Picture...");
        mProgress.setCanceledOnTouchOutside(false);

        //Firebase Storage Reference
        mStorage = FirebaseStorage.getInstance().getReference();

        //ActionBar
        sToolBar = (Toolbar) findViewById(R.id.sToolBar);
        setSupportActionBar(sToolBar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rootref = FirebaseDatabase.getInstance().getReference();

        //Change Status Button
        statusBtn = (Button) findViewById(R.id.statusBtn);
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statusIntent = new Intent(SetupActivity.this,StatusActivity.class);
                startActivity(statusIntent);
            }
        });


        //Circle Image View
        profileImage = (CircleImageView) findViewById(R.id.profileImage);

        //Change Profile Picture Button
        imgBtn = (Button) findViewById(R.id.imgBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            }
        });

        statusText = (TextView) findViewById(R.id.statusText);
        mName = (TextView) findViewById(R.id.name);
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();
        if (uid != null) {
            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            mRef.keepSynced(true);
        }
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                mName.setText(name);
                String status = dataSnapshot.child("status").getValue().toString();
                statusText.setText(status);
                final String image = dataSnapshot.child("image").getValue().toString();
                if(!image.equals("Default")){
                    Picasso.with(SetupActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.defaultprofile).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SetupActivity.this).load(image).placeholder(R.mipmap.defaultprofile).into(profileImage);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            String currentUser = mAuth.getUid();
            mImageStorage = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);

            mProgress.show();
            Uri imageUri = data.getData();


            StorageReference filepath = mStorage.child("Profile_Images").child(currentUser + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String downloadUrl = task.getResult().getDownloadUrl().toString();
                        mImageStorage.child("image").setValue(downloadUrl);
                        mProgress.dismiss();
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(SetupActivity.this, "Error Updating Profile Picture..", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference mRef = rootref.child("Users").child(mAuth.getCurrentUser().getUid());
        mRef.child("online").setValue("true");
    }


}

