package com.ufo.ufomobile.reportesmoviles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import utilities.Constants;
import utilities.JSONParser;
import utilities.User;

public class SignUpActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;

    private TextView termns;
    private EditText name, mail, id_number,phone, password,re_password;
    private ImageView picture;
    private String pictureString="";
    private String pictureName = "";
    //private Spinner gender;
    private Button signup;
    private CheckBox agree,male,female;
    private Bitmap bitmap;
    private Uri filePath;

    ProgressDialog progressBar;

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
                String pass=password.getText().toString();
                String repass= re_password.getText().toString();
                String gender="";
                if(male.isChecked()){
                    gender=getResources().getString(R.string.male_id);
                }else if(female.isChecked()){
                    gender=getResources().getString(R.string.female_id);
                }

                boolean doAgree=agree.isChecked();

                if(!nam.equals("") && !mai.equals("") && !id_n.equals("") && !phon.equals("") &&
                        !pass.equals("") && !repass.equals("") && !gender.equals("")){
                    if(pass.equals(repass)){
                        if(doAgree==true){
                            if(pictureString!=null && !pictureString.equals("")){
                                pictureName = Constants.giverImageName(Constants.IMG_CONS_USER);
                            }
                            User user = new User(nam,mai,pass,phon,id_n,gender,pictureName);
                            //Send the data to the server
                            JSONObject jsonUser = createUserJSONObject(user);
                            new SignupAsyncTask(jsonUser,Constants.URL_SIGNUP,SignUpActivity.this).execute();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the file chooser
     */
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Creates a json object with the info of the user
     * @param user
     * @return
     */
    private JSONObject createUserJSONObject(User user){
        //Creates the map with the user info
        HashMap<String,String> userMap = new HashMap<>();
        userMap.put(Constants.NAME_EXTRA,user.getName());
        userMap.put(Constants.MAIL_EXTRA,user.getMail());
        userMap.put(Constants.ID_NUMBER_EXTRA,user.getId());
        userMap.put(Constants.PHONE_EXTRA,user.getPhone());
        userMap.put(Constants.GENDER_EXTRA,user.getGender());
        userMap.put(Constants.PASSWORD_EXTRA,user.getPassword());
        userMap.put(Constants.IMAGE_EXTRA,user.getImage());
        userMap.put(Constants.DATE_EXTRA,Constants.getDateString(Constants.getActualDate()));

        JSONObject jsonObject = new JSONObject(userMap);
        Log.d("user json",jsonObject.toString());
        return jsonObject;
    }

    /**
     * Creates a json object with the image to send
     * @return
     */
    private JSONObject createImageJSONObject(){
        HashMap<String,String> imageMap = new HashMap<>();
        imageMap.put(Constants.NAME_EXTRA,pictureName);
        imageMap.put(Constants.IMAGE_EXTRA,pictureString);

        JSONObject jsonObject = new JSONObject(imageMap);
        return jsonObject;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap,240,240,false);
                int w=256;
                int h=256;
                Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bitmap,w,h);
                picture.setImageBitmap(bitmap2);
                pictureString= Constants.getStringImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class SignupAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        /**
         * Params
         */
        private JSONObject data;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        /**
         *
         * @param data
         * @param url
         * @param context
         */
        public SignupAsyncTask(JSONObject data,String url,Context context){
            this.data = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(context);
            progressBar.setCancelable(true);
            progressBar.setMessage(context.getString(R.string.sending_data));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject response;
            response = connectionHelper.sendPOST(data,url);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject!=null) {
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(SignUpActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                        if(pictureName!=null && !pictureName.equals("")){
                            //Send the image to server
                            JSONObject jsonImage = createImageJSONObject();
                            new ImageupAsyncTask(jsonImage,Constants.URL_USER_IMAGE,getApplicationContext()).execute();
                        }else {
                            progressBar.dismiss();
                            //Close the activity
                            Intent i = new Intent(SignUpActivity.this,LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }else {
                        progressBar.dismiss();
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(SignUpActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                progressBar.dismiss();
                // Show the error message
                Toast.makeText(SignUpActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            //progressBar.dismiss();
        }
    }

    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send image data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class ImageupAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        /**
         * Params
         */
        private JSONObject data;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        /**
         *
         * @param data
         * @param url
         * @param context
         */
        public ImageupAsyncTask(JSONObject data,String url,Context context){
            this.data = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject response;
            response = connectionHelper.sendPOST(data,url);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject!=null) {
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(SignUpActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                        //Close the activity
                        Intent i = new Intent(SignUpActivity.this,LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(SignUpActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(SignUpActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            progressBar.dismiss();
        }
    }

}
