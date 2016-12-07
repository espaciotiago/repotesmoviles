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

import utilities.DBHelper;
import utilities.User;

public class ProfileActivity extends AppCompatActivity implements EditProfileDialogFragment.OnaEditProfileSelected{

    private TextView name,mail,idNumber,phone;
    private ImageView picture;

    private DBHelper db;
    private User user;

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
        //Database ---------------------------------------------------------------------------------
        db=new DBHelper(this);
        user = db.userExists();
        //------------------------------------------------------------------------------------------
        name=(TextView)findViewById(R.id.name);
        mail=(TextView)findViewById(R.id.mail);
        phone=(TextView)findViewById(R.id.phone);
        idNumber=(TextView)findViewById(R.id.id_number);
        picture=(ImageView)findViewById(R.id.pic);

        name.setText(user.getName());
        mail.setText(user.getMail());
        phone.setText(user.getPhone());
        idNumber.setText(user.getId());
        if(user.getImage()!=null && !user.getImage().equals("")){
            byte[] decodedString = Base64.decode(user.getImage(), Base64.NO_PADDING);
            Bitmap imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(imag);

        }

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

            FragmentManager fm = getSupportFragmentManager();
            EditProfileDialogFragment dialog = EditProfileDialogFragment.newInstance(nam,idNum,pho,user.getImage());
            dialog.show(fm, "dialog");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditProfileSelectedListener(String name, String idNum, String phone, String newPicture) {
        this.name.setText(name);
        this.idNumber.setText(idNum);
        this.phone.setText(phone);

        Bitmap imag = null;
        if(!newPicture.equals("") && newPicture!=null){
            byte[] decodedString = Base64.decode(newPicture, Base64.DEFAULT);
            imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(imag);
        }
        String mai=mail.getText().toString();

        // TODO: 20/09/16 UPDATE BD USER (LOCALE, REMOTE)
    }
}
