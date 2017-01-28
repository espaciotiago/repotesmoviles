package com.ufo.ufomobile.reportesmoviles;

import android.*;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.Report;
import utilities.User;

import static com.ufo.ufomobile.reportesmoviles.MenuActivity.drawableToBitmap;

public class ReportDescriptionActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**
     * UI Elements
     */
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView date,id,title,description,address,status,supports,comments,supotingTxt;
    View likeView,commentView,bottomBarView;
    Recycler_View_Adapter adapter;
    List<String> dataList;
    ImageView categoryImage,imageReport;
    RecyclerView myList;
    LinearLayoutManager layoutManager;
    ProgressBar loader,loader2;
    ProgressDialog progressBar;

    /**
     * Map elemnts
     */
    private GoogleMap mMap;
    private SupportMapFragment fragment;
    LocationManager locationManager;
    double longitude,latitude;
    private LatLng latiLong;
    private MarkerOptions markerOptions;
    private String addressStr;
    private DBHelper db;
    private User user;
    int supporting;
    Report report;

    /**
     * Logical elemnts
     */
    int resource_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setTitle("Reporte ciudadano");

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db=new DBHelper(this);
        user= db.userExists();

        report = (Report) getIntent().getSerializableExtra("report");
        supporting = report.getSuporting();
        String stat = Constants.getStatus(report.getStatus());
        String cat = report.getCategory();
        String[] categories=getResources().getStringArray(R.array.categories);
        int[] imageIDs = {
                R.drawable.ic_marker_water,
                R.drawable.ic_marker_trash,
                R.drawable.ic_marker_traffic,
                R.drawable.ic_marker_public,
                R.drawable.ic_marker_road,
                R.drawable.ic_marker_animal,
                R.drawable.ic_marker_police,
                R.drawable.ic_marker_other
        };
        //------------------------------------------------------------------------------------------
        latitude = report.getLatitude();
        longitude = report.getLongitude();

        //------------------------------------------------------------------------------------------


        date = (TextView)findViewById(R.id.date);
        id = (TextView)findViewById(R.id.id);
        title = (TextView)findViewById(R.id.title);
        description = (TextView)findViewById(R.id.description);
        address = (TextView)findViewById(R.id.address);
        status = (TextView)findViewById(R.id.status);
        supports= (TextView) findViewById(R.id.supports);
        comments= (TextView) findViewById(R.id.comments);
        supotingTxt = (TextView) findViewById(R.id.supporting);
        categoryImage = (ImageView)findViewById(R.id.category__img);
        imageReport = (ImageView) findViewById(R.id.image_report);
        likeView = (View) findViewById(R.id.like_view);
        commentView = (View) findViewById(R.id.comment_view);
        bottomBarView = (View) findViewById(R.id.view_bottom);
        loader = (ProgressBar) findViewById(R.id.loader);
        loader2 = (ProgressBar) findViewById(R.id.loader2);

        date.setText(report.getDate());
        id.setText(report.getId());
        title.setText(report.getTitle());
        description.setText(report.getDescription());
        address.setText(report.getAddress());
        status.setText(Constants.getStatus(report.getStatus()));
        supports.setText(report.getSupports()+"");
        comments.setText(report.getComments()+"");

        if(supporting>0){
            supotingTxt.setText(getString(R.string.suporting));
        }

        if(cat.equals(categories[0])){
            categoryImage.setImageResource(imageIDs[0]);
            resource_category=imageIDs[0];
        }else if(cat.equals(categories[1])){
            categoryImage.setImageResource(imageIDs[1]);
            resource_category=imageIDs[1];
        }else if(cat.equals(categories[2])){
            categoryImage.setImageResource(imageIDs[2]);
            resource_category=imageIDs[2];
        }else if(cat.equals(categories[3])){
            categoryImage.setImageResource(imageIDs[3]);
            resource_category=imageIDs[3];
        }else if(cat.equals(categories[4])){
            categoryImage.setImageResource(imageIDs[4]);
            resource_category=imageIDs[4];
        }else if(cat.equals(categories[5])){
            categoryImage.setImageResource(imageIDs[5]);
            resource_category=imageIDs[5];
        }else if(cat.equals(categories[6])){
            categoryImage.setImageResource(imageIDs[6]);
            resource_category=imageIDs[6];
        }else if(cat.equals(categories[7])){
            categoryImage.setImageResource(imageIDs[7]);
            resource_category=imageIDs[7];
        }

        if(stat.equals(Report.IN_PROCESS)){
            status.setBackgroundColor(getResources().getColor(R.color.colorInPorcess));
            bottomBarView.setBackgroundColor(getResources().getColor(R.color.colorInPorcess));
        }else if(stat.equals(Report.PUBLISHED)){
            status.setBackgroundColor(getResources().getColor(R.color.colorPublished));
            bottomBarView.setBackgroundColor(getResources().getColor(R.color.colorPublished));
        }else if(stat.equals(Report.SOLVED)){
            status.setBackgroundColor(getResources().getColor(R.color.colorSolved));
            bottomBarView.setBackgroundColor(getResources().getColor(R.color.colorSolved));
        }

        //Map --------------------------------------------------------------------------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Horizontal list -------------------------------------------------------------------------
        dataList = new ArrayList<String>();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myList = (RecyclerView) findViewById(R.id.img_recycler_view);

        //Button -----------------------------------------------------------------------------------
        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the dialog with comments
                FragmentManager fm = getSupportFragmentManager();
                CommentsDialogFragment dialog = CommentsDialogFragment.newInstance(report.getId());
                dialog.show(fm, "dialog");
            }
        });
        
        likeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(supporting>0){
                    //Already supporting, do nothing
                }else{
                    String reportId = report.getId();
                    String userId = user.getId();
                    JSONObject jsonObject = createJsonSupport(reportId,userId);
                    Log.e("JSON CRE",jsonObject.toString());
                    new Http_Support(jsonObject,Constants.URL_SUPPORT,ReportDescriptionActivity.this).execute();

                }
            }
        });

        //Get the images of the report
        JSONObject jsonObject = createJsonGetImages(report.getId());
        new Http_GetReportImages(jsonObject,Constants.URL_GET_REPORT_IMAGES,ReportDescriptionActivity.this).execute();
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
     * Prepare the info to send and get the encoded images
     * @param idReport
     * @return
     */
    private JSONObject createJsonGetImages(String idReport){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.ID_EXTRA,idReport);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
    }

    /**
     * Prepare the data to do a support
     * @param reportId
     * @param userId
     * @return
     */
    private JSONObject createJsonSupport(String reportId,String userId){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.REPORT_EXTRA,reportId);
        map.put(Constants.USER_EXTRA,userId);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Bitmap b1 = drawableToBitmap(getResources().getDrawable(resource_category));
        Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);
        LatLng latLng = new LatLng(latitude,longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Open the dialog at first
                FragmentManager fm = getSupportFragmentManager();
                AddressViewDialogFragment dialog = AddressViewDialogFragment.newInstance(latitude, longitude,addressStr,resource_category);
                dialog.show(fm, "dialog");
            }
        });
    }


    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<String> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_image_row_layout, parent, false);
            View_Holder holder = new View_Holder(v);
            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            if(list.get(position)!=null){
                if(position==0){
                    //Set Header image
                    loadBitmap(list.get(position),imageReport,0);
                }
                //Load the images in the UI
                loadBitmap(list.get(position),holder.image,1);

                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: 29/12/16
                        Intent i = new Intent(ReportDescriptionActivity.this,ImageActivity.class);
                        i.putExtra("reportId",report.getId());
                        i.putExtra("pos",position);
                        startActivity(i);

                        //Open the dialog with images
                        /*
                        FragmentManager fm = getSupportFragmentManager();
                        ImageDetailsDialogFragment dialog = ImageDetailsDialogFragment.newInstance(list.get(position));
                        dialog.show(fm, "dialog");
                        */
                    }
                });
            }
            else{

            }
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
        public void insert(int position, String data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing a specified Data object
        public void remove(String data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

    }

    //Auxiliar class holder ------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        ImageView image;

        View_Holder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
    // ---------------------------------------------------------------------------------
    /**
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     *
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(String resId,
                                                         int reqWidth, int reqHeight, int pos) {

        // First decode with inJustDecodeBounds=true to check dimensions
        byte[] decodedString = Base64.decode(resId, Base64.NO_PADDING);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedString,0,decodedString.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bp = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length, options);
        if(pos == Constants.IMAGE_COMPLETE){
            return bp;
        }else {
            int w = 256;
            int h = 256;
            Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bp, w, h);
            return bitmap2;
        }
    }

    /**
     * Processing Bitmaps Off the UI Thread
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int pos;
        private String data = "";

        public BitmapWorkerTask(ImageView imageView,int pos) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.pos = pos;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return decodeSampledBitmapFromResource(data,200,200,pos);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     *   </ Processing Bitmaps Off the UI Thread >
     */

    /**
     * Handle Concurrency
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     *
     * @param resId
     * @param imageView
     */
    public void loadBitmap(String resId, ImageView imageView,int pos) {
        if (cancelPotentialWork(resId, imageView)) {
            Bitmap mPlaceHolderBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView,pos);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    /**
     *
     * @param data
     * @param imageView
     * @return
     */
    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData.equals("") || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     *
     * @param imageView
     * @return
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * </ Handle Concurrency >
     */


    //----------------------------------------------------------------------------------------------
    //ASYNC TASK GETTING THE IMAGES OF THE REPORT
    //----------------------------------------------------------------------------------------------
    private class Http_GetReportImages extends AsyncTask<Void, Void, JSONObject>
    {
        ProgressDialog loading;
        private JSONObject jsonObject;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        public Http_GetReportImages(JSONObject data,String url,Context context){
            this.jsonObject = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(MenuActivity.this, "Comprobando datos...",
            //      "Puede tardar unos segundos", true, true);
            /*
            loading = new ProgressDialog(ReportDescriptionActivity.this,R.style.StyledDialog);
            loading.setInverseBackgroundForced(true);
            loading.setCancelable(false);
            loading.show();
            */
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {   JSONObject ret=null;

            try
            {
                ret = connectionHelper.sendPOST(jsonObject,url);
            } catch (Exception e)
            {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return ret;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(JSONObject info)
        {
            if(info!=null) {
                // TODO: 8/12/16
                Log.d("JSON IMGS",info.toString());
                try {
                    JSONArray jsonImagesArray = info.getJSONArray(Constants.IMAGES_REPORT_EXTRA);
                    for(int i = 0; i < jsonImagesArray.length();i++){
                        JSONObject jsonImage = (JSONObject) jsonImagesArray.get(i);
                        String image = jsonImage.getString(Constants.IMAGE_EXTRA);
                        dataList.add(image);
                    }
                } catch (JSONException e) {
                    // TODO: 8/12/16 Show error
                    e.printStackTrace();
                }
            }else{
                // TODO: 8/12/16 Show Error
            }
            adapter = new Recycler_View_Adapter(dataList, getApplication());
            myList.setAdapter(adapter);
            myList.setLayoutManager(layoutManager);

            loader.setVisibility(View.GONE);
            loader2.setVisibility(View.GONE);
            //loading.dismiss();
        }
    }

    //----------------------------------------------------------------------------------------------
    //ASYNC TASK SUPPORTING
    //----------------------------------------------------------------------------------------------
    private class Http_Support extends AsyncTask<Void, Void, JSONObject>
    {
        private JSONObject jsonObject;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        public Http_Support(JSONObject data,String url,Context context){
            this.jsonObject = data;
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
        protected JSONObject doInBackground(Void... params)
        {   JSONObject ret=null;
            try
            {
                ret = connectionHelper.sendPOST(jsonObject,url);
            } catch (Exception e)
            {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject)
        {
            if(jsonObject!=null) {
                Log.e("JSON RESPONSE",jsonObject.toString());
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(ReportDescriptionActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                        report.setSuporting(1);
                        report.setSupports(report.getSupports()+1);
                        supporting = 1;
                        supotingTxt.setText(getString(R.string.suporting));
                        supports.setText(report.getSupports()+"");

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(ReportDescriptionActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                // TODO: 8/12/16 Show Error
            }
            progressBar.dismiss();
        }
    }

}
