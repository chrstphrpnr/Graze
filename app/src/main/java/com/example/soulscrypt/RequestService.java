package com.example.soulscrypt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class RequestService extends AppCompatActivity {
    String user_primary_id, record_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service);

        user_primary_id = getIntent().getStringExtra("user_primary_id");
        record_id = getIntent().getStringExtra("record_id");

        Log.d("RequestService", "UserID: " + user_primary_id);
        Log.d("RequestService", "RecordID: " + record_id);


    }
}