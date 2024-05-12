

package com.example.soulscrypt.RelativeList;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelativeAdapter extends RecyclerView.Adapter<RelativeAdapter.ViewHolder> {

    private Context context;
    private List<RelativeModel> relativeList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(double latitude, double longitude);
    }

    public RelativeAdapter(Context context, List<RelativeModel> relativeList) {
        this.context = context;
        this.relativeList = relativeList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.relative_list_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RelativeModel model = relativeList.get(position);
        holder.relative_name.setText(model.getRelative_name());
        holder.relative_death_date.setText(model.getRelative_death_date());
        holder.relative_section.setText(model.getRelative_section());

        // Inside your RelativeAdapter's onBindViewHolder method
        holder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String record_id = String.valueOf(model.getRecord_id());
                StringRequest request = new StringRequest(Request.Method.POST, API.relative_details_api, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                JSONArray relativesDetailsArray = jsonResponse.getJSONArray("relativeDetails");
                                for (int i = 0; i < relativesDetailsArray.length(); i++) {
                                    JSONObject relativeDetailsObject = relativesDetailsArray.getJSONObject(i);

                                    String relative_name = relativeDetailsObject.getString("full_name");
                                    String birth_date = relativeDetailsObject.getString("birth_date");
                                    String death_date = relativeDetailsObject.getString("death_date");
                                    String exhumation_date = relativeDetailsObject.getString("exhumation_date");
                                    String section_name = relativeDetailsObject.getString("section_name");
                                    String lot_number = relativeDetailsObject.getString("lot_number");




                                   Intent intent = new Intent(context, RelativeDetails.class);

                                    intent.putExtra("record_id", record_id);

                                    intent.putExtra("relative_name", relative_name);
                                    intent.putExtra("birth_date", birth_date);
                                    intent.putExtra("death_date", death_date);
                                    intent.putExtra("exhumation_date", exhumation_date);
                                    intent.putExtra("section_name", section_name);
                                    intent.putExtra("lot_number", lot_number);


                                    context.startActivity(intent);

                                }
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
                        map.put("record_id", record_id);
                        return map;
                    }
                };


                request.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);


            }















        });
    }

    @Override
    public int getItemCount() {
        return relativeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView relative_name, relative_death_date, relative_section;
        ImageView btnDetails;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relative_name = itemView.findViewById(R.id.relativeNameTv);
            relative_death_date = itemView.findViewById(R.id.relativeDeathDateTv);
            relative_section = itemView.findViewById(R.id.relativeSectionTv);
            btnDetails = itemView.findViewById(R.id.btnDetails);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            double latitude = relativeList.get(position).getLatitude();
                            double longitude = relativeList.get(position).getLongitude();
                            mListener.onItemClick(latitude, longitude);
                        }
                    }
                }
            });



        }
    }
}


