package com.example.soulscrypt.RelativeList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.soulscrypt.R;
import com.example.soulscrypt.RequestService;

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

public class RelativeDetails extends AppCompatActivity {

    private TextView relativeDetailsName, relativeDetailsBirth, relativeDetailsDeath, relativeDetailsExhumation, relativeDetailsSection, relativeDetailsLotNumber;

    private ImageView btnRelativeBack;

    private Button btnRequestService;

    String user_primary_id, record_id, service_id;

    Spinner spinnerServices;
    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative_details);
        queue = Volley.newRequestQueue(this);

        relativeDetailsName = findViewById(R.id.relativeDetailsName);
        relativeDetailsBirth = findViewById(R.id.relativeDetailsBirth);
        relativeDetailsDeath = findViewById(R.id.relativeDetailsDeath);
        relativeDetailsExhumation = findViewById(R.id.relativeDetailsExhumation);
        relativeDetailsSection = findViewById(R.id.relativeDetailsSection);
        relativeDetailsLotNumber = findViewById(R.id.relativeDetailsLotNumber);
        btnRequestService = findViewById(R.id.btnRequestService);
        spinnerServices = findViewById(R.id.spinner);
        btnRelativeBack = findViewById(R.id.btnRelativeBack);

        record_id = getIntent().getStringExtra("record_id");
        String relative_name = getIntent().getStringExtra("relative_name");
        String death_date = getIntent().getStringExtra("death_date");
        String birth_date = getIntent().getStringExtra("birth_date");
        String exhumation_date = getIntent().getStringExtra("exhumation_date");
        String section_name = getIntent().getStringExtra("section_name");
        String lot_number = getIntent().getStringExtra("lot_number");

        Log.d("RecordDetails", "Record ID: " + record_id);
        Log.d("RecordDetails", "Name: " + relative_name);
        Log.d("RecordDetails", "Death: " + death_date);
        Log.d("RecordDetails", "Birth: " + birth_date);
        Log.d("RecordDetails", "Exhumation: " + exhumation_date);
        Log.d("RecordDetails", "Section: " + section_name);
        Log.d("RecordDetails", "Lot Number: " + lot_number);

//        String relative_death_date = formatDate(relativeObject.getString("date_of_death"));


        relativeDetailsName.setText("Name: " + relative_name);
        relativeDetailsBirth.setText("Date of Birth: " + formatDate(birth_date));
        relativeDetailsDeath.setText("Date of Death: " + formatDate(death_date));
        relativeDetailsExhumation.setText("Date of Exhumation: " + formatDate(exhumation_date));
        relativeDetailsSection.setText("Section: " + section_name);
        relativeDetailsLotNumber.setText("Lot: " + lot_number);

        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found


        btnRelativeBack = findViewById(R.id.btnRelativeBack);
        btnRelativeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRequestService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitServiceRequest();

            }
        });



        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.fetch_services, response -> {
                try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            JSONArray servicesArray = jsonResponse.getJSONArray("service_requests");

                            // Create a list to hold the services
                            List<String> servicesList = new ArrayList<>();

                            // Add a default value as the first item
                            servicesList.add("Request a Service Here");

                            // Iterate through the JSON array and add services to the list
                            for (int i = 0; i < servicesArray.length(); i++) {
                                JSONObject serviceObject = servicesArray.getJSONObject(i);
                                service_id = serviceObject.getString("id");
                                String serviceName = serviceObject.getString("service_name");
                                String price = serviceObject.getString("price");
                                servicesList.add(serviceName + " - ₱" + price);
                            }

                            // Create an ArrayAdapter using the string array and a default spinner layout
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item, servicesList);

                            // Specify the layout to use when the list of choices appears
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            // Apply the adapter to the spinner
                            spinnerServices.setAdapter(adapter);
                        } else {
                            Log.e("RelativeDetails", "Server response indicates failure");
                        }
                    } catch (JSONException e) {
                        Log.e("RelativeDetails", "Error parsing JSON response: " + e.getMessage());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RelativeDetails", "Volley error: " + error.getMessage());
            }
        });

        queue.add(stringRequest);


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

    public void submitServiceRequest(){
        StringRequest request = new StringRequest(Request.Method.POST, API.submit_service_request, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {

                        Toast.makeText(RelativeDetails.this, "Successfully Submitted a Request", Toast.LENGTH_SHORT).show();
                        spinnerServices.setSelection(0);

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
                map.put("service_id", service_id);

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





