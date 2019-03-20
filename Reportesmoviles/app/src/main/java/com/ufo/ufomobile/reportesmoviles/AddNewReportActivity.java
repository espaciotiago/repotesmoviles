package com.ufo.ufomobile.reportesmoviles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.Report;
import utilities.User;

public class AddNewReportActivity extends AppCompatActivity implements AddressPickerDialogFragment.OnaAddSelected {

    /**
     * UI ELEMENTS
     */
    private ImageView categoryImg;
    private ImageButton addImage;
    private EditText title,description,address;
    private Button send;
    private ScrollView scrollView;
    private ImageButton mapview;
    private String categoryName;
    private RecyclerView myList;
    private Recycler_View_Adapter adapter;

    /**
     * LOGICAL ELEMENTS
     */
    private int categoryResource;
    private double latitude,longitude;
    private List<Bitmap> data;
    private ArrayList<String> images = new ArrayList<String>();
    private ArrayList<String> imagesName = new ArrayList<String>();
    private int total_imgs;
    private DBHelper db;
    private User user;
    ProgressDialog progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Database ---------------------------------------------------------------------------------
        db=new DBHelper(this);
        user = db.userExists();

        categoryName=getIntent().getStringExtra("category_name");

        scrollView=(ScrollView)findViewById(R.id.scrollView);
        title=(EditText)findViewById(R.id.title);
        description=(EditText)findViewById(R.id.description);
        address=(EditText)findViewById(R.id.address);
        addImage = (ImageButton) findViewById(R.id.add_image);

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
                dialog.show(fm, "dialog");
            }
        });

        mapview = (ImageButton)findViewById(R.id.mapview);
        mapview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
                dialog.show(fm, "dialog");
            }
        });

        categoryResource = getIntent().getIntExtra("category_icon",0);
        categoryImg=(ImageView)findViewById(R.id.category__img);
        categoryImg.setImageResource(categoryResource);

        description=(EditText)findViewById(R.id.description);
        description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(total_imgs<4) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Número máximo de imagenes alcanzado",Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Horizontal list -------------------------------------------------------------------------
        //----------------------------------------------------------------------------------------
        data = new ArrayList<Bitmap>();
        //----------------------------------------------------------------------------------------
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myList = (RecyclerView) findViewById(R.id.img_recycler_view);
        adapter = new Recycler_View_Adapter(data, getApplication());
        myList.setAdapter(adapter);
        myList.setLayoutManager(layoutManager);

        //Send button ------------------------------------------------------------------------------
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tit=title.getText().toString();
                String desc=description.getText().toString();
                String addr=address.getText().toString();
                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());


                if(!tit.equals("") && !desc.equals("") && !addr.equals("")){
                    //Creates the new report
                    Report report = new Report("00",user.getId(),tit,desc,addr,
                            latitude,0,0,0,longitude,Report.PUBLISHED,categoryName,date,imagesName);
                    JSONObject reportJson = createReportJson(report);
                    JSONObject imagesJson = createImagesJson();

                    new SendReporAsyncTask(reportJson,imagesJson,Constants.URL_CREATE_REPORT,AddNewReportActivity.this).execute();
                }else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.incomplete_info_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Open the dialog at first
        FragmentManager fm = getSupportFragmentManager();
        AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
        dialog.show(fm, "dialog");
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
     * Prepare the info to send
     * @param report
     * @return
     */
    private JSONObject createReportJson(Report report){
        HashMap<String,String> reportmap = new HashMap<>();
        ArrayList<HashMap<String,String>> imagesmap = new ArrayList();
        HashMap<String,HashMap<String,String>> map = new HashMap<>();

        //Prepare the info for the report
        reportmap.put(Constants.TITLE_EXTRA,report.getTitle());
        reportmap.put(Constants.DESCRIPTION_EXTRA,report.getDescription());
        reportmap.put(Constants.CATEGORY_EXTRA,report.getCategory());
        reportmap.put(Constants.ADDRESS_EXTRA,report.getAddress());
        reportmap.put(Constants.LONGITUDE_EXTRA,report.getLongitude()+"");
        reportmap.put(Constants.LATITUDE_EXTRA,report.getLatitude()+"");
        reportmap.put(Constants.USER_EXTRA,user.getId());

        //Prepare the info for the images (names)
        for(int i =0; i < report.getImages().size();i++){
            HashMap<String,String> auxMap = new HashMap<>();
            String image = report.getImages().get(i);
            auxMap.put(Constants.IMAGE_EXTRA,image);
            imagesmap.add(auxMap);
        }

        map.put(Constants.REPORT_EXTRA,reportmap);

        JSONObject jsonObject = new JSONObject(map);
        JSONArray jsonArray = new JSONArray(imagesmap);
        try{
            jsonObject.put(Constants.IMAGES_REPORT_EXTRA,jsonArray);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("Json report",jsonObject.toString());
        return jsonObject;
    }

    /**
     * Prepares the info withe the images to send
     * @return
     */
    private JSONObject createImagesJson(){
        if(!imagesName.isEmpty()) {
        ArrayList<HashMap<String,String>> imagesmap = new ArrayList<>();

        //Prepare the info with the base64 encoded images
            for (int i = 0; i < imagesName.size(); i++) {
                HashMap<String, String> auxMap = new HashMap<>();
                String imageName = imagesName.get(i);
                String image = images.get(i);
                auxMap.put(Constants.NAME_EXTRA, imageName);
                auxMap.put(Constants.IMAGE_EXTRA, image);
                imagesmap.add(auxMap);
            }

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray(imagesmap);
            try {
                jsonObject.put(Constants.IMAGES_REPORT_EXTRA, jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("Json report images", jsonObject.toString());
            return jsonObject;
        }else{
            return null;
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap bp = decodeUri(getApplicationContext(), selectedImage);
                        int w=256;
                        int h=256;
                        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bp,w,h);
                        adapter.insert(adapter.getItemCount(), thumbImage);
                        //myList.scrollToPosition(adapter.getItemCount());
                        //data.add(bp);
                        images.add(Constants.getStringImage(bp));
                        total_imgs++;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap bp = decodeUri(getApplicationContext(), selectedImage);
                        //adapter.insert(0, Bitmap.createScaledBitmap(bp, w, h, false));
                        //adapter.insert(0,null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     *
     * @param c
     * @param uri
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap decodeUri(Context c, Uri uri)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);
    }

    @Override
    public void onArticleSelectedListener(String addr,double latitude,double longitude) {
        address.setText(addr);
        this.latitude=latitude;
        this.longitude=longitude;
        Log.d("TAG LONG","long: "+this.longitude+ " lat: " +this.latitude);
    }


    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<Bitmap> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<Bitmap> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_layout, parent, false);
            View_Holder holder = new View_Holder(v);

            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

             //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            if(list.get(position)!=null){
                holder.image.setImageBitmap(list.get(position));
                holder.delete.setVisibility(View.VISIBLE);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.remove(position);
                    }
                });
            }
            /*
            else{
                holder.delete.setVisibility(View.GONE);
                holder.image.setBackgroundResource(R.drawable.add_image_background);
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //data.add(0,null);
                        //adapter.insert(0,null);
                        if(total_imgs<4) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Número máximo de imagenes alcanzado",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            */
            //holder.image.setText(list.get(position).getTitle());

            //animate(holder);

        }

        @Override
        public int getItemCount() {
            //returns the number of elements the RecyclerView will display
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView on a predefined position
        public void insert(int position, Bitmap data) {
            list.add(position, data);
            imagesName.add(Constants.giverImageName(Constants.IMG_CONS_REP));
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing a specified Data object
        public void remove(int position) {
            //int position = list.indexOf(data);
            // TODO: 15/11/16 elimia raro
            Log.d("POS DEL",position+"");
            total_imgs--;
            list.remove(position);
            images.remove(position);
            imagesName.remove(position);
            notifyItemRemoved(position);

        }

    }

    //Auxiliar class holder ------------------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        ImageView image,delete;

        View_Holder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            delete = (ImageView) itemView.findViewById(R.id.delete_btn);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */

    public class SendReporAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        /**
         * Params
         */
        private JSONObject data;
        private JSONObject dataImages;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        /**
         *
         * @param data
         * @param url
         * @param context
         */
        public SendReporAsyncTask(JSONObject data,JSONObject dataImages,String url,Context context){
            this.data = data;
            this.dataImages = dataImages;
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
                Log.d("JSON RESPONSE",jsonObject.toString());
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(AddNewReportActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                        if(dataImages!=null){
                            //Send images if exist
                            new SendImagesAsyncTask(dataImages,Constants.URL_CREATE_IMAGES_REPORT,context).execute();
                        }else {
                            progressBar.dismiss();
                            //Go back to Menu
                            Intent goToMenu = new Intent(AddNewReportActivity.this,MenuActivity.class);
                            startActivity(goToMenu);
                            finish();
                        }

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(AddNewReportActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(AddNewReportActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            //progressBar.dismiss();
        }
    }

    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send images to the server
     *----------------------------------------------------------------------------------------------
     */

    public class SendImagesAsyncTask extends AsyncTask<Void, Void, JSONObject> {
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
        public SendImagesAsyncTask(JSONObject data,String url,Context context){
            this.data = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            progressBar.dismiss();
            if(jsonObject!=null) {
                Log.d("JSON RESPONSE",jsonObject.toString());
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(AddNewReportActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                        //Go back to Menu
                        Intent goToMenu = new Intent(AddNewReportActivity.this,MenuActivity.class);
                        goToMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToMenu);
                        finish();
                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(AddNewReportActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(AddNewReportActivity.this,getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
        }
    }

}
