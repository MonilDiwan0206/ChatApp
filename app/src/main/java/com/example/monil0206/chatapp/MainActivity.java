package com.example.monil0206.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolBar;
    private FirebaseAuth mAuth;
    private TabLayout mTabLayout;
    private ViewPager mPager;
    private SectionPagerAdapter mPagerAdapter;
    private DatabaseReference mRef;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Check Internet Connection
        if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();



        //ActionBar
        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.mToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chat App");

        //FirebaseAuth
        mAuth = FirebaseAuth.getInstance();


        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mPager = (ViewPager) findViewById(R.id.mPager);

        //Database
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        String uid = mAuth.getUid();


        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mTabLayout.setupWithViewPager(mPager);

        if(mAuth.getCurrentUser() != null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
            startActivity(startIntent);
            finish();
        } else {
            mUserDatabase.child("online").setValue("true");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
            startActivity(startIntent);
            finish();
        } else if(item.getItemId() == R.id.settings){
            Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(settingsIntent);
        } else if(item.getItemId() == R.id.aUsers){
            Intent users = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(users);
        }

        return true;
    }

    public boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if(netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        }
        return false;
    }
    public AlertDialog.Builder buildDialog(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please make sure your mobile data or wifi is on to proceed....");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent mainIntent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
        return builder;
    }
}

