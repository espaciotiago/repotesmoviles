package com.ufo.ufomobile.reportesmoviles;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import utilities.DBHelper;
import utilities.User;

public class LoginActivity extends AppCompatActivity {
    private EditText mail,password;
    private TextView signup;
    private Button login;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        //--------------------------------------------------------------------------------------------
        db=new DBHelper(this);

        mail=(EditText)findViewById(R.id.mail);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mai=mail.getText().toString();
                String pass=password.getText().toString();
                if(!mai.equals("") && !pass.equals("")){
                    //TO DO Autenticate user http
                    User u = new User("Santiago Moreno Benavides","msantim@hotmail.com",
                                        "santiago29","3016929622","1144072657","Cali-Colombia",
                                        "Masculino","");
                    db.createUser(u);
                    Intent goToMenu = new Intent(LoginActivity.this,MenuActivity.class);
                    startActivity(goToMenu);
                    finish();
                }else{
                    String incompleteInfoError=getResources().getString(R.string.incomplete_info_error);
                    Toast.makeText(getApplicationContext(),incompleteInfoError,Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup=(TextView)findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToSignUp = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(goToSignUp);
            }
        });

    }

}
