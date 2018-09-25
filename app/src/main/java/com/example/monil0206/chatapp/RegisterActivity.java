package com.example.monil0206.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar rToolBar;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button subBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private TextInputLayout mName;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //ActionBar
        rToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.rToolBar);
        setSupportActionBar(rToolBar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mEmail = (TextInputLayout) findViewById(R.id.mEmail);
        mPassword = (TextInputLayout) findViewById(R.id.mPass);
        mName = (TextInputLayout) findViewById(R.id.mName);
        subBtn = (Button) findViewById(R.id.subBtn);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please wait while we create your Account");
        mProgress.setCanceledOnTouchOutside(false);



        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mName.getEditText().getText().toString();
                final String email = mEmail.getEditText().getText().toString();
                final String pass = mPassword.getEditText().getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)){
                    Toast.makeText(RegisterActivity.this, "Empty Fields", Toast.LENGTH_SHORT).show();

                } else {
                    mProgress.show();
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){


                                String current_user_id = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                //Database Reference
                                mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);


                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("name",name);
                                userMap.put("status","Hi there! I am using Lapit Chat app");
                                userMap.put("image","default");
                                userMap.put("device_token",deviceToken);


                                mRef.setValue(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                        mProgress.dismiss();
                                    }
                                });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Error Logging In", Toast.LENGTH_LONG).show();
                                mProgress.dismiss();
                            }

                        }
                    });

                }


            }
        });

    }
}
