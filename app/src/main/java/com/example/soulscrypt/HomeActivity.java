package com.example.soulscrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
//    s
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        FrameLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenHeight = metrics.heightPixels;


        // Calculate the peek height (500 SDP)
//        int peekHeight = 300;
        int peekHeight = (int) (screenHeight * 0.05);

        // Set the initial peek height of the bottom sheet
        bottomSheetBehavior.setPeekHeight(peekHeight);
        bottomSheet.setMinimumHeight(peekHeight);


        // Set the new height of the bottom sheet
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = peekHeight;
        bottomSheet.setLayoutParams(layoutParams);

        // Set the initial state of the bottom sheet to expanded
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

                // Calculate the peek height (25% of the screen height)
                int peekHeight = (int) (screenHeight * 0.05);

                // Calculate the expanded height (85% of the screen height)
                int expandedHeight = (int) (screenHeight * 0.85);

                // Calculate the new height of the bottom sheet based on the slide offset
                int newHeight;
                if (slideOffset > 0) {
                    newHeight = (int) (peekHeight + slideOffset * (expandedHeight - peekHeight));
                } else {
                    newHeight = (int) (expandedHeight + slideOffset * (peekHeight - expandedHeight));
                }

                // Limit the new height to the maximum allowed height (85% of the screen height)
                newHeight = Math.min(newHeight, expandedHeight);

                // Limit the new height to the minimum allowed height (25% of the screen height)
                newHeight = Math.max(newHeight, peekHeight);

                // Set the new height of the bottom sheet
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = newHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }



        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng pasig_cementery = new LatLng(14.56906330839855, 121.08088743318035);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pasig_cementery, 15f));

        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(20, 10, 900, 2050);

    }

}
