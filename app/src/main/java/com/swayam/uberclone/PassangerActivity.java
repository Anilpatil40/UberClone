package com.swayam.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nikartm.button.FitButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.LogOutCallback;
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

import static android.Manifest.*;

public class PassangerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SimpleLocation location;
    private FitButton fitButton;
    private ProgressDialog dialog;
    private boolean isRequested = false;
    private boolean driverAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passanger);

        fitButton = findViewById(R.id.fitButton);

        location = new SimpleLocation(this,false,false,0);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Connecting...");
        dialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dialog.dismiss();
                if (e == null){
                    if (objects.size() > 0){
                        setRequest(true);
                    }else {
                        setRequest(false);
                    }
                }else {
                    showError(e.getMessage());
                }
            }
        });
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!location.hasLocationEnabled())
            return;

        location.setListener(new SimpleLocation.Listener() {
            @Override
            public void onPositionChanged() {
                updateCameraPassangerLocation(location);
            }
        });

        if (Build.VERSION.SDK_INT < 23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                updateCameraPassangerLocation(location);
            }
        }else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                location.beginUpdates();
            }else {
                ActivityCompat.requestPermissions(this,new String[]{permission.ACCESS_FINE_LOCATION},1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    location.beginUpdates();
                    updateCameraPassangerLocation(location);
                }
            }
        }
    }

    private void updateCameraPassangerLocation(SimpleLocation location){
        if (location == null)
            return;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(new MarkerOptions().position(latLng).title("you are here"));
    }

    public void onButtonClick(View view) {
        if (isRequested){
            cancelCar();
        }else {
            requestCar();
        }
    }

    private void cancelCar(){
        dialog.setMessage("Cancelling...");
        dialog.show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size()>0){
                    objects.get(0).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            dialog.dismiss();
                            if (e == null){
                                Toast.makeText(PassangerActivity.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                                setRequest(false);
                            }else {
                                showError("Something went wrong");
                            }
                        }
                    });
                }else {
                    dialog.dismiss();
                    showError("Something went wrong");
                }
            }
        });
    }

    private void requestCar(){
        if (!location.hasLocationEnabled())
            return;
        dialog.setMessage("Requesting...");
        dialog.show();

        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),location.getLatitude());
        ParseObject parseObject = new ParseObject("PassangerRequests");
        parseObject.put("username",ParseUser.getCurrentUser().getUsername());
        parseObject.put("point",point);

        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null){
                    setRequest(true);
                    Toast.makeText(PassangerActivity.this, "car requested", Toast.LENGTH_SHORT).show();
                }else {
                    showError("Something went wrong");
                }
            }
        });
    }

    private void setRequest(boolean requestStatus){
        if (requestStatus){
            fitButton.setText("Cancel Car");
            isRequested = true;
        }else {
            fitButton.setText("Request Car");
            isRequested = false;
        }
        //To set width of label in FitButton to match parent
        TextView textView = (TextView)fitButton.getChildAt(0);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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

    public void logout(View view) {
        dialog.setMessage("Logging out...");
        dialog.show();

        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null){
                    startActivity(new Intent(PassangerActivity.this,MainActivity.class));
                    finish();
                }else {
                    showError("Something went wrong");
                }
            }
        });
    }

    public void refresh(View view) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");
        dialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                dialog.dismiss();
                if (e == null){
                    if (requests.size()>0){
                        ParseObject request = requests.get(0);
                        if (request.getString("driver") != null){
                            ArrayList<Double> list = (ArrayList) request.getList("driverLocation");

                            updateDriverLocation(request.getString("username"),list.get(0),list.get(1));
                        }else {
                            Toast.makeText(PassangerActivity.this, "no driver accepted your request", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        showError("You did not order car please book a car");
                    }
                }else {
                    Toast.makeText(PassangerActivity.this, "Something wnt wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateDriverLocation(String username,double lat,double lon){

        double distance = SimpleLocation.calculateDistance(location.getLatitude(),location.getLongitude(),lat,lon);
        distance = Math.round(distance);

        if (distance < 10){
            CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
            dialog.setTitle("SUCCESSFUL");
            dialog.setContentText(username + " is 10 m away from you");
            dialog.setPositiveButton("END REQUEST", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteRequestAnsRefreshActivity();
                }
            });
            dialog.show();
            return;
        }

        distance = distance/1000;

        Toast.makeText(PassangerActivity.this,
                "Your driver is "+username+"\n"+distance+" KM away from you"
                , Toast.LENGTH_SHORT).show();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker passangerMarker =  mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Passanger is here"));
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Driver is here")
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

    private void deleteRequestAnsRefreshActivity() {
        dialog.setMessage("please wait...");
        dialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                if (e == null && requests.size()>0){
                    ParseObject request = requests.get(0);
                    request.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            dialog.dismiss();
                            if (e == null){
                                startActivity(new Intent(PassangerActivity.this,PassangerActivity.class));
                                finish();
                            }else {
                                showError("Something went wrong");
                            }
                        }
                    });
                }else {
                    dialog.dismiss();
                    showError("Something went wrong");
                }
            }
        });
    }
}