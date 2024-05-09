package com.example.soulscrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.example.soulscrypt.Auth.LoginActivity;
import com.example.soulscrypt.Auth.RegisterActivity;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        startActivity(new Intent(MainActivity.this, LoginActivity.class));


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.w("WHATHAPPEN", "LOCATION GRANTED");
            if (!isLocationEnabled()) {
                showLocationServicesDialog();
            } else {
                proceedAfterPermission();
            }
        }

    }

    private void proceedAfterPermission() {

        SharedPreferences userPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.w("WHATHAPPEN", "LOCATION GRANTED");
                if (!isLocationEnabled()) {
                    showLocationServicesDialog();
                } else {
                    proceedAfterPermission();
                }
            } else {
                // Inform the user that the permission is necessary and consider directing them to settings
                Log.w("WHATHAPPEN", "LOCATION PERMISSION DENIED");
                // Handle the denial appropriately
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Required");
        builder.setMessage("This app requires location permission to function properly. Please enable it in the app settings.");

        builder.setPositiveButton("App Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked App Settings button
                openAppSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog, handle as you see fit
                dialog.dismiss();
                checkLocationPermissionAgain();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkLocationPermissionAgain() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDeniedDialog();
        } else {
            if (!isLocationEnabled()) {
                showLocationServicesDialog();
            } else {
                proceedAfterPermission();
            }
        }
    }

    private void openAppSettings() {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!isLocationEnabled()) {
                showLocationServicesDialog();
            } else {
                Log.w("WHATHAPPEN", "Location services enabled");
                if (!isLocationEnabled()) {
                    showLocationServicesDialog();
                } else {
                    proceedAfterPermission();
                }
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationServicesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Services Disabled");
        builder.setMessage("Please enable location services for this app to function properly.");

        builder.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                dialog.dismiss();
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                checkLocationPermissionAgain();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




}