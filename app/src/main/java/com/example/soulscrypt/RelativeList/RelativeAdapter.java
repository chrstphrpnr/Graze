

package com.example.soulscrypt.RelativeList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soulscrypt.R;

import java.util.List;

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

            btnDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a toast message
                    Toast.makeText(context, "Details clicked for item: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}


