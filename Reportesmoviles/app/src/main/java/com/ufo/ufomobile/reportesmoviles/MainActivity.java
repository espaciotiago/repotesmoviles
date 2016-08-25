package com.ufo.ufomobile.reportesmoviles;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import utilities.DBHelper;
import utilities.User;

public class MainActivity extends AppCompatActivity {
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db=new DBHelper(this);
        User user= db.userExists();
        if(user==null){
            Intent init=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(init);
            finish();
        }else{
            Intent init=new Intent(MainActivity.this,MenuActivity.class);
            startActivity(init);
            finish();
        }
    }
}
