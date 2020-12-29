package com.swayam.uberclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

        location = new SimpleLocation(this,false,false,50);

        adapter = new DriverRecyclerViewAdapter();
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
        query.whereNotContainedIn("username",adapter.getUsers());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                refreshLayout.setRefreshing(false);
                if (e == null){
                    for (ParseObject parseObject : objects){
                        adapter.addUser(parseObject.getString("username"));
                    }
                }else {
                    Toast.makeText(DriverActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void logout(MenuItem item) {

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