package com.darker.motorservice.ui.map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.darker.motorservice.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.darker.motorservice.utility.Constant.LATLNG;
import static com.darker.motorservice.utility.Constant.NAME;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String name, latlng, posOf;
    private int mType, mapType = GoogleMap.MAP_TYPE_NORMAL;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        name = intent.getStringExtra(NAME);
        latlng = intent.getStringExtra(LATLNG);

        posOf = "ตำแหน่งของ " + name;
        getSupportActionBar().setTitle(posOf);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean checkLocationPermission() {
        // In Android 6.0 and higher you need to request permissions at runtime, and the user has
        // the ability to grant or deny each permission. Users can also revoke a previously-granted
        // permission at any time, so your app must always check that it has access to each
        // permission, before trying to perform actions that require that permission. Here, we’re using
        // ContextCompat.checkSelfPermission to check whether this app currently has the
        // ACCESS_COARSE_LOCATION permission

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                // If your app does have access to COARSE_LOCATION, then this method will return
                // PackageManager.PERMISSION_GRANTED//
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // If your app doesn’t have this permission, then you’ll need to request it by calling
                // the ActivityCompat.requestPermissions method//
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // Request the permission by launching Android’s standard permissions dialog.
                // If you want to provide any additional information, such as why your app requires this
                // particular permission, then you’ll need to add this information before calling
                // requestPermission //
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(mapType);
        mType = mMap.getMapType();

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        checkLocationPermission();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        String[] arr = latlng.split(",");
        arr[0] = arr[0].replace("(", "");
        arr[1] = arr[1].replace(")", "");

        double lat = Double.parseDouble(arr[0]);
        double lng = Double.parseDouble(arr[1]);

        LatLng posStore = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(posStore).title(posOf));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posStore));
        Log.w("Location", name + " : " + String.valueOf(lat) + "," + String.valueOf(lng));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("mapType", mType);
        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mapType = savedInstanceState.getInt("mapType");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_normal) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mType = mMap.getMapType();
            return true;
        }else if(id == R.id.menu_hybrid){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mType = mMap.getMapType();
            return true;
        }else if(id == R.id.menu_satellite){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mType = mMap.getMapType();
            return true;
        }else if(id == R.id.menu_terrain){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mType = mMap.getMapType();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
