package com.example.soulscrypt.NotificationPackages;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Constant.API;
import com.example.soulscrypt.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {


    private ListView notificationListView;
    private ArrayList<NotificationModel> notificationModelArrayList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    private SharedPreferences userPref;
    private RequestQueue requestQueue;
    private ProgressBar progressBar;

    private TextView txtMarkAllAsRead;
    private ImageView btnNotificationBack;
    private boolean activityStopped = false;
    private boolean isMarkingAsRead = false; // Variable to track whether marking as read is in progress
    private SwipeRefreshLayout swipeRefreshLayout;

    String user_primary_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found

        requestQueue = Volley.newRequestQueue(this);

        notificationListView = findViewById(R.id.notificationListView);
        btnNotificationBack = findViewById(R.id.btnNotificationBack);
        txtMarkAllAsRead = findViewById(R.id.txtMarkAllAsRead);
        progressBar = findViewById(R.id.progressBar);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNotifications();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        btnNotificationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        notificationAdapter = new NotificationAdapter(this, notificationModelArrayList);
        notificationListView.setAdapter(notificationAdapter);

        // Set the onItemClick listener here
        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NotificationModel model = notificationModelArrayList.get(position);
                showNotificationDialog(model.getTitle(), model.getContext());
            }
        });

        notificationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Check if the first visible item is at the top and the ExpandableListView has children
                if (firstVisibleItem == 0 && view.getChildCount() > 0) {
                    // Now, also check if the top of the first visible child is at 0
                    boolean topOfFirstItemVisible = view.getChildAt(0).getTop() == 0;
                    swipeRefreshLayout.setEnabled(topOfFirstItemVisible);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        txtMarkAllAsRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markReadNotification();
            }
        });

        populateSampleNotifications();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityStopped = true;
        requestQueue.cancelAll(this);
    }

    private void markReadNotification() {
        if (isMarkingAsRead) {
            return;
        }
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar before making the request

        });
//        progressBar.setVisibility(View.VISIBLE); // Show progress bar before making the request
        isMarkingAsRead = true; // Set flag to indicate marking as read is in progress

        StringRequest request = new StringRequest(Request.Method.POST, API.mark_all_as_read, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar on successful response

                });
                isMarkingAsRead = false; // Reset flag after request completes successfully

                refreshNotifications();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar on error response
                });
                isMarkingAsRead = false; // Reset flag after request completes (even if it fails)

//                runOnUiThread(() -> Toast.makeText(ActivityNotification.this, "Please Try Again.", Toast.LENGTH_SHORT).show());
                error.printStackTrace();
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
        request.setTag(this);
        requestQueue.add(request);
    }

    private void refreshNotifications() {
        notificationModelArrayList.clear();
        populateSampleNotifications();
    }

    private void populateSampleNotifications() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE)); // Ensure UI update is on the main thread

        StringRequest request = new StringRequest(Request.Method.POST, API.get_notification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE); // Hide progress bar on successful response
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        JSONArray notifications = jsonResponse.getJSONArray("notifications");
                        for (int i = 0; i < notifications.length(); i++) {
                            JSONObject notification = notifications.getJSONObject(i);
                            String title = notification.getString("title");
                            String context = notification.getString("context");
                            String createdAt = notification.getString("created_at");
                            String isRead = notification.getString("is_read");

                            NotificationModel notificationModel = new NotificationModel(title, context, createdAt, isRead);
                            notificationModelArrayList.add(notificationModel);
                        }
                        if (!activityStopped) {
//                            notificationAdapter.notifyDataSetChanged();
                            runOnUiThread(() -> notificationAdapter.notifyDataSetChanged()); // Ensure UI update is on the main thread

                        }
                    } else {
                        Log.e("ActivityNotification", "Server response indicates failure");
                    }
                } catch (JSONException e) {
                    Log.e("ActivityNotification", "Error parsing JSON response: " + e.getMessage());
//                    runOnUiThread(() -> Toast.makeText(ActivityNotification.this, "An error occurred while processing the response", Toast.LENGTH_SHORT).show());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar on error response on the UI thread
//                    Toast.makeText(ActivityNotification.this, "A network error occurred", Toast.LENGTH_SHORT).show();
                });
                Log.e("ActivityNotification", "Volley error: " + error.getMessage());
            }
        }){

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

        request.setTag(this);
        requestQueue.add(request);
    }

    private void showNotificationDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}