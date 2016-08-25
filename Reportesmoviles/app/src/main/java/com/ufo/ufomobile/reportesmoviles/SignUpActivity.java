package com.ufo.ufomobile.reportesmoviles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import utilities.User;

public class SignUpActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;

    private TextView termns;
    private EditText name, mail, id_number,phone,place, password,re_password;
    private ImageView picture;
    private String pictureString="";
    //private Spinner gender;
    private Button signup;
    private CheckBox agree,male,female;
    private Bitmap bitmap;
    private Uri filePath;

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
        male=(CheckBox)findViewById(R.id.male);
        female=(CheckBox)findViewById(R.id.female);

        picture=(ImageView)findViewById(R.id.pic);

        //Click on pictures
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

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
        male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    female.setChecked(false);
                }
            }
        });
        female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    male.setChecked(false);
                }
            }
        });


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
                String gender="";
                if(male.isChecked()){
                    gender=getResources().getString(R.string.male);
                }else if(female.isChecked()){
                    gender=getResources().getString(R.string.female);
                }

                boolean doAgree=agree.isChecked();

                if(!nam.equals("") && !mai.equals("") && !id_n.equals("") && !phon.equals("") && !plac.equals("") &&
                        !pass.equals("") && !repass.equals("") && !gender.equals("")){
                    if(pass.equals(repass)){
                        if(doAgree==true){
                            User user = new User(nam,mai,pass,phon,id_n,plac,gender,pictureString);
                            //TO DO Create user in DB
                            //TO DO Create user http
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

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap,240,240,false);
                picture.setImageBitmap(bitmap2);
                pictureString=getStringImage(bitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}
