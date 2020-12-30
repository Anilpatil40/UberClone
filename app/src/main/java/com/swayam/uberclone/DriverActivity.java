package com.swayam.uberclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import im.delight.android.location.SimpleLocation;

public class DriverActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private DriverRecyclerViewAdapter adapter;
    private SimpleLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        location = new SimpleLocation(this,false,false,0);

        location.setListener(new SimpleLocation.Listener() {
            @Override
            public void onPositionChanged() {
                adapter.setDriverLocation(location.getPosition());
            }
        });

        adapter = new DriverRecyclerViewAdapter();
        adapter.setOnItemSelectListener(new DriverRecyclerViewAdapter.OnItemSelectListener() {
            @Override
            public void selectedItem(String username, double lat, double lon) {
                Intent intent = new Intent(DriverActivity.this,PassangerLocationActivity.class);
                intent.putExtra("username",username);
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        refreshLayout = findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        refreshList();

    }

    private void refreshList(){
        refreshLayout.setRefreshing(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PassangerRequests");
        query.whereNear("point",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
        query.whereDoesNotExist("driver");
        adapter.clear();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                refreshLayout.setRefreshing(false);
                if (e == null){
                    for (ParseObject parseObject : objects){
                        adapter.addUser(parseObject);
                    }
                }else {
                    Toast.makeText(DriverActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void logout(MenuItem item) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    startActivity(new Intent(DriverActivity.this,MainActivity.class));
                    finish();
                }else {

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
}