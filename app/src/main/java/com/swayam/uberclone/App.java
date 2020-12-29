package com.swayam.uberclone;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // Initializes Parse SDK as soon as the application is created
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("664P1q1tdB6VBUXbIUEomsoz1e5S6pRpKdpZeRa0")
                .clientKey("mKvIo7bLzjx3LJdJddCIlJLyzKTVNNnjrgiiboxP")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}