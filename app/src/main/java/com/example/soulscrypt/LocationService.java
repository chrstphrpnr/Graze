package com.example.soulscrypt;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Constant.API;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double currentUserLatitude = 0.0;
    private double currentUserLongitude = 0.0;

    private double lastSentLatitude = 0.0;
    private double lastSentLongitude = 0.0;

    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 123;
    private SharedPreferences userPref, locationPref;
    private RequestQueue requestQueue;
    private final Handler serviceHandler = new Handler(Looper.getMainLooper());
    private boolean isNotificationRunnablePosted = false;

    private KalmanFilter latitudeFilter = new KalmanFilter();
    private KalmanFilter longitudeFilter = new KalmanFilter();
    String user_primary_id;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize location-related components here
        Log.w("WHATHAPPEN", "oncreateLOCATION");
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found
        locationPref = getSharedPreferences("location", Context.MODE_PRIVATE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        requestQueue = Volley.newRequestQueue(this);


    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking location in background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // This makes the notification un-clearable
                .setSound(null)
                .setVibrate(null)
                .setSilent(true);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setSound(null, null);
            serviceChannel.enableVibration(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createLocationRequest() {
        Log.w("TAG", "request");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        latitudeFilter = new KalmanFilter();
        longitudeFilter = new KalmanFilter();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    double accuracy = location.getAccuracy();
                    long timeStamp = location.getTime();

                    // Update Kalman filters with new measurements
                    latitudeFilter.processMeasurement(location.getLatitude(), accuracy, timeStamp);
                    longitudeFilter.processMeasurement(location.getLongitude(), accuracy, timeStamp);

                    currentUserLatitude = latitudeFilter.getPosition();
                    currentUserLongitude = longitudeFilter.getPosition();


                    // Calculate distance moved
                    float[] results = new float[1];
                    Location.distanceBetween(lastSentLatitude, lastSentLongitude, currentUserLatitude, currentUserLongitude, results);
                    float distanceMoved = results[0];

                    Log.e("Location Submit", String.valueOf("Distance Moved: " + distanceMoved));

                    // Send initial location or when user has moved more than 10 meters
                    if (distanceMoved > 5) {
                        sendLocationToDatabase();
                        lastSentLatitude = currentUserLatitude;
                        lastSentLongitude = currentUserLongitude;
                        Log.d("TestLocation", String.valueOf("Location Sent"));

                    }

                    // Update shared preferences
                    String latitudeString = String.valueOf(currentUserLatitude);
                    String longitudeString = String.valueOf(currentUserLongitude);


//                    SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
                    SharedPreferences.Editor editor = locationPref.edit();
                    editor.putString("latitude", latitudeString);
                    editor.putString("longitude", longitudeString);
                    editor.apply();

                    // Update the notification
                    if (!isNotificationRunnablePosted) {
                        isNotificationRunnablePosted = true;
                        serviceHandler.post(updateNotificationTask);
                    }
                }
            }
        };
    }


    // This task updates the notification with the current location
    private final Runnable updateNotificationTask = new Runnable() {
        @Override
        public void run() {
            updateNotification("Location Services Enabled", "Your location is now being actively monitored.");
            serviceHandler.postDelayed(this, 60000);
        }
    };

    private void updateNotification(String contentTitle, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_logo_horizontal)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true); // This makes the notification un-clearable

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void sendLocationToDatabase() {

        double[][] boundaries = {
                {14.569223824788123, 121.07857253040139},
                {14.56777189038692, 121.07904616045009},
                {14.567916419173063, 121.07966010997401},
                {14.567785913406011, 121.07970535755159},
                {14.567868984685852, 121.08007282018664},
                {14.56843592099765, 121.07997894287111},
                {14.568386159929423, 121.08097135987919},
                {14.569195576439387, 121.08133318699403},
                {14.569213696673737, 121.08121007680894},
                {14.56947848455418, 121.08124494552614},
                {14.569608282418624, 121.08053147792818},
                {14.569600494548906, 121.08053952455523},
                {14.569823746704863, 121.08054488897325},
                {14.569853316241268, 121.08036518096925},
                {14.569294171455361, 121.08029544353487}
        };

        if (isLocationInsideBoundaries(currentUserLatitude, currentUserLongitude, boundaries)) {


            StringRequest request = new StringRequest(Request.Method.POST, API.live_location,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("Location Submit", "Latitude: " + currentUserLatitude + " Longitude: " + currentUserLongitude);
                            SharedPreferences.Editor editor = locationPref.edit();
                            editor.putString("latitude", String.valueOf(currentUserLatitude));
                            editor.putString("longitude", String.valueOf(currentUserLongitude));
                            editor.apply();

                        }
                    }, error -> {
                Log.e("Location Submit", "Error sending location. Attempt ", error);
            }) {


                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("user_id", user_primary_id);
                    map.put("latitude", String.valueOf(currentUserLatitude));
                    map.put("longitude", String.valueOf(currentUserLongitude));
                    return map;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(request);

        }

    }

    private boolean isLocationInsideBoundaries(double latitude, double longitude, double[][] boundaries) {
        boolean inside = false;
        for (int i = 0, j = boundaries.length - 1; i < boundaries.length; j = i++) {
            double xi = boundaries[i][0], yi = boundaries[i][1];
            double xj = boundaries[j][0], yj = boundaries[j][1];
            boolean intersect = ((yi > longitude) != (yj > longitude))
                    && (latitude < (xj - xi) * (longitude - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "Service Started.");

        if (intent.getAction() != null && intent.getAction().equals("STOP_SERVICE")) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent broadcastIntent = new Intent("org.focus.app.ACTION_RESTART_SERVICE");
        sendBroadcast(broadcastIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        serviceHandler.removeCallbacks(updateNotificationTask);
        if (requestQueue != null) {
            requestQueue.cancelAll(tag -> true);
        }
        stopSelf();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}