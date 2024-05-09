package com.example.soulscrypt.Auth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.soulscrypt.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout layoutTextFirstName, layoutTextLastname, layoutTextAddress, layoutTextPhone, layoutTextEmail, layoutTextPassword;
    private TextInputEditText edtInputFirstName, edtInputLastname, edtInputAddress, edtInputPhone, edtInputEmail, edtInputPassword;
    private Button btnRegister;
    RequestQueue queue;

    private TextView txtLinkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

    }

    private void init(){
        queue = Volley.newRequestQueue(this);

        layoutTextFirstName = findViewById(R.id.layoutTextFirstName);
        layoutTextLastname = findViewById(R.id.layoutTextLastname);
        layoutTextAddress = findViewById(R.id.layoutTextAddress);
        layoutTextPhone = findViewById(R.id.layoutTextPhone);
        layoutTextEmail = findViewById(R.id.layoutTextEmail);
        layoutTextPassword = findViewById(R.id.layoutTextPassword);

        edtInputFirstName = findViewById(R.id.edtInputFirstName);
        edtInputLastname = findViewById(R.id.edtInputLastname);
        edtInputAddress = findViewById(R.id.edtInputAddress);
        edtInputPhone = findViewById(R.id.edtInputPhone);
        edtInputEmail = findViewById(R.id.edtInputEmail);
        edtInputPassword = findViewById(R.id.edtInputPassword);


        btnRegister = findViewById(R.id.btnRegister);

        txtLinkLogin = findViewById(R.id.txtLinkLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    register();
                }
            }
        });

        txtLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });


    }

    private boolean validate(){

        String first_name = edtInputFirstName.getText().toString();
        String last_name = edtInputLastname.getText().toString();
        String address = edtInputAddress.getText().toString();
        String phone = edtInputPhone.getText().toString();
        String email = edtInputEmail.getText().toString();
        String password = edtInputPassword.getText().toString();

        if(first_name.isEmpty()){
            layoutTextFirstName.setErrorEnabled(true);
            layoutTextFirstName.setError("First Name is Required");
            return false;
        }

        if(last_name.isEmpty()){
            layoutTextLastname.setErrorEnabled(true);
            layoutTextLastname.setError("Last Name is Required");
            return false;
        }

        if(address.isEmpty()){
            layoutTextAddress.setErrorEnabled(true);
            layoutTextAddress.setError("Address is Required");
            return false;
        }

        if(phone.isEmpty()){
            layoutTextPhone.setErrorEnabled(true);
            layoutTextPhone.setError("Phone is Required");
            return false;
        }

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


    private void register(){


        StringRequest request = new StringRequest(Request.Method.POST, API.register_api, response -> {

            Log.d("LoginResponse", response); // Log the response

            try {
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.getBoolean("success")) {
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    SharedPreferences userPref = this.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();


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
                map.put("first_name", edtInputFirstName.getText().toString().trim());
                map.put("last_name", edtInputLastname.getText().toString());
                map.put("address", edtInputAddress.getText().toString().trim());
                map.put("phone", edtInputPhone.getText().toString());
                map.put("email", edtInputEmail.getText().toString().trim());
                map.put("password", edtInputPassword.getText().toString());
                return map;
            }
        };

        queue.add(request);

    }








}