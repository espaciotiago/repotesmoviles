package com.ufo.ufomobile.reportesmoviles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.User;

public class ProfileActivity extends AppCompatActivity implements EditProfileDialogFragment.OnaEditProfileSelected{

    private TextView name,mail,idNumber,phone,txtReports,txtSupports,txtComments;
    private ImageView picture;

    private DBHelper db;
    private User user;
    private JSONObject imgJson=null;
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        //final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        //Database ---------------------------------------------------------------------------------
        db=new DBHelper(this);
        user = db.userExists();
        Log.e("PAS",user.toString());
        //------------------------------------------------------------------------------------------
        name=(TextView)findViewById(R.id.name);
        mail=(TextView)findViewById(R.id.mail);
        phone=(TextView)findViewById(R.id.phone);
        idNumber=(TextView)findViewById(R.id.id_number);
        txtReports=(TextView)findViewById(R.id.reports_number_txt);
        txtSupports=(TextView)findViewById(R.id.likes_number_txt);
        txtComments=(TextView)findViewById(R.id.comments_number_txt);
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

        //Update stats with info
        new StatsAsyncTask(jsonIdUsuario(user.getId()),Constants.URL_GET_USER_STATS,ProfileActivity.this).execute();

    }

    /**
     * Prepare the user id in a json object
     * @param userId
     * @return
     */
    private JSONObject jsonIdUsuario(String userId){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.USER_EXTRA,userId);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
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
            EditProfileDialogFragment dialog = EditProfileDialogFragment.newInstance(nam,idNum,pho,user.getImage(),user.getPassword());
            dialog.show(fm, "dialog");
        }
        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditProfileSelectedListener(String name, String idNum, String phone, String newPicture,String newPassword) {
        this.name.setText(name);
        this.idNumber.setText(idNum);
        this.phone.setText(phone);

        Bitmap imag = null;
        String picName=null;
        if(newPicture!=null && !newPicture.equals("")){
            //Update the image in the moment
            byte[] decodedString = Base64.decode(newPicture, Base64.DEFAULT);
            imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(imag);

            //Prepare the info to send the image
            picName = Constants.giverImageName(Constants.IMG_CONS_USER);
            imgJson = jsonUserImage(picName,newPicture);
        }
        String mai=user.getMail();
        String idNumb = user.getId();

        // TODO: 20/09/16 UPDATE BD USER (LOCALE, REMOTE)
        JSONObject jsonObject = jsonUpdateUser(idNum,name,phone,newPassword,picName);
        new UpdateUserAsyncTask(jsonObject,Constants.URL_UPDATE_USER,ProfileActivity.this).execute();
    }

    /**
     * Creates a json object with the image to send
     * @return
     */
    private JSONObject jsonUserImage(String nameImg,String image){
        HashMap<String,String> imageMap = new HashMap<>();
        imageMap.put(Constants.NAME_EXTRA,nameImg);
        imageMap.put(Constants.IMAGE_EXTRA,image);

        JSONObject jsonObject = new JSONObject(imageMap);
        return jsonObject;
    }

    /**
     * Prepare the info to update about the user
     * @param userId
     * @param name
     * @param phone
     * @param password
     * @param imgName
     * @return
     */
    private JSONObject jsonUpdateUser(String userId,String name,String phone,String password,String imgName){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.ID_EXTRA,userId);
        map.put(Constants.NAME_EXTRA,name);
        map.put(Constants.PHONE_EXTRA,phone);
        map.put(Constants.PASSWORD_EXTRA,password);
        map.put(Constants.IMAGE_EXTRA,imgName);

        JSONObject jsonObject = new JSONObject(map);
        Log.e("JSON UPD",jsonObject.toString());
        return jsonObject;
    }

    //----------------------------------------------------------------------------------------------
    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class StatsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
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
        public StatsAsyncTask(JSONObject data,String url,Context context){
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
            progressBar.setMessage(context.getString(R.string.getting_data));
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
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                        //Get the user info
                        int reportsN = jsonObject.getInt(Constants.REPORT_EXTRA);
                        int supportsN = jsonObject.getInt(Constants.SUPPORTS_EXTRA);
                        int commentN = jsonObject.getInt(Constants.COMMENT_EXTRA);

                        txtReports.setText(reportsN+"");
                        txtSupports.setText(supportsN+"");
                        txtComments.setText(commentN+"");

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(ProfileActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            progressBar.dismiss();
        }
    }
    //----------------------------------------------------------------------------------------------
    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class UpdateUserAsyncTask extends AsyncTask<Void, Void, JSONObject> {
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
        public UpdateUserAsyncTask(JSONObject data,String url,Context context){
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
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                        // TODO: 28/12/16 Update the internal bd
                        String name = this.data.getString(Constants.NAME_EXTRA);
                        String phone = this.data.getString(Constants.PHONE_EXTRA);
                        String pass = this.data.getString(Constants.PASSWORD_EXTRA);
                        db.updateUser(name,user.getId(),phone,null,user.getMail(),pass);
                        if(imgJson!=null){
                            //Send the image to the server
                            new ImageupAsyncTask(imgJson,Constants.URL_USER_IMAGE,getApplicationContext()).execute();
                        }else{
                            progressBar.dismiss();
                        }

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(ProfileActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
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
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                        String image = data.getString(Constants.IMAGE_EXTRA);
                        db.updateUser(null,user.getId(),null,image,null,null);
                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(ProfileActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(ProfileActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            progressBar.dismiss();
        }
    }
}
