package com.example.raymond.raindrop;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.raymond.raindrop.persistence.AsyncWriter;
import com.example.raymond.raindrop.persistence.posts.PostRepository;
import com.example.raymond.raindrop.persistence.posts.PostStorable;
import com.example.raymond.raindrop.persistence.users.UserRepository;
import com.example.raymond.raindrop.persistence.users.UserStorable;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // Map Variables
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private LocationManager mLocationManager;
    private Location mLastKnownLocation;
    String mLocationProvider = LocationManager.NETWORK_PROVIDER;

    // Default Map Variables
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;

    // Map Posts
    private static final PostRepository postRepository = new PostRepository();
    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;

    //For moving to other activities
    private String userEmail = null;

    // For Logging out
    // Configure sign-in to request the user's ID, email address, and basic profile
    private GoogleSignInOptions gso;
    // Build a GoogleApiClient with access to the Google Sign-In API
    private GoogleApiClient mGoogleApiClient;

    //For the reporting system
    private String lastPostId = null;

    //For the upvote and downvote system
    private String lastPostUserEmail = null;

    private static final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get content view to render map
        setContentView(R.layout.activity_maps);

        // Create locationManager
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        // Setup Bottom Sheet
        initializeBottomSheet();

        // Setup floating action buttons
        setupFloatingActionButtons();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        intent.putExtra("userEmail", userEmail);// if its string type
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();

        // Get email
        if(getIntent().getExtras() != null)
        {
            userEmail = getIntent().getStringExtra("userEmail");
            System.out.println("we passed the user email:" + userEmail);
        }
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

        // Disable google maps toolbar when clicking markers
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Point mapPoint = mMap.getProjection().toScreenLocation(marker.getPosition());
                mapPoint.set(mapPoint.x, mapPoint.y+250);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mMap.getProjection().fromScreenLocation(mapPoint)),
                        1000, null);

                updateBottomSheetContent(marker);
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Prompt for user permissions
        getLocationPermission();

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        // Get the current location of the device and set position of map
        getDeviceLocation();
    }

    // Create JsonObject for info window
    private JSONObject createJSON(PostStorable p) {
        JSONObject json = new JSONObject();

        try {
            json.put("currentLocation", false);
            json.put("outOfRange", false);
            json.put("postId", p.getPostId());
            json.put("username", p.getEmail());
            json.put("karma", p.getKarma());
            json.put("message", p.getMedia());
            json.put("expiration", p.getExpires());
        } catch (JSONException e) {
            Log.e("Exception: %s", e.getMessage());
        }

        return json;
    }

    // Get the current location of the device
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                    }
                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }
                    @Override
                    public void onProviderEnabled(String s) {
                    }
                    @Override
                    public void onProviderDisabled(String s) {
                    }
                });
                mLocationManager.getLastKnownLocation(mLocationProvider);
                mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
                if (mLastKnownLocation != null) {
                    // Clear map of previous markers
                    mMap.clear();

                    // Set the map's camera position to the current location of the device.
                    LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude());

                    mCameraPosition = new CameraPosition(latLng, DEFAULT_ZOOM, mMap.getCameraPosition().tilt,
                            mMap.getCameraPosition().bearing);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition),
                            2000, null);

                    // Get current location posts and display them
                    getLocationPosts(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Prompts the user for permission to use the device location
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Gets posts from posts database and displays them on the map
    private void getLocationPosts(double latitude, double longitude) {
        List<PostStorable> nearbyPosts = postRepository.scanCloseEntries(latitude, longitude);
        List<PostStorable> farPosts = postRepository.scanFarEntries(latitude, longitude);

        for (PostStorable p : nearbyPosts) {
            LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
            if (p.getEmail().equals(userEmail)) {
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .snippet(createJSON(p).toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            } else {
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .snippet(createJSON(p).toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            }
        }
        for (PostStorable p : farPosts) {
            LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
            if (!nearbyPosts.contains(p)) {
                if (p.getEmail().equals(userEmail)) {
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .alpha(0.5f)
                            .snippet("{\"outOfRange\": true}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                } else {
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .alpha(0.5f)
                            .snippet("{\"outOfRange\": true}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                }
            }
        }
    }

    // Initialize Bottom Sheet for posts
    public void initializeBottomSheet() {
        bottomSheet = findViewById(R.id.coordinator).findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (bottomSheetBehavior != null)
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    //showing the different states
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // React to dragging events

                }
            });
    }

    // Setup all floating action buttons and other buttons
    private void setupFloatingActionButtons() {
        final FloatingActionButton fabGetLocation = (FloatingActionButton) findViewById(R.id.fabGetLocation);
        fabGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        final FloatingActionButton fabAddPost = (FloatingActionButton) findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, PostActivity.class);
                intent.putExtra("userEmail", userEmail);// if its string type
                startActivity(intent);
            }
        });

        final FloatingActionButton fabReport = (FloatingActionButton) findViewById(R.id.fabReport);
        fabReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastPostId != null) {
                    Intent intent = new Intent(MapsActivity.this, ReportActivity.class);
                    intent.putExtra("userEmail", userEmail);// if its string type
                    intent.putExtra("postId", lastPostId);
                    startActivity(intent);
                }
                else {
                    System.out.println("ERROR: lastPostId = null");
                }
            }
        });

        final FloatingActionButton fabProfile = (FloatingActionButton) findViewById(R.id.fabProfile);
        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabProfile.setSelected(!fabProfile.isSelected());
                fabProfile.setImageResource(fabProfile.isSelected() ? R.drawable.ic_close
                        : R.drawable.ic_account_circle);

                // Fetch karma
                Integer karma = userRepository.read(new UserStorable(userEmail, 0)).getKarma();
                System.out.println("fetched karma:" + karma);

                final TextView s = (TextView)findViewById(R.id.karmaText);
                s.setText(karma.toString());

                if (fabProfile.isSelected()) {
                    FrameLayout profOverlay = (FrameLayout) findViewById(R.id.profOverlay);
                    profOverlay.setVisibility(view.VISIBLE);
                    fabGetLocation.setVisibility(view.INVISIBLE);
                    fabAddPost.setVisibility(view.INVISIBLE);
                    bottomSheet.setVisibility(view.INVISIBLE);
                    mMap.getUiSettings().setAllGesturesEnabled(false);

                    ValueAnimator animator = new ValueAnimator();
                    animator.setObjectValues(0, karma);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            s.setText(String.valueOf(animation.getAnimatedValue()));
                        }
                    });
                    animator.setEvaluator(new TypeEvaluator<Integer>() {
                        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                            return Math.round(startValue + (endValue - startValue) * fraction);
                        }
                    });
                    animator.setDuration(2000);
                    animator.start();
                } else {
                    FrameLayout profOverlay = (FrameLayout) findViewById(R.id.profOverlay);
                    profOverlay.setVisibility(view.INVISIBLE);
                    fabGetLocation.setVisibility(view.VISIBLE);
                    fabAddPost.setVisibility(view.VISIBLE);
                    bottomSheet.setVisibility(view.VISIBLE);
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                }
            }
        });

        final TextView karmaUi = (TextView) bottomSheet.findViewById(R.id.karma);
        final FloatingActionButton fabDownVote = (FloatingActionButton) findViewById(R.id.fabDownVote);
        final FloatingActionButton fabUpVote = (FloatingActionButton) findViewById(R.id.fabUpVote);
        fabUpVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabUpVote.setRippleColor(getResources().getColor(R.color.colorPrimary));
                fabUpVote.setSelected(!fabUpVote.isSelected());

                if (fabDownVote.isSelected() && fabUpVote.isSelected()) {
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    fabDownVote.setSelected(false);
                    voteOnPost(2, true);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) + 2));
                }
                else if (fabUpVote.isSelected()) {
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    voteOnPost(1, true);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) + 1));
                } else {
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    voteOnPost(-1, false);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) - 1));
                }
            }
        });

        fabDownVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabDownVote.setRippleColor(getResources().getColor(R.color.colorPrimary));
                fabDownVote.setSelected(!fabDownVote.isSelected());

                if (fabUpVote.isSelected() && fabDownVote.isSelected()) {
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    fabUpVote.setSelected(false);
                    voteOnPost(-2, true);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) - 2));
                }
                else if (fabDownVote.isSelected()) {
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    voteOnPost(-1, true);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) - 1));
                } else {
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    voteOnPost(1, false);
                    karmaUi.setText(String.valueOf(Integer.parseInt(karmaUi.getText().toString()) + 1));
                }
            }
        });

        Button signOutButton = (Button) findViewById(R.id.buttonSignOut);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    }
                });
    }

    public void voteOnPost(int karmaToAdd, boolean addToVoters) {
        System.out.println("email is: " + lastPostUserEmail);
        System.out.println("postId is: " + lastPostId);
        System.out.println("adding this karma: " + karmaToAdd);

        //Add karma to post
        PostStorable postToUpvote = postRepository.read(lastPostId);
        int karma = postToUpvote.getKarma();

        Set<String> upVoters = postToUpvote.getUpVoters();
        Set<String> downVoters = postToUpvote.getDownVoters();
        if(upVoters == null) {
            upVoters = new HashSet<String>();
            upVoters.add("joe");
        }
        if(downVoters == null) {
            downVoters = new HashSet<String>();
            downVoters.add("joe");
        }
        if(karmaToAdd > 0) {
            System.out.println(postToUpvote.toString() + " receiving karma");
            postToUpvote.setKarma(postToUpvote.getKarma() + karmaToAdd);
            System.out.println(postToUpvote.toString() + " is the new post");
            if (!addToVoters) {
                downVoters.remove(userEmail);
            } else {
                upVoters.add(userEmail);
                downVoters.remove(userEmail);
            }
            postToUpvote.setUpVoters(upVoters);
            postToUpvote.setDownVoters(downVoters);
            postRepository.write(postToUpvote);
            //Add karma to user
            UserStorable userToUpvote = userRepository.read(lastPostUserEmail);
            System.out.println(userToUpvote.toString() + " receiving karma");
            userToUpvote.setKarma(userToUpvote.getKarma() + karmaToAdd);
            System.out.println(userToUpvote.toString() + " is the result");
            AsyncWriter.write(UserRepository.getTableName(), userToUpvote.generateMapItem());
        }
        else {
            System.out.println(postToUpvote.toString() + " receiving karma");
            postToUpvote.setKarma(postToUpvote.getKarma() + karmaToAdd);
            System.out.println(postToUpvote.toString() + " is the new post");
            if (!addToVoters) {
                upVoters.remove(userEmail);
            } else {
                upVoters.remove(userEmail);
                downVoters.add(userEmail);
            }
            postToUpvote.setUpVoters(upVoters);
            postToUpvote.setDownVoters(downVoters);
            postRepository.write(postToUpvote);
            //Add karma to user
            UserStorable userToUpvote = userRepository.read(lastPostUserEmail);
            System.out.println(userToUpvote.toString() + " receiving karma");
            userToUpvote.setKarma(userToUpvote.getKarma() + karmaToAdd);
            System.out.println(userToUpvote.toString() + " is the result");
            AsyncWriter.write(UserRepository.getTableName(), userToUpvote.generateMapItem());
        }
    }

    // Use marker information in bottom sheet
    private void updateBottomSheetContent(Marker marker) {
        try {
            JSONObject json = new JSONObject(marker.getSnippet());

            if (json.getBoolean("outOfRange") || json.getBoolean("currentLocation")) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            if(json.has("postId")) {
                lastPostId = json.getString("postId");
            } else {
                throw new AssertionError("There should be a postId in the Marker json!");
            }

            // update upvote buttons
            PostStorable post = postRepository.read(lastPostId);
            // update json on marker click / when viewing post
            json = createJSON(post);
            final FloatingActionButton fabDownVote = (FloatingActionButton) findViewById(R.id.fabDownVote);
            final FloatingActionButton fabUpVote = (FloatingActionButton) findViewById(R.id.fabUpVote);
            if (post.getUpVoters() != null) {
                if (post.getUpVoters().contains(userEmail)) {
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    fabUpVote.setSelected(true);
                    fabDownVote.setSelected(false);
                } else if (post.getDownVoters().contains(userEmail)) {
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    fabDownVote.setSelected(true);
                    fabUpVote.setSelected(false);
                } else {
                    fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                    fabDownVote.setSelected(false);
                    fabUpVote.setSelected(false);
                }
            } else { // Reset upvote buttons
                fabUpVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                fabDownVote.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorNeutral)));
                fabDownVote.setSelected(false);
                fabUpVote.setSelected(false);
            }

            TextView usernameUi = (TextView) bottomSheet.findViewById(R.id.username);
            if (json.has("username")) {
                String username = json.getString("username");
                usernameUi.setText(username);
                lastPostUserEmail = username;
            } else {
                usernameUi.setText("");
            }

            TextView messageUi = (TextView) bottomSheet.findViewById(R.id.message);
            if (json.has("message")) {
                String message = json.getString("message");
                messageUi.setText(message);
            } else {
                messageUi.setText("");
            }

            TextView karmaUi = (TextView) bottomSheet.findViewById(R.id.karma);
            if (json.has("karma")) {
                String karma = json.getString("karma");
                karmaUi.setText(karma);
            } else {
                karmaUi.setText("1");
            }

            TextView expirationUi = (TextView) bottomSheet.findViewById(R.id.expiration);
            if (json.has("expiration")) {
                Long expiration = Long.valueOf(json.getString("expiration"));
                System.out.println("expriation: " + expiration);
                Long postTime = expiration - 86400;
                System.out.println("post time: " + postTime);
                Long currentTime = System.currentTimeMillis()/1000;
                System.out.println("current time: " + currentTime);
                Long timePassed = currentTime - postTime;
                System.out.println("time passed: " + timePassed);

                //convert to hours passed if > 0 else convert to minutes
                if (timePassed/3600 > 0) {
                    timePassed = timePassed/3600;
                    if (timePassed / 1 == 1) {
                        expirationUi.setText(timePassed + " hour ago");
                    } else {
                        expirationUi.setText(timePassed + " hours ago");
                    }
                } else {
                    timePassed = timePassed/60;
                    if (timePassed / 1 == 1) {
                        expirationUi.setText(timePassed + " minute ago");
                    } else {
                        expirationUi.setText(timePassed + " minutes ago");
                    }
                }

            } else {
                expirationUi.setText("");
            }

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } catch (JSONException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
