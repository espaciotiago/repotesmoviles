package com.ufo.ufomobile.reportesmoviles;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utilities.Report;

import static com.ufo.ufomobile.reportesmoviles.MenuActivity.drawableToBitmap;

public class ReportDescriptionActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**
     * UI Elements
     */
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView date,id,title,description,address,status,supports,comments;
    View likeView,commentView,bottomBarView;
    Recycler_View_Adapter adapter;
    List<Bitmap> data;
    ImageView categoryImage;

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

        final Report report = (Report) getIntent().getSerializableExtra("report");
        String stat = report.getStatus();
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
        categoryImage = (ImageView)findViewById(R.id.category__img);
        likeView = (View) findViewById(R.id.like_view);
        commentView = (View) findViewById(R.id.comment_view);
        bottomBarView = (View) findViewById(R.id.view_bottom);

        date.setText(report.getDate());
        id.setText(report.getId());
        title.setText(report.getTitle());
        description.setText(report.getDescription());
        address.setText(report.getAddress());
        status.setText(report.getStatus());
        supports.setText(report.getSupports()+"");

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
        data = new ArrayList<Bitmap>();
        data.add(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView myList = (RecyclerView) findViewById(R.id.img_recycler_view);
        adapter = new Recycler_View_Adapter(data, getApplication());
        myList.setAdapter(adapter);
        myList.setLayoutManager(layoutManager);

        //Button -----------------------------------------------------------------------------------
        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the dialog with comments
                FragmentManager fm = getSupportFragmentManager();
                CommentsDialogFragment dialog = CommentsDialogFragment.newInstance();
                dialog.show(fm, "dialog");
            }
        });
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
                AddressViewDialogFragment newFragment;
                FragmentManager fragmentManager = getSupportFragmentManager();
                newFragment = AddressViewDialogFragment.newInstance(latitude, longitude,addressStr,resource_category);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
            }
        });
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
        public void insert(int position, Bitmap data) {
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

}
