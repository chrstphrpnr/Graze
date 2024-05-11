package com.example.soulscrypt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {
    String user_primary_id;

    TextView txtName, txtAddress, txtPhone, txtEmail;
    RequestQueue queue;

    ImageView profile_image;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        profile_image = findViewById(R.id.profile_image);

        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        user_primary_id = userPref.getString("user_id", "0"); // 0 is default value if key not found
        queue = Volley.newRequestQueue(this);


        showProfile();


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

    }

    // Method to correct image orientation
    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(getContentResolver().openInputStream(selectedImage));
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    // Utility method to rotate the image
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                // Correct the orientation of the image
                Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, filePath);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Send this encoded string to your server
                sendImageToServer(encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FOCUS_DEBUG", "Error in Connection: ", e);
            }
        }
    }

    private void sendImageToServer(final String encodedImage) {
        final ProgressDialog progressDialog = new ProgressDialog(Settings.this);

        StringRequest request = new StringRequest(Request.Method.POST, API.update_profile, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                showProfile();
                progressDialog.dismiss(); // Ensure dialog is dismissed if there's no URL

            }
        }, error -> {
            runOnUiThread(() -> Toast.makeText(Settings.this, "Error in Connection", Toast.LENGTH_SHORT).show());
            error.printStackTrace();
            Log.e("Image", "Image" + error.getMessage());

            progressDialog.dismiss(); // Ensure dialog is dismissed if there's no URL

        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", user_primary_id);

                map.put("image", encodedImage); // Adjust this based on your API's expected parameter
                return map;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }


    private void showProfile(){

        StringRequest request = new StringRequest(Request.Method.POST, API.profile_information, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                            JSONObject userDetails = jsonObject.getJSONObject("user");

                            String first_name = userDetails.getString("first_name");
                            String last_name = userDetails.getString("last_name");
                            String full_name = first_name + " " + last_name;

                            final String userImage = userDetails.getString("img_path");
                            final String imageUrl = API.URL + userImage;

                            String address = userDetails.getString("address");
                            String phone = userDetails.getString("phone");
                            String email = userDetails.getString("email");

                            updateProfileImage(imageUrl); // Update the image using the new method


                            txtName.setText(full_name);
                            txtAddress.setText(address);
                            txtPhone.setText(phone);
                            txtEmail.setText(email);

                    } else {
                        Log.e("RelativeList", "Server response indicates failure");
                    }
                } catch (JSONException e) {
                    Log.e("RelativeList", "Error parsing JSON response: " + e.getMessage());
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
                return map;
            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }


    // Existing function where you update the profile image and possibly show a dialog
    private void updateProfileImage(String imageUrl) {
        final ProgressDialog progressDialog = new ProgressDialog(Settings.this);

        if (!isFinishing()) { // Check if the activity is not finishing
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            // Handle error scenario, perhaps set an error image
                            profile_image.setImageResource(R.drawable.ic_profile);
                        }
                    });
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss(); // Ensure dialog is dismissed if there's no URL
            }
            profile_image.setImageResource(R.drawable.ic_profile); // Your default or placeholder image
        }
    }

}