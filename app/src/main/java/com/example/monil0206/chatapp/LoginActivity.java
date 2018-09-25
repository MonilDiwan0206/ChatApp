package com.example.monil0206.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private Toolbar lToolBar;
    private TextInputLayout mEmail;
    private TextInputLayout mPass;
    private Button loginBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar
        lToolBar = (Toolbar) findViewById(R.id.lToolBar);
        setSupportActionBar(lToolBar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mEmail = (TextInputLayout) findViewById(R.id.mEmail);
        mPass = (TextInputLayout) findViewById(R.id.mPass);
        mAuth = FirebaseAuth.getInstance();
        loginBtn = (Button) findViewById(R.id.loginBtn);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Logging In....");
        mProgress.setCanceledOnTouchOutside(false);

        //Database Reference
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");





        //Login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.show();
                String email = mEmail.getEditText().getText().toString();
                String pass = mPass.getEditText().getText().toString();

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mRef.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                    mProgress.dismiss();

                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid user Details", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    }
                });
            }
        });



    }

}
