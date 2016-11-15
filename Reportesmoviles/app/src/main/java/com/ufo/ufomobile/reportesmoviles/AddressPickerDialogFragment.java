package com.ufo.ufomobile.reportesmoviles;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utilities.Category;

/**
 * Created by 'Santiago on 9/8/2016.
 */
public class AddressPickerDialogFragment extends DialogFragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment fragment;
    private EditText address;
    private ImageButton addAddress;
    LocationManager locationManager;
    double longitude,latitude,longitude_rep,latitude_rep=0;
    private LatLng latiLong;
    private MarkerOptions markerOptions;
    OnaAddSelected mListener;

    public interface OnaAddSelected {
        public void onArticleSelectedListener(String addr,double latitude,double longitude);
    }

    public AddressPickerDialogFragment(){
        fragment = new SupportMapFragment();
    }

    public static AddressPickerDialogFragment newInstance(){
        AddressPickerDialogFragment f = new AddressPickerDialogFragment();
;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout to use as dialog or embedded fragment
        View view=inflater.inflate(R.layout.address_picker_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        address = (EditText) view.findViewById(R.id.address);
        addAddress = (ImageButton) view.findViewById(R.id.add_address);
        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addr=address.getText().toString();
                if(!addr.equals("")){
                    mListener.onArticleSelectedListener(addr,latitude_rep,longitude_rep);
                    dismiss();
                }else{
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.address_no_selected_error),Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Map ---------------------------------------------------------------------------------
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, fragment).commit();
        fragment.getMapAsync(this);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnaAddSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Location ---------------------------------------------------------------
        mMap.setMyLocationEnabled(true);
        find_Location(getActivity().getApplicationContext());
        final LatLng latLng = new LatLng(latitude, longitude);
        try {
            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude, 1);
            latitude_rep=latitude;
            longitude_rep=longitude;
            address.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Marker ---------------------------------------------------------------
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                LatLng newLoc = marker.getPosition();
                Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(newLoc.latitude,newLoc.longitude, 1);
                    latitude_rep=newLoc.latitude;
                    longitude_rep=newLoc.longitude;
                    address.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                LatLng newLoc = marker.getPosition();
                Geocoder geocoder = new Geocoder(getActivity().
                        getApplicationContext(), Locale.getDefault());
                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(newLoc.latitude,newLoc.longitude, 1);
                    address.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
                    address.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        mMap.addMarker(new MarkerOptions().position(latLng)
                .draggable(true));
        //Camera ---------------------------------------------------------------
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(13)
                .bearing(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void find_Location(Context con) {
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) con.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    //Address search task --------------------------------------------------------------------------

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity().getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getActivity().getBaseContext(), "Dirección no encontrada", Toast.LENGTH_SHORT).show();
            }else {

                // Clears all the existing markers on the map
                mMap.clear();

                // Adding Markers on Google Map for each matching address
                for (int i = 0; i < addresses.size(); i++) {

                    Address address = (Address) addresses.get(i);

                    // Creating an instance of GeoPoint, to display in Google Map
                    latiLong = new LatLng(address.getLatitude(), address.getLongitude());

                    String addressText = String.format("%s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getCountryName());

                    markerOptions = new MarkerOptions();
                    markerOptions.position(latiLong);
                    markerOptions.draggable(true);

                    mMap.addMarker(markerOptions);

                    // Locate the first location
                    if (i == 0)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latiLong));
                }
            }
        }
    }
}
