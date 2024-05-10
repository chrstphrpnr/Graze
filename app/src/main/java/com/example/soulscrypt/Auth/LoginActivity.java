package com.example.soulscrypt.Auth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.soulscrypt.Constant.API;
import com.example.soulscrypt.HomeActivity;
import com.example.soulscrypt.MainActivity;
import com.example.soulscrypt.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutTextEmail, layoutTextPassword;
    private TextInputEditText edtInputEmail, edtInputPassword;

    private Button btnLogin;

    private TextView txtLinkRegister;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    private void init() {
        queue = Volley.newRequestQueue(this);

        layoutTextEmail = findViewById(R.id.layoutTextEmail);
        layoutTextPassword = findViewById(R.id.layoutTextPassword);
        edtInputEmail = findViewById(R.id.edtInputEmail);
        edtInputPassword = findViewById(R.id.edtInputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtLinkRegister = findViewById(R.id.txtLinkRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    login();
                }
            }
        });

        txtLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private boolean validate(){

        String email = edtInputEmail.getText().toString();
        String password = edtInputPassword.getText().toString();

        if(email.isEmpty()){
            layoutTextEmail.setErrorEnabled(true);
            layoutTextEmail.setError("Email is Required");
            return false;
        }

        if(password.isEmpty()){
            layoutTextPassword.setErrorEnabled(true);
            layoutTextPassword.setError("Password is Required");
            return false;
        }

        return true;
    }


    private void login(){

        StringRequest request = new StringRequest(Request.Method.POST, API.login_api, response -> {

            Log.d("LoginResponse", response); // Log the response

            try {
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.getBoolean("success")) {
                    JSONObject userObject = jsonObject.getJSONObject("user");

                    SharedPreferences userPref = this.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("user_id", userObject.getString("id"));

                    editor.apply();


                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();

            }

        }, error -> {
            Log.d("LoginError", "Error encountered during login.");
            error.printStackTrace();

        }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", edtInputEmail.getText().toString().trim());
                map.put("password", edtInputPassword.getText().toString());
                return map;
            }
        };

        queue.add(request);
    }



}