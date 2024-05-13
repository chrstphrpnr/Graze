package com.example.soulscrypt.Reports;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.example.soulscrypt.HomeActivity;
import com.example.soulscrypt.R;
import com.example.soulscrypt.RelativeList.RelativeDetails;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    TextInputLayout layoutTextIncidentTitle, layoutTextIncidentDesc;
    TextInputEditText edtInputIncidentTitle, edtInputIncidentDesc;

    Spinner spinnerLocation;

    Button btnSubmitIncident;

    String incidentTitle, incidentDescription, section_id, user_primary_id;

    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        queue = Volley.newRequestQueue(this);

        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found


        layoutTextIncidentTitle = findViewById(R.id.layoutTextIncidentTitle);
        layoutTextIncidentDesc = findViewById(R.id.layoutTextIncidentDesc);


        edtInputIncidentTitle = findViewById(R.id.edtInputIncidentTitle);
        edtInputIncidentDesc = findViewById(R.id.edtInputIncidentDesc);

        spinnerLocation = findViewById(R.id.spinnerLocation);

        btnSubmitIncident = findViewById(R.id.btnSubmitIncident);


        btnSubmitIncident = findViewById(R.id.btnSubmitIncident);

        showLocationSpinner();

        incidentTitle = edtInputIncidentTitle.getText().toString();
        incidentDescription = edtInputIncidentDesc.getText().toString();

        btnSubmitIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    submitReport();
                }
            }
        });


    }

    private boolean validate(){

        incidentTitle = edtInputIncidentTitle.getText().toString();
        incidentDescription = edtInputIncidentDesc.getText().toString();

        if(incidentTitle.isEmpty()){
            layoutTextIncidentTitle.setErrorEnabled(true);
            layoutTextIncidentTitle.setError("Title is Required");
            return false;
        }

        if(incidentDescription.isEmpty()){
            layoutTextIncidentDesc.setErrorEnabled(true);
            layoutTextIncidentDesc.setError("Incident is Required");
            return false;
        }

        return true;
    }

    public void showLocationSpinner(){


        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.fetch_sections, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean("success");
                if (success) {
                    JSONArray sectionsArray = jsonResponse.getJSONArray("sections");

                    // Create a list to hold the services
                    List<String> sectionsList = new ArrayList<>();

                    // Add a default value as the first item
                    sectionsList.add("Choose Section");

                    // Iterate through the JSON array and add services to the list
                    for (int i = 0; i < sectionsArray.length(); i++) {
                        JSONObject serviceObject = sectionsArray.getJSONObject(i);
                        section_id = serviceObject.getString("id");
                        String sectionName = serviceObject.getString("section_name");
                        sectionsList.add(sectionName);

                    }

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_spinner_item, sectionsList);

                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Apply the adapter to the spinner
                    spinnerLocation.setAdapter(adapter);
                } else {
                    Log.e("SectionSpinner", "Server response indicates failure");
                }
            } catch (JSONException e) {
                Log.e("SectionSpinner", "Error parsing JSON response: " + e.getMessage());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("SectionSpinner", "Volley error: " + error.getMessage());
            }
        });

        queue.add(stringRequest);




    }

    public void submitReport(){


        StringRequest request = new StringRequest(Request.Method.POST, API.submit_incident_report, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {

                        Toast.makeText(ReportActivity.this, "Successfully Submitted a Incident", Toast.LENGTH_SHORT).show();
                        spinnerLocation.setSelection(0);
                        startActivity(new Intent(ReportActivity.this, HomeActivity.class));
                    }
                } catch (JSONException e) {
                    Log.e("SubmitIncident", "Error JSON response: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("SubmitIncident", "Volley error: " + error.getMessage());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("title", incidentTitle);
                map.put("description", incidentDescription);
                map.put("user_id", user_primary_id);
                map.put("section_id", section_id);

                return map;
            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);



    }
}