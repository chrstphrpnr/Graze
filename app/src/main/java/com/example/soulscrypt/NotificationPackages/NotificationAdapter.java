package com.example.soulscrypt.NotificationPackages;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.soulscrypt.R;

import java.util.List;

public class NotificationAdapter extends BaseAdapter {

    private Context context;
    private List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }
    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp;
        ImageView notificationIcon;
        FrameLayout borderLayout;
        CardView cardView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notification_list_group, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.txtNotificationTitle);
            holder.tvMessage = convertView.findViewById(R.id.notificationContext);
            holder.tvTimestamp = convertView.findViewById(R.id.notificationTimeStamp);
            holder.notificationIcon = convertView.findViewById(R.id.notificationIcon);
            holder.borderLayout = convertView.findViewById(R.id.borderLayout);
            holder.cardView = convertView.findViewById(R.id.notificationItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NotificationModel model = notificationList.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.tvMessage.setText(model.getContext());
        holder.tvTimestamp.setText(model.getCreatedAt());


//        // Check the category of the notification and set the appropriate icon
//        if ("announcement".equals(model.getCategory())) {
//            holder.notificationIcon.setImageResource(R.drawable.layer_announcement);
//
//            // Set padding for the notificationIcon
//            int paddingInDp = 5; // Set your desired padding in dp
//            float scale = context.getResources().getDisplayMetrics().density;
//            int paddingInPx = (int) (paddingInDp * scale + 0.5f); // Convert dp to pixels
//
//            holder.notificationIcon.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
//
//
//            holder.borderLayout.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.transparent)));
//        } else if ("notification".equals(model.getCategory())) {
//            holder.notificationIcon.setImageResource(R.drawable.ic_notification_filled);
//        }


        if ("0".equals(model.getIsRead())) {
            // Apply the border shape for unread notifications
            holder.cardView.setBackgroundResource(R.drawable.border_shape);
        } else {
            // Optionally set a different or no background for read notifications
            holder.cardView.setBackgroundResource(0); // Remove background if needed
            holder.borderLayout.setBackgroundResource(R.drawable.border_outline_notification);

        }

        return convertView;



    }


}