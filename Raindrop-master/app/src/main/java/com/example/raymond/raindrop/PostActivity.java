package com.example.raymond.raindrop;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.raymond.raindrop.persistence.AsyncWriter;
import com.example.raymond.raindrop.persistence.posts.PostRepository;
import com.example.raymond.raindrop.persistence.posts.PostStorable;

public class PostActivity extends AppCompatActivity {
    private LocationManager locationManager;

    private String userEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        onResume();
        cancel();
        submit();
    }

    @Override
    public void onResume(){
        super.onResume();

        System.out.println("joe is one sexy mofo.");

        // Get email
        if( getIntent().getExtras() != null)
        {
            userEmail = getIntent().getStringExtra("userEmail");
        }

        System.out.println("we passed the user email:" + userEmail);
    }

    private void cancel() {
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMaps();
            }
        });
    }

    private void submit() {
        Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
    }


    public void handleSubmit() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        System.out.println("Entered handle submit");
        try {
            System.out.println("Entered try");
            //get Location
            locationManager.getLastKnownLocation(locationProvider);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

            System.out.println("location found, post looks like: " +
            new PostStorable(
                    generatePostId(),
                    userEmail,
                    0,
                    0,
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude(),
                    System.currentTimeMillis()/1000 + 86400,
                    ((EditText) findViewById(R.id.etMessage)).getText().toString()).toString().toString());

            //write to db
            AsyncWriter.write(PostRepository.getTableName(), new PostStorable(
                    userEmail + "/" + Long.toString(System.currentTimeMillis()) ,
                    userEmail,
                    0,
                    0,
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude(),
                    System.currentTimeMillis()/1000 + 86400,
                    ((EditText) findViewById(R.id.etMessage)).getText().toString()).generateMapItem());
        } catch(SecurityException e) {
            System.out.println("Couldn't check the user location");
        }
        switchToMaps();
    }

    public void switchToMaps() {
        // Sending over userEmail to access the MapActivity
        Intent intent = new Intent(PostActivity.this, MapsActivity.class);
        intent.putExtra("userEmail", userEmail);// if its string type
        startActivity(intent);
    }

    public String generatePostId() {
        return userEmail + "/" + Long.toString(System.currentTimeMillis());
    }
}
