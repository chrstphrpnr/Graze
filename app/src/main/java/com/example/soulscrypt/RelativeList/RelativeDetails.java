package com.example.soulscrypt.RelativeList;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.soulscrypt.R;

public class RelativeDetails extends AppCompatActivity {

    private TextView txtRelativeId;

    private ImageView btnNotificationBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative_details);

//        txtRelativeId = findViewById(R.id.txtRelativeId);

        String relative_name = getIntent().getStringExtra("relative_name");
        String death_date = getIntent().getStringExtra("death_date");
        String birth_date = getIntent().getStringExtra("birth_date");
        String exhumation_date = getIntent().getStringExtra("exhumation_date");
        String section_name = getIntent().getStringExtra("section_name");
        String lot_number = getIntent().getStringExtra("lot_number");


        Log.d("RecordDetails", "Name: " + relative_name);
        Log.d("RecordDetails", "Death: " + death_date);
        Log.d("RecordDetails", "Birth: " + birth_date);
        Log.d("RecordDetails", "Exhumation: " + exhumation_date);
        Log.d("RecordDetails", "Section: " + section_name);
        Log.d("RecordDetails", "Lot Number: " + lot_number);

        btnNotificationBack = findViewById(R.id.btnNotificationBack);
        btnNotificationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}