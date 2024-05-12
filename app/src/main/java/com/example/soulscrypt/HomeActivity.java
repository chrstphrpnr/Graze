package com.example.soulscrypt;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Auth.LoginActivity;
import com.example.soulscrypt.Constant.API;
import com.example.soulscrypt.NotificationPackages.NotificationActivity;
import com.example.soulscrypt.NotificationPackages.NotificationService;
import com.example.soulscrypt.RelativeList.RelativeAdapter;
import com.example.soulscrypt.RelativeList.RelativeModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView relativeListView;
    private ArrayList<RelativeModel> relativeModelArrayList = new ArrayList<>();
    RelativeAdapter relativeAdapter;
    RequestQueue queue;
    String user_primary_id;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap googleMap;

    private ImageView btnSettings, btnNotification, btnReport;
    private Marker currentMarker = null;

     RelativeLayout btnAddRelatives;

    private ImageView notificationIndicator;

    private BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the visibility of the notification indicator
            notificationIndicator.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startService(new Intent(HomeActivity.this, NotificationService.class));

        // Initialize notification indicator
        notificationIndicator = findViewById(R.id.notification_indicator);

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("com.example.soulscrypt.NEW_NOTIFICATION");
        registerReceiver(newNotificationReceiver, filter);


        btnAddRelatives = findViewById(R.id.btnAddRelatives);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        relativeListView = findViewById(R.id.relativeListView);

        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found
        queue = Volley.newRequestQueue(this);

        bottomSheet();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update the user's current location on the map
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    googleMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_user_marker, 30)));


                }
            }
        };

        populateRelativeList();


        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                    finishAffinity();

                }

            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);


        btnSettings = findViewById(R.id.btnSettings);
        btnNotification = findViewById(R.id.btnNotification);
        btnReport = findViewById(R.id.btnReport);


        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, Settings.class));

            }
        });

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));

            }
        });


        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, Report.class));

            }
        });


        btnAddRelatives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddRelative.class));
            }
        });



    }

    private void bottomSheet() {
        FrameLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        int peekHeight = (int) (screenHeight * 0.15);

        bottomSheetBehavior.setPeekHeight(peekHeight);
        bottomSheet.setMinimumHeight(peekHeight);

        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = peekHeight;
        bottomSheet.setLayoutParams(layoutParams);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                int screenHeight = metrics.heightPixels;

                int peekHeight = (int) (screenHeight * 0.15);
                int expandedHeight = (int) (screenHeight * 0.85);

                int newHeight;
                if (slideOffset > 0) {
                    newHeight = (int) (peekHeight + slideOffset * (expandedHeight - peekHeight));
                } else {
                    newHeight = (int) (expandedHeight + slideOffset * (peekHeight - expandedHeight));
                }

                newHeight = Math.min(newHeight, expandedHeight);
                newHeight = Math.max(newHeight, peekHeight);

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = newHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(false);


        LatLng pasig_cementery = new LatLng(14.568566970199212, 121.07937723366804);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pasig_cementery, 19f));

        // Enable zoom controls
        googleMap.setPadding(20, 10, 900, 2010);


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update location every 5 seconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        unregisterReceiver(newNotificationReceiver);

    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsedDate = sdf.parse(date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void populateRelativeList() {

        relativeAdapter = new RelativeAdapter(this, relativeModelArrayList);
        relativeAdapter.setOnItemClickListener(new RelativeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(double latitude, double longitude) {
                // Remove the previous marker if it exists
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                // Move the map camera to the clicked location
                LatLng location = new LatLng(latitude, longitude);
                googleMap.setPadding(0, 0, 0, 0);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 19f));

                // Add a marker at the clicked location with the custom marker icon
                currentMarker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_relative_marker, 60)));

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        relativeListView.setLayoutManager(layoutManager);
        relativeListView.setAdapter(relativeAdapter);

        StringRequest request = new StringRequest(Request.Method.POST, API.relative_list_api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        JSONArray relativesArray = jsonResponse.getJSONArray("relatives");
                        for (int i = 0; i < relativesArray.length(); i++) {
                            JSONObject relativeObject = relativesArray.getJSONObject(i);
                            String relative_name = relativeObject.getString("full_name");
//                            String relative_death_date = relativeObject.getString("date_of_death");
                            String relative_death_date = formatDate(relativeObject.getString("date_of_death"));

                            String relative_section = relativeObject.getString("section");
                            double section_latitude = relativeObject.getDouble("latitude");
                            double section_longitude = relativeObject.getDouble("longitude");
                            int record_id = relativeObject.getInt("record_id");

                            RelativeModel relativeModel = new RelativeModel(relative_name, relative_death_date, relative_section, section_latitude, section_longitude, record_id);
                            relativeModelArrayList.add(relativeModel);
                        }
                        relativeAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("RelativeList", "Server response indicates failure");
                    }
                } catch (JSONException e) {
                    Log.e("RelativeList", "Error parsing JSON response: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RelativeList", "Volley error: " + error.getMessage());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", user_primary_id);
                return map;
            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    // Method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Method to convert a drawable resource to a BitmapDescriptor with a specified size in dp
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int sizeInDp) {
        int sizeInPixels = dpToPx(sizeInDp);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, sizeInPixels, sizeInPixels);
        Bitmap bitmap = Bitmap.createBitmap(sizeInPixels, sizeInPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }




}


