package com.ufo.ufomobile.reportesmoviles;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.User;

public class LoginActivity extends AppCompatActivity {
    private EditText mail,password;
    private TextView signup;
    private Button login;
    DBHelper db;
    ProgressDialog progressBar;

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
                    //Send the info to the server
                    JSONObject loginJson = createLoginJson(mai,pass);
                    new LoginAsyncTask(loginJson,Constants.URL_LOGIN,LoginActivity.this).execute();
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

        permisosCamara();
        permisosGPS();

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
     * Creates a jsonobject with the login info
     * @param mail
     * @param password
     * @return
     */
    private JSONObject createLoginJson(String mail,String password){
        HashMap<String,String> loginMap = new HashMap<>();
        loginMap.put(Constants.MAIL_EXTRA,mail);
        loginMap.put(Constants.PASSWORD_EXTRA,password);

        JSONObject jsonObject = new JSONObject(loginMap);
        Log.d("json login",jsonObject.toString());
        return jsonObject;
    }

    //----------------------------------------------------------------------------------------------
    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class LoginAsyncTask extends AsyncTask<Void, Void, JSONObject> {
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
        public LoginAsyncTask(JSONObject data,String url,Context context){
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
                        Toast.makeText(LoginActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                        //Get the user info
                        JSONObject jsonUser = jsonObject.getJSONObject(Constants.USER_EXTRA);
                        //Get the image info
                        String imageStr = jsonObject.getString(Constants.IMAGE_EXTRA);
                        //Creates the user in the db
                        User u = new User(jsonUser.getString(Constants.NAME_EXTRA),
                                jsonUser.getString(Constants.MAIL_EXTRA),
                                jsonUser.getString(Constants.PASSWORD_EXTRA),
                                jsonUser.getString(Constants.PHONE_EXTRA),
                                jsonUser.getString(Constants.ID_NUMBER_EXTRA),
                                jsonUser.getString(Constants.GENDER_EXTRA),
                                imageStr);
                        db.createUser(u);
                        Intent goToMenu = new Intent(LoginActivity.this,MenuActivity.class);
                        startActivity(goToMenu);
                        finish();

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(LoginActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(LoginActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            progressBar.dismiss();
        }
    }

    // PERMISOS ------------------------------------------------------------------------------------
    private void permisosGPS() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // PoneGPS
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISO_GPS);
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISO_GPS);
            }
        }
    }

    private void permisosCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, Constants.PERMISO_CAMARA);
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISO_CAMARA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISO_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PoneGPS
                } else {

                }
                return;
            }
            case Constants.PERMISO_CAMARA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    return;
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------

}
