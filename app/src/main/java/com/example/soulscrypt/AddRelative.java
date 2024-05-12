package com.example.soulscrypt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Constant.API;
import com.example.soulscrypt.RelativeList.RelativeDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddRelative extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextView;
    private RequestQueue requestQueue;
    private TextView relativeDateOfDeathTv;

    String user_primary_id, record_id;

    Button btnSubmitRelative;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_relative);

        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        relativeDateOfDeathTv = findViewById(R.id.relativeDateOfDeathTv);
        btnSubmitRelative = findViewById(R.id.btnSubmitRelative);


        requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.fetch_add_relatives, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean("success");
                if (success) {
                    JSONArray relativesArray = jsonResponse.getJSONArray("fetch_request_relatives");

                    // Create a list to hold the relative's full names
                    List<String> relativesList = new ArrayList<>();

                    // Create a map to hold the relative's full name and date of death
                    Map<String, String> relativeDateMap = new HashMap<>();

                    // Iterate through the JSON array and add full names to the list
                    for (int i = 0; i < relativesArray.length(); i++) {
                        JSONObject relativeObject = relativesArray.getJSONObject(i);
                        String firstName = relativeObject.getString("first_name");
                        String lastName = relativeObject.getString("last_name");
                        String fullName = firstName + " " + lastName;
                        relativesList.add(fullName);

                        // Add full name and date of death to the map
                        String dateOfDeath = formatDate(relativeObject.getString("date_death"));

                        relativeDateMap.put(fullName, dateOfDeath);

                        record_id = relativeObject.getString("id");
                    }

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line, relativesList);

                    // Apply the adapter to the AutoCompleteTextView
                    autoCompleteTextView.setAdapter(adapter);

                    // Set an item click listener for the AutoCompleteTextView
                    autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedFullName = (String) parent.getItemAtPosition(position);
                        String dateOfDeath = relativeDateMap.get(selectedFullName);
                        relativeDateOfDeathTv.setText(dateOfDeath);
                    });

                    Log.d("RecordID", "User ID: " + user_primary_id);
                    Log.d("RecordID", "Record ID: " + record_id);

                    // Set a text changed listener for the AutoCompleteTextView
                    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.toString().isEmpty()) {
                                relativeDateOfDeathTv.setText("");
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                } else {
                    Log.e("RelativeDetails", "Server response indicates failure");
                }
            } catch (JSONException e) {
                Log.e("RelativeDetails", "Error parsing JSON response: " + e.getMessage());
            }
        }, error -> Log.e("RelativeDetails", "Volley error: " + error.getMessage()));

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);



        btnSubmitRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAddRelative();
            }
        });
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

    public void submitAddRelative(){
        StringRequest request = new StringRequest(Request.Method.POST, API.submit_add_relatives, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {

                        Toast.makeText(AddRelative.this, "Successfully Submitted a Request for Adding a Relative.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddRelative.this, HomeActivity.class));

                    }
                } catch (JSONException e) {
                    Log.e("RequestService", "Error JSON response: " + e.getMessage());
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
                map.put("record_id", record_id);

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
