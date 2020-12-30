package com.swayam.uberclone;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.random.customdialog.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;

public class PassangerLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SimpleLocation location;
    private double passangerLat;
    private  double passangerLon;
    private String passangerName;
    private Toolbar toolbar;
    private boolean ridingThisPassanger = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passanger_location);

        location = new SimpleLocation(this,false,false,0);

        passangerName = getIntent().getStringExtra("username");
        passangerLat = getIntent().getDoubleExtra("lat",0);
        passangerLon = getIntent().getDoubleExtra("lon",0);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(passangerName);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double driverLat = 0;
        double driverLon = 0;

        if (location != null){
            driverLat = location.getLatitude();
            driverLon = location.getLongitude();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker passangerMarker =  mMap.addMarker(new MarkerOptions().position(new LatLng(passangerLat, passangerLon)).title("Passanger is here"));
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(driverLat, driverLon)).title("Driver is here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        ArrayList<Marker> markers = new ArrayList<>();
        markers.add(passangerMarker);
        markers.add(driverMarker);

        for (Marker marker : markers){
            builder.include(marker.getPosition());
        }

        LatLngBounds latLngBounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,0);
        mMap.animateCamera(cameraUpdate);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        location.beginUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        location.endUpdates();
    }

    public void ridePassanger(View view) {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");
        dialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username",passangerName);
        query.whereDoesNotExist("driver");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                if (e == null ){
                    // if user don't have driver then assign this ParseUser as driver
                    if (requests.size() > 0) {
                        ParseObject request = requests.get(0);
                        request.put("driver", ParseUser.getCurrentUser().getUsername());
                        request.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                dialog.dismiss();
                                if (e == null){
                                    showNavigation();
                                }else {
                                    showError(e.getMessage());
                                }
                            }
                        });
                    }else {
                        dialog.dismiss();
                        showError(passangerName+" has already driver or cancelled car request");
                    }
                }else {
                    dialog.dismiss();
                    showError(e.getMessage());
                }
            }
        });

    }

    private void showNavigation(){

        ridingThisPassanger = true;

        Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://maps.google.com/maps?saddr="+
                        passangerLat+","+
                        passangerLon+"&"+"daddr="+
                        location.getLatitude()+","+
                        location.getLongitude()
                )
        );
        startActivity(googleIntent);

    }

    private void showError(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.FAILURE);
        dialog.setTitle("FAILED");
        dialog.setContentText(message);
        dialog.show();
    }

    private void showSuccessfulMessage(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
        dialog.setTitle("SUCCESSFUL");
        dialog.setContentText(message);
        dialog.show();
    }

    public void sendCurrentLocation(View view) {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");
        dialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username",passangerName);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                if (e == null){
                    if (requests.size() > 0){
                        ParseObject request = requests.get(0);
                        if (ParseUser.getCurrentUser().getUsername().equals(request.getString("driver"))){
                            ArrayList list = new ArrayList();
                            list.add(location.getLatitude());
                            list.add(location.getLongitude());
                            request.put("driverLocation",list);
                            request.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    dialog.dismiss();
                                    if (e == null){
                                        Toast.makeText(PassangerLocationActivity.this, "location sent", Toast.LENGTH_SHORT).show();
                                    }else {
                                        showError("Something went wrong");
                                    }
                                }
                            });
                        }else {
                            dialog.dismiss();
                            showError("You are not driver for this passanger");
                        }
                    }else {
                        dialog.dismiss();
                        showError("no such passanger");
                    }
                }else {
                    dialog.dismiss();
                    showError("Something went wrong");
                }
            }
        });

    }
}