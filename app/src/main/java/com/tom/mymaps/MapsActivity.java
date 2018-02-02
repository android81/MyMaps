package com.tom.mymaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_LOCATION = 2;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            //自定義InfoWindow
            mMap.setInfoWindowAdapter(
                    new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            View view = getLayoutInflater().inflate(R.layout.info_window, null);
                            TextView title = view.findViewById(R.id.info_title);
                            title.setText("Title:" + marker.getTitle());
                            TextView snippet = view.findViewById(R.id.info_snippet);
                            snippet.setText("說明:" + marker.getSnippet());
                            return view;

                        }
                    });
            mMap.setOnMarkerClickListener(
                    new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            //自行加入程式碼
                            new AlertDialog.Builder(MapsActivity.this)
                                    .setTitle(marker.getTitle())
                                    .setMessage(marker.getSnippet())
                                    .setPositiveButton("OK", null)
                                    .show();
                            return true;

                        }
                    });

            //台北101的位置
            LatLng taipei101 = new LatLng(25.033408, 121.564099);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    taipei101, 15));
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(taipei101)
                    .title("101")
                    .snippet("這是台北101"));
            marker.showInfoWindow();
//                    .setIcon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.map_marker));
            /*setupMyLocation();
                            createLocationRequest();
                            fuseLocationRequest();*/
        }
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    @SuppressLint("MissingPermission")
    private void setupMyLocation() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(
                new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        //透過位置服務，取得目前裝置所在
                        fuseLocation();
//                        gpsLocation();
                        return false;
                    }
                });
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    private void fuseLocationRequest() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        client.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        Log.i("UPDATE", location.toString());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(),
                                        location.getLongitude())
                                , 15));
                    }
                }
                , null);
    }

    @SuppressLint("MissingPermission")
    private void fuseLocation() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnCompleteListener(
                this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(),
                                            location.getLongitude())
                                    , 15));
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void gpsLocation() {
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = "gps";
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            Log.i("LOCATION", location.getLatitude() + "/"
                    + location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude())
                    , 15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //使用者允許權限
                    setupMyLocation();
                } else {
                    //使用者拒絕授權, 停用MyLocation功能
                }
                break;
        }
    }
}
