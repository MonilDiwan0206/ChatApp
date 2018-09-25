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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private Toolbar sToolbar;
    private TextInputLayout status;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private Button sBtn;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Progress Dialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Changing Status...");

        //Actionbar
        sToolbar = (Toolbar) findViewById(R.id.sToolBar);
        setSupportActionBar(sToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();

        //Database
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //Status Text
        status = (TextInputLayout) findViewById(R.id.status);



        //Button
        sBtn = (Button) findViewById(R.id.saveBtn);
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.show();
                String nStatus = status.getEditText().getText().toString();
                mRef.child("status").setValue(nStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                            Intent set = new Intent(StatusActivity.this,SetupActivity.class);
                            startActivity(set);
                            finish();
                        } else {
                            Toast.makeText(StatusActivity.this, "Error Changing Status", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }
}
