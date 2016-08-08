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
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    private TextView termns;
    private EditText name, mail, id_number,phone,place, password,re_password;
    //private Spinner gender;
    private Button signup;
    private CheckBox agree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        termns=(TextView)findViewById(R.id.termns);
        name=(EditText)findViewById(R.id.name);
        mail=(EditText)findViewById(R.id.mail);
        id_number=(EditText)findViewById(R.id.id_number);
        phone=(EditText)findViewById(R.id.phone);
        place=(EditText)findViewById(R.id.place);
        password=(EditText)findViewById(R.id.pass);
        re_password=(EditText)findViewById(R.id.re_pass);
        //gender=(Spinner)findViewById(R.id.genderr);
        signup=(Button)findViewById(R.id.button);
        agree=(CheckBox)findViewById(R.id.agree);

        //Click on termns and conditions, link to pdf
        termns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.adobe.com/devnet/acrobat/pdfs/pdf_open_parameters.pdf";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        //Set adapter for spinner, fill the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //gender.setAdapter(adapter);

        //Click Sign up button
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nam=name.getText().toString();
                String mai=mail.getText().toString();
                String id_n=id_number.getText().toString();
                String phon=phone.getText().toString();
                String plac= place.getText().toString();
                String pass=password.getText().toString();
                String repass= re_password.getText().toString();
                //String gende=gender.getSelectedItem().toString();
                boolean doAgree=agree.isChecked();

                if(!nam.equals("") && !mai.equals("") && !id_n.equals("") && !phon.equals("") && !plac.equals("") &&
                        !pass.equals("") && !repass.equals("")){// && !gende.equals("Género")){
                    if(pass.equals(repass)){
                        if(doAgree==true){
                            //Create user in DB
                        }else{
                            String agreementError=getResources().getString(R.string.agreement_error);
                            Toast.makeText(getApplicationContext(),agreementError,Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        String noMatchError=getResources().getString(R.string.no_match_password_error);
                        Toast.makeText(getApplicationContext(),noMatchError,Toast.LENGTH_SHORT).show();
                    }

                }else{
                    String incompleteInfoError=getResources().getString(R.string.incomplete_info_error);
                    Toast.makeText(getApplicationContext(),incompleteInfoError,Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

}
