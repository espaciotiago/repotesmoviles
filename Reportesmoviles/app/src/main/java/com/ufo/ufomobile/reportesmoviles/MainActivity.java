package com.ufo.ufomobile.reportesmoviles;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String a= null;
        if(a==null){
            Intent init=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(init);
            finish();
        }
    }
}
