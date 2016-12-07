package com.ufo.ufomobile.reportesmoviles;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import utilities.Constants;
import utilities.DBHelper;
import utilities.Report;
import utilities.User;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        CategorySelectionDialogFragment.OnaAddSelected {

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
    private GoogleMap mMap;
    View lastSelectedView = null;
    Gallery gallery;
    LinearLayoutManager layoutManager;
    LocationManager locationManager;
    double longitude,latitude=0;
    List<Report> data;
    DBHelper db;
    User user;
    CategorySelectionDialogFragment newFragment;

    private int positionList = -1;
    private int stateScroll = -1;
    private int scrollPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Toolbar -----------------------------------------------------------------------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Database ---------------------------------------------------------------------------------
        db=new DBHelper(this);
        user = db.userExists();
        //Map --------------------------------------------------------------------------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Drawer -----------------------------------------------------------------------------------
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigationview ---------------------------------------------------------------------------
        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        //View headerView = navigationView.inflateHeaderView(R.layout.nav_header_menu);
        ImageView navHeaderImageView = (ImageView) navigationView.findViewById(R.id.nav_profile_pic);
        TextView navHeaderName = (TextView) navigationView.findViewById(R.id.nav_name);
        TextView navHeaderMail = (TextView) navigationView.findViewById(R.id.nav_mail);

        //Put the header information
        if(user.getImage()!=null && !user.getImage().equals("")){
            byte[] decodedString = Base64.decode(user.getImage(), Base64.NO_PADDING);
            Bitmap imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            navHeaderImageView.setImageBitmap(imag);

        }
        navHeaderName.setText(user.getName());
        navHeaderMail.setText(user.getMail());

        //Horizontal list --------------------------------------------------------------------------
        data = new ArrayList<Report>();
        //------------------------------------------------------------------------------------------

        permisosCamara();
        permisosGPS();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(newFragment!=null){
            dismissAllDialogFragments();
            newFragment = null;
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifications) {
            Intent goToNotification = new Intent(MenuActivity.this, NotificationsActivity.class);
            startActivity(goToNotification);
            return true;
        }
        if(id == R.id.action_report){
            //Open the dialog with categories
            FragmentManager fm = getSupportFragmentManager();
            CategorySelectionDialogFragment dialog = CategorySelectionDialogFragment.newInstance();
            dialog.show(fm, "dialog");
        }

        return super.onOptionsItemSelected(item);
    }

    public void dismissAllDialogFragments() {
        getSupportFragmentManager().popBackStack("dialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_new_report){
            //Open the dialog with categories
            FragmentManager fm = getSupportFragmentManager();
            CategorySelectionDialogFragment dialog = CategorySelectionDialogFragment.newInstance();
            dialog.show(fm, "dialog");
        }else if(id==R.id.nav_profile){
            Intent goToProfile = new Intent(MenuActivity.this, ProfileActivity.class);
            startActivity(goToProfile);
        }else if(id==R.id.nav_reports){
            Intent goToNotification = new Intent(MenuActivity.this, NotificationsActivity.class);
            startActivity(goToNotification);
        }else if(id==R.id.nav_close){
            db.deleteUser();
            Intent goToLogin = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(goToLogin);
            finish();
        }

        //Filtros


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // PERMISOS ------------------------------------------------------------------------------------
    private void permisosGPS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // PoneGPS
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISO_GPS);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISO_GPS);
            }
        }
    }

    private void permisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.PERMISO_CAMARA);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISO_CAMARA);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        /*
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
        */
        find_Location(getApplicationContext());
        LatLng latLng = new LatLng(latitude, longitude);
        /*
            Bitmap b1 = drawableToBitmap(getResources().getDrawable(R.drawable.alcantarillado));
            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth() / 4, b1.getHeight() / 4, false);
            mMap.addMarker(new MarkerOptions().position(sydney)
                    .title("Marker in Sydney")
                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            Bitmap b2 = drawableToBitmap(getResources().getDrawable(R.drawable.alumbrado));
            Bitmap bhalfsize2 = Bitmap.createScaledBitmap(b2, b2.getWidth() / 4, b2.getHeight() / 4, false);
            mMap.addMarker(new MarkerOptions().position(sydney2)
                    .title("Marker in Sydney2")
                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize2)));

            Bitmap b = drawableToBitmap(getResources().getDrawable(R.drawable.acueducto));
            Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 4, b.getHeight() / 4, false);
            mMap.addMarker(new MarkerOptions().position(sydney3)
                    .title("Marker in Sydney3")
                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize)));
            */
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(13)
                    .bearing(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String id = marker.getTitle();
                    for (int i =0; i < data.size(); i++){
                        String reprId = data.get(i).getId();
                        if(id.equals(reprId)){
                            layoutManager.scrollToPositionWithOffset(i, 5);
                            Log.d("TAG LAT",marker.getPosition().latitude+"");
                            Log.d("TAG LONG",marker.getPosition().longitude+"");
                            Log.d("TAG POS",i+"");
                            positionList = i;
                        }
                    }
/*
                    if (tit.equals("Marker in Sydney")) {
                        layoutManager.scrollToPositionWithOffset(0, 20);
                    } else if (tit.equals("Marker in Sydney2")) {
                        layoutManager.scrollToPositionWithOffset(1, 20);
                    } else if (tit.equals("Marker in Sydney3")) {
                        layoutManager.scrollToPositionWithOffset(2, 20);
                    }
                    */
                    return true;
                }
            });
        new Http_GetReports(latitude,longitude).execute();
    }


    //----------------------------------------------------------------------------------------------

    /**
     * Convert a drawable into a bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //----------------------------------------------------------------------------------------------
    public void find_Location(Context con) {
        Log.d("Find Location", "in find_location");
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) con.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
    }

    @Override
    public void onArticleSelectedListener(int resource, String name) {
        Intent goToReport = new Intent(MenuActivity.this, AddNewReportActivity.class);
        goToReport.putExtra("category_icon", resource);
        goToReport.putExtra("category_name", name);
        startActivity(goToReport);
    }

    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<Report> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<Report> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_row_layout, parent, false);
            View_Holder holder = new View_Holder(v);

            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

            String state=list.get(position).getStatus();
            String category=list.get(position).getCategory();
            String[] categories=getResources().getStringArray(R.array.categories);

            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            holder.title.setText(list.get(position).getTitle());
            holder.description.setText(list.get(position).getDescription());
            holder.state.setText(state);
            if(state.equals(Report.PUBLISHED)){
                holder.state.setTextColor(getResources().getColor(R.color.colorPublished));
            }else if(state.equals(Report.IN_PROCESS)){
                holder.state.setTextColor(getResources().getColor(R.color.colorInPorcess));
            }else{
                holder.state.setTextColor(getResources().getColor(R.color.colorSolved));
            }

            if(category.equals(categories[0])){
                holder.category_img.setImageResource(imageIDs[0]);
            }else if(category.equals(categories[1])){
                holder.category_img.setImageResource(imageIDs[1]);
            }else if(category.equals(categories[2])){
                holder.category_img.setImageResource(imageIDs[2]);
            }else if(category.equals(categories[3])){
                holder.category_img.setImageResource(imageIDs[3]);
            }else if(category.equals(categories[4])){
                holder.category_img.setImageResource(imageIDs[4]);
            }else if(category.equals(categories[5])){
                holder.category_img.setImageResource(imageIDs[5]);
            }else if(category.equals(categories[6])){
                holder.category_img.setImageResource(imageIDs[6]);
            }else if(category.equals(categories[7])){
                holder.category_img.setImageResource(imageIDs[7]);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Report report=list.get(position);
                    Intent goToDescription = new Intent(getApplicationContext(),ReportDescriptionActivity.class);
                    goToDescription.putExtra("report",report);
                    startActivity(goToDescription);
                }
            });

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
        public void insert(int position, Report data) {
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

    //Auxiliar class holder ------------------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        TextView title,state,description;//,more;
        ImageView category_img;

        View_Holder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            state = (TextView) itemView.findViewById(R.id.state);
            //more = (TextView) itemView.findViewById(R.id.more);
            category_img = (ImageView) itemView.findViewById(R.id.category_img);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------------------------------------
    //ASYNC TASK BRINGING THE NEARESTS REPORTS
    //----------------------------------------------------------------------------------------------
        private class Http_GetReports extends AsyncTask<Void, Void, ArrayList<Report>>
    {
        ProgressDialog loading;
        double latitude,longitude;

        public Http_GetReports(double latitude,double longitude){
            this.latitude=latitude;
            this.longitude=longitude;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(MenuActivity.this, "Comprobando datos...",
              //      "Puede tardar unos segundos", true, true);
            loading = new ProgressDialog(MenuActivity.this,R.style.StyledDialog);
            loading.setInverseBackgroundForced(true);
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected ArrayList<Report> doInBackground(Void... params)
        {   ArrayList<Report> ret=new ArrayList<>();

            try
            {
                String[] categories=getResources().getStringArray(R.array.categories);
                //TO DO GET REPORTS
                //----------------------------------------------------------------------------------
                Thread.sleep(2000);
                ret.add(new Report("id123", "Titulo del reportes lo suficientmente largo", "Esta es una descripción" +
                        " del reporte considerablemente larga, a manera de prueba." +
                        "La idea es ver que tal se ve en la UI. Descripción de prueba de id123.",
                        "String address", 3.391318,
                        0, -76.5238619, Report.PUBLISHED, categories[0], "hoy", null));

                ret.add(new Report("id124", "String title", "String description",
                        "String address", 3.391318,
                        0, -76.54386190000001, Report.IN_PROCESS, categories[1], "hoy", null));

                ret.add(new Report("id125", "String title", "String description",
                        "String address", 3.4113179999999996,
                        0, -76.54386190000001, Report.SOLVED, categories[2], "hoy", null));

                ret.add(new Report("id126", "String title", "String description",
                        "String address", 3.4113179999999996+0.002,
                        0, -76.54386190000001-0.002, Report.SOLVED, categories[3], "hoy", null));

                ret.add(new Report("id127", "String title", "String description",
                        "String address", 3.4113179999999996-0.004,
                        0, -76.54386190000001-0.0004, Report.SOLVED, categories[4], "hoy", null));

                ret.add(new Report("id128", "String title", "String description",
                        "String address", 3.391318+0.006,
                        0, -76.5238619+0.006, Report.SOLVED, categories[5], "hoy", null));

                ret.add(new Report("id129", "String title", "String description",
                        "String address", 3.4113179999999996+0.008,
                        0, -76.5438619000000-0.008, Report.SOLVED, categories[6], "hoy", null));

                ret.add(new Report("id1210", "String title", "String description",
                        "String address", 3.391318-0.001,
                        0, -76.54386190000001-0.001, Report.SOLVED, categories[7], "hoy", null));
                //----------------------------------------------------------------------------------

            } catch (Exception e)
            {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return ret;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(ArrayList<Report> info)
        {

            data=info;
            String[] categories=getResources().getStringArray(R.array.categories);
            for(int i = 0; i<data.size(); i++){
                Report rep = data.get(i);
                String category = rep.getCategory();
                double lat=rep.getLatitude();
                double lon=rep.getLongitude();
                LatLng pos = new LatLng(lat,lon);

                if(category.equals(categories[0])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[0]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)))
                            .setInfoWindowAnchor(0,0);

                }else if(category.equals(categories[1])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[1]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[2])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[2]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[3])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[3]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[4])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[4]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[5])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[5]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[6])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[6]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }else if(category.equals(categories[7])){
                    Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[7]));
                    Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(rep.getId())
                            .snippet(rep.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                }
            }

            layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            final RecyclerView myList = (RecyclerView) findViewById(R.id.my_recycler_view);
            Recycler_View_Adapter adapter = new Recycler_View_Adapter(data, getApplication());
            myList.setAdapter(adapter);
            myList.setLayoutManager(layoutManager);

            /*
            myList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    stateScroll = newState;
                    if(stateScroll==2) {
                        if (scrollPos > 0) {
                            if (positionList < data.size()) {
                                positionList++;
                                layoutManager.scrollToPositionWithOffset(positionList,20);
                            }
                        } else if (scrollPos < 0) {
                            if (positionList >= 0) {
                                positionList--;
                                layoutManager.scrollToPositionWithOffset(positionList,20);
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    scrollPos = dx;
                }
            });
            */
            loading.dismiss();
        }
    }

}
