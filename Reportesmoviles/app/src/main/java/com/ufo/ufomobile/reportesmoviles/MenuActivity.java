package com.ufo.ufomobile.reportesmoviles;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
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
    ArrayList<Report> data;
    DBHelper db;
    User user;
    CategorySelectionDialogFragment newFragment;
    RecyclerView myList;
    Recycler_View_Adapter adapter;

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

        View headerView = navigationView.getHeaderView(0);
        ImageView navHeaderImageView = (ImageView) headerView.findViewById(R.id.nav_profile_pic);
        TextView navHeaderName = (TextView) headerView.findViewById(R.id.nav_name);
        TextView navHeaderMail = (TextView) headerView.findViewById(R.id.nav_mail);

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

    /**
     * Prepares the info for the request of the reports
     * @param latitud
     * @param longitude
     * @param idUsuario
     * @return
     */
    private JSONObject createGetReportsJson(double longitude,double latitud,String idUsuario){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.LONGITUDE_EXTRA,longitude+"");
        map.put(Constants.LATITUDE_EXTRA,latitud+"");
        map.put(Constants.USER_EXTRA,idUsuario);

        JSONObject jsonObject = new JSONObject(map);
        Log.d("JSON GET REPORTS",jsonObject.toString());
        return jsonObject;
    }

    /**
     * Filter the reports given a category
     * @param category
     * @return
     */
    private ArrayList<Report> filter(String category){
        ArrayList<Report> filteredinfo = new ArrayList<>();
        for (int i = 0; i < data.size();i++){
            Report rep = data.get(i);
            if(rep.getCategory().equals(category)){
                filteredinfo.add(rep);
            }
        }
        return filteredinfo;
    }

    /**
     * Set the filtered info into the map and the recycler view
     * @param filteredInfo
     */
    private void addFilteredInfoToMap(ArrayList<Report> filteredInfo){
        mMap.clear();
        String[] categories = getResources().getStringArray(R.array.categories);
        for(int i =0; i < filteredInfo.size();i++){
            Report rep = filteredInfo.get(i);
            //get the info
            String category = rep.getCategory();
            double lat = rep.getLatitude();
            double lon = rep.getLongitude();
            LatLng pos = new LatLng(lat, lon);

            //Creates the amrker in the map
            if (category.equals(categories[0])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[0]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)))
                        .setInfoWindowAnchor(0, 0);

            } else if (category.equals(categories[1])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[1]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[2])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[2]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[3])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[3]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[4])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[4]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[5])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[5]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[6])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[6]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            } else if (category.equals(categories[7])) {
                Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[7]));
                Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                mMap.addMarker(new MarkerOptions().position(pos)
                        .title(rep.getId())
                        .snippet(rep.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

            }
        }

        //Put the info in the recyclerview
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        final RecyclerView myList = (RecyclerView) findViewById(R.id.my_recycler_view);
        final Recycler_View_Adapter adapter = new Recycler_View_Adapter(filteredInfo, getApplication());
        myList.setAdapter(adapter);
        myList.setLayoutManager(layoutManager);

        //Set the on click marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = marker.getTitle();
                for (int i =0; i < adapter.getList().size(); i++){
                    String reprId = adapter.getList().get(i).getId();
                    if(id.equals(reprId)){
                        layoutManager.scrollToPositionWithOffset(i, 5);
                        positionList = i;
                    }
                }
                return true;
            }
        });
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
        if(id == R.id.action_about){
            // TODO: 8/12/16
        }
        if(id == R.id.action_close){
            db.deleteUser();
            Intent goToLogin = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(goToLogin);
            finish();
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
        ArrayList<Report> filteredInfo;

        switch (id){
            //General
            case R.id.nav_new_report:
                //Open the dialog with categories
                FragmentManager fm = getSupportFragmentManager();
                CategorySelectionDialogFragment dialog = CategorySelectionDialogFragment.newInstance();
                dialog.show(fm, "dialog");
                break;
            case R.id.nav_profile:
                Intent goToProfile = new Intent(MenuActivity.this, ProfileActivity.class);
                startActivity(goToProfile);
                break;
            case R.id.nav_reports:
                Intent goToNotification = new Intent(MenuActivity.this, NotificationsActivity.class);
                startActivity(goToNotification);
                break;

            //Filters
            case R.id.nav_all:
                addFilteredInfoToMap(data);
                break;
            case R.id.nav_aqueduct:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[0]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_garbage:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[1]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_traffic:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[2]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_public:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[3]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_roads:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[4]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_animal:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[5]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_security:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[6]);
                addFilteredInfoToMap(filteredInfo);
                break;
            case R.id.nav_other:
                filteredInfo = filter(getResources().getStringArray(R.array.categories)[7]);
                addFilteredInfoToMap(filteredInfo);
                break;

            //Configuration
            case R.id.nav_about:
                Intent goToAbout = new Intent(MenuActivity.this, AboutActivity.class);
                startActivity(goToAbout);
                break;
            case R.id.nav_close:
                db.deleteUser();
                Intent goToLogin = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(goToLogin);
                finish();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // PERMISOS ------------------------------------------------------------------------------------
    private void permisosGPS() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showExplanation("Reportes Móviles", "Requiere permisos para acceder a tu ubicación actual", new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISO_GPS);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISO_GPS);
            }

        } else {
            // Permission has already been granted
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String[] permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String[] permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                permissionName, permissionRequestCode);
    }

    private void permisosCamara() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.PERMISO_CAMARA);                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISO_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PoneGPS
                    mMap.setMyLocationEnabled(true);
                    find_Location(getApplicationContext());
                    LatLng latLng = new LatLng(latitude, longitude);
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
                            return true;
                        }
                    });
                    JSONObject jsonObject = createGetReportsJson(longitude,latitude,user.getId());
                    new Http_GetReports(jsonObject,Constants.URL_GET_REPORTS,MenuActivity.this).execute();
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
            showExplanation("Permission Needed", "Rationale", new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.PERMISO_GPS);
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
        JSONObject jsonObject = createGetReportsJson(longitude,latitude,user.getId());
        new Http_GetReports(jsonObject,Constants.URL_GET_REPORTS,MenuActivity.this).execute();
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
                showExplanation("Permission Needed", "Rationale", new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISO_GPS);
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

            String state=Constants.getStatus(list.get(position).getStatus());
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
            }else if(state.equals(Report.SOLVED)){
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

        /**
         *
         * @return
         */
        public List<Report> getList(){
            return list;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //----------------------------------------------------------------------------------------------
    //ASYNC TASK BRINGING THE NEARESTS REPORTS
    //----------------------------------------------------------------------------------------------
    private class Http_GetReports extends AsyncTask<Void, Void, JSONObject>
    {
        ProgressDialog loading;
        private JSONObject jsonObject;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        public Http_GetReports(JSONObject data,String url,Context context){
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
            loading = new ProgressDialog(MenuActivity.this,R.style.StyledDialog);
            loading.setInverseBackgroundForced(true);
            loading.setCancelable(false);
            loading.show();
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
                Log.d("REPORT ARRAY",info.toString());
                String[] categories = getResources().getStringArray(R.array.categories);
                data = new ArrayList<>();
                //Get the json array of reports
                try {
                    JSONArray jsonArray = info.getJSONArray(Constants.ALL_REPORTS_EXTRA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        //{"id":"19","usuario":"0123456789","estado":"P","categoria":"IV","direccion":"Cantarrana, Palmira, Valle del Cauca Colombia","latitud":"-76.4530738","longitud":"-76.4530738","titulo":"REPORTE DE PRUEBA #1","descripcion":"Reporte de pruebas de infraestructuras y v\u00edas. Con im\u00e1genes x 4.","fecha":"2016-12-07 14:47:41","apoyo":"1"}
                        JSONObject jsonRep = (JSONObject) jsonArray.get(i);
                        Log.d("REPORT JSON",jsonRep.toString());

                        Report rep = new Report(jsonRep.getString(Constants.ID_EXTRA),
                                                jsonRep.getString(Constants.USER_EXTRA),
                                                        jsonRep.getString(Constants.TITLE_EXTRA),
                                                        jsonRep.getString(Constants.DESCRIPTION_EXTRA),
                                                        jsonRep.getString(Constants.ADDRESS_EXTRA),
                                                        jsonRep.getDouble(Constants.LATITUDE_EXTRA),
                                                        jsonRep.getInt(Constants.SUPPORTS_EXTRA),
                                                        jsonRep.getInt(Constants.COMMENTS_EXTRA),
                                                        jsonRep.getInt(Constants.SUPPORTING_EXTRA),
                                                        jsonRep.getDouble(Constants.LONGITUDE_EXTRA),
                                                        jsonRep.getString(Constants.STATUS_EXTRA),
                                                        jsonRep.getString(Constants.CATEGORY_EXTRA),
                                                        jsonRep.getString(Constants.DATE_EXTRA),
                                                        new ArrayList<String>());

                        data.add(rep);
                        String category = rep.getCategory();
                        double lat = rep.getLatitude();
                        double lon = rep.getLongitude();
                        LatLng pos = new LatLng(lat, lon);

                        if (category.equals(categories[0])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[0]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)))
                                    .setInfoWindowAnchor(0, 0);

                        } else if (category.equals(categories[1])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[1]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[2])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[2]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[3])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[3]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[4])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[4]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[5])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[5]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[6])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[6]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        } else if (category.equals(categories[7])) {
                            Bitmap b1 = drawableToBitmap(getResources().getDrawable(imageIDs[7]));
                            Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);

                            mMap.addMarker(new MarkerOptions().position(pos)
                                    .title(rep.getId())
                                    .snippet(rep.getTitle())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));

                        }
                    }

                    layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                    myList = (RecyclerView) findViewById(R.id.my_recycler_view);
                    adapter = new Recycler_View_Adapter(data, getApplication());
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
                }catch (Exception e){
                    // TODO: 7/12/16 Mostrar pantalla con error de JSON
                }
            }else{
                // TODO: 7/12/16 Mostrar pantalla con error de conexión
            }
            loading.dismiss();
        }
    }

}
