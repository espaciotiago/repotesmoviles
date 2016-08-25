package com.ufo.ufomobile.reportesmoviles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import utilities.User;

public class ProfileActivity extends AppCompatActivity implements EditProfileDialogFragment.OnaAddSelected{

    private TextView name,mail,idNumber,phone,place;
    private ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
<<<<<<< HEAD
<<<<<<< HEAD
        //----------------------------------------------------------------------------------------------
=======
        //--------------------------------------------------------------------------------------------
>>>>>>> master
=======
        //------------------------------------------------------------------------------------------
        name=(TextView)findViewById(R.id.name);
        mail=(TextView)findViewById(R.id.mail);
        phone=(TextView)findViewById(R.id.phone);
        idNumber=(TextView)findViewById(R.id.id_number);
        place=(TextView)findViewById(R.id.place);
        picture=(ImageView)findViewById(R.id.pic);
>>>>>>> master

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            String nam=name.getText().toString();
            String idNum=idNumber.getText().toString();
            String pho=phone.getText().toString();
            String plc=place.getText().toString();

            EditProfileDialogFragment newFragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            newFragment = EditProfileDialogFragment.newInstance(nam,idNum,pho,plc,"");
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArticleSelectedListener(String name, String idNum, String phone, String place, String newPicture) {
        this.name.setText(name);
        this.idNumber.setText(idNum);
        this.phone.setText(phone);
        this.place.setText(place);

        if(!newPicture.equals("") && newPicture!=null){
            Bitmap imag = null;
            byte[] decodedString = Base64.decode(newPicture, Base64.DEFAULT);
            imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(imag);
        }

        String mai=mail.getText().toString();

        //TO DO Set bd
        //TO DO Update user http
    }
}
