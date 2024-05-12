package com.example.soulscrypt.NotificationPackages;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Constant.API;
import com.example.soulscrypt.HomeActivity;
import com.example.soulscrypt.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class NotificationService extends Service {
    private SharedPreferences userPref;

    private RequestQueue requestQueue;
    private Handler handler = new Handler();
    private static final long INTERVAL = 5000; // Interval for checking notifications

    private boolean isCheckingNotifications = false; // Flag to track if checkForNewNotifications is running
    private String user_primary_id;
    private Set<Integer> shownNotificationIds = new HashSet<>(); // Set to store IDs of shown notifications

    private static final String PREF_LAST_NOTIFICATION_ID = "last_notification_id";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SharedPreferences and create notification channel
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found
        createNotificationChannel();

        // Retrieve the last shown notification ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        int lastNotificationId = sharedPreferences.getInt(PREF_LAST_NOTIFICATION_ID, -1);

        // Start the handler for periodic notification checks
        handler.postDelayed(checkForNotificationsRunnable, INTERVAL);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

    }

    private Runnable checkForNotificationsRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCheckingNotifications) { // Check if not already running
                checkForNewNotifications();
            }

            // Remove any existing callbacks to avoid posting multiple callbacks
            handler.removeCallbacks(this);
            // Post the new callback
            handler.postDelayed(this, INTERVAL);
        }
    };


    private void checkForNewNotifications() {
        StringRequest request = new StringRequest(Request.Method.POST, API.check_new_notification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        Object notifications = jsonObject.get("notifications");

                        // Handle different types of notifications (JSONObject or JSONArray)
                        if (notifications instanceof JSONObject) {
                            JSONObject notificationObject = (JSONObject) notifications;
                            handleSingleNotification(notificationObject);
                        } else if (notifications instanceof JSONArray) {
                            JSONArray notificationArray = (JSONArray) notifications;
                            for (int i = 0; i < notificationArray.length(); i++) {
                                JSONObject notificationObject = notificationArray.getJSONObject(i);
                                handleSingleNotification(notificationObject);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("NotificationService", "JSON parsing error: ", e);
                } finally {
                    isCheckingNotifications = false;
                }
            }
        }, error -> {
            Log.e("NotificationService", "Volley error: ", error);
            isCheckingNotifications = false;
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", user_primary_id);
                return map;
            }
        };

        // Configure the retry policy for the request
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the Volley request queue
        requestQueue.add(request);
    }

    private void handleSingleNotification(JSONObject notificationObject) throws JSONException {
        int id = notificationObject.getInt("id");
        if (!shownNotificationIds.contains(id) && id > getLastNotificationId()) {
            String title = notificationObject.getString("title");
            String context = notificationObject.getString("context");
            showNotification(title, context, id);
            shownNotificationIds.add(id);
            saveLastNotificationId(id); // Save the last shown notification ID

            // Broadcast that a new notification has arrived
            Intent intent = new Intent("com.example.soulscrypt.NEW_NOTIFICATION");
            intent.putExtra("notification_id", id);
            sendBroadcast(intent);
        }
    }

    // Save the last shown notification ID to SharedPreferences
    private void saveLastNotificationId(int id) {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_LAST_NOTIFICATION_ID, id);
        editor.apply();
    }

    // Retrieve the last shown notification ID from SharedPreferences
    private int getLastNotificationId() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        return sharedPreferences.getInt(PREF_LAST_NOTIFICATION_ID, -1);
    }



    private void showNotification(String title, String context, int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create an intent that will open when the notification is tapped
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(context)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI); // Add sound here

        // Show the notification
        if (notificationManager != null) {
            notificationManager.notify(id, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ("Notification");
            String description = ("Notification Description");
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
