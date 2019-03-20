package com.ufo.ufomobile.reportesmoviles;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.ufo.ufomobile.reportesmoviles.MenuActivity.drawableToBitmap;

/**
 * Created by Tiago on 9/08/16.
 */
public class AddressViewDialogFragment extends DialogFragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment fragment;
    LocationManager locationManager;
    double longitude,latitude;
    private LatLng latiLong;
    private MarkerOptions markerOptions;
    private String address;
    private int resource;

    public AddressViewDialogFragment(){
        fragment = new SupportMapFragment();
    }

    public static AddressViewDialogFragment newInstance(double lat, double lng, String addr, int rec){
        AddressViewDialogFragment f = new AddressViewDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("latitude", lat);
        args.putDouble("longitude", lng);
        args.putString("address",addr);
        args.putInt("resource",rec);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout to use as dialog or embedded fragment
        View view=inflater.inflate(R.layout.address_view_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        latitude=getArguments().getDouble("latitude");
        longitude=getArguments().getDouble("longitude");
        address=getArguments().getString("address");
        resource=getArguments().getInt("resource");
        //Map ---------------------------------------------------------------------------------
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, fragment).commit();
        fragment.getMapAsync(this);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bitmap b1 = drawableToBitmap(getResources().getDrawable(resource));
        Bitmap bhalfsize1 = Bitmap.createScaledBitmap(b1, b1.getWidth(), b1.getHeight(), false);
        LatLng latLng = new LatLng(latitude,longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        mMap.addMarker(new MarkerOptions().position(latLng)
                                            .title(address)
                                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize1)));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
