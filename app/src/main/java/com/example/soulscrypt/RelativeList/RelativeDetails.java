package com.example.soulscrypt.RelativeList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.soulscrypt.R;
import com.example.soulscrypt.RequestService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RelativeDetails extends AppCompatActivity {

    private TextView relativeDetailsName, relativeDetailsBirth, relativeDetailsDeath, relativeDetailsExhumation, relativeDetailsSection, relativeDetailsLotNumber;

    private ImageView btnNotificationBack;

    private Button btnRequestService;

    String user_primary_id;

    Spinner spinnerServices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative_details);

        relativeDetailsName = findViewById(R.id.relativeDetailsName);
        relativeDetailsBirth = findViewById(R.id.relativeDetailsBirth);
        relativeDetailsDeath = findViewById(R.id.relativeDetailsDeath);
        relativeDetailsExhumation = findViewById(R.id.relativeDetailsExhumation);
        relativeDetailsSection = findViewById(R.id.relativeDetailsSection);
        relativeDetailsLotNumber = findViewById(R.id.relativeDetailsLotNumber);
        btnRequestService = findViewById(R.id.btnRequestService);

        String record_id = getIntent().getStringExtra("record_id");
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



        spinnerServices = findViewById(R.id.spinner);














//        Log.d("RequestService", "UserID: " + user_primary_id);

        btnNotificationBack = findViewById(R.id.btnNotificationBack);
        btnNotificationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRequestService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RelativeDetails.this, RequestService.class);
                intent.putExtra("user_primary_id", user_primary_id);
                intent.putExtra("record_id", record_id);
                startActivity(intent);
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


}