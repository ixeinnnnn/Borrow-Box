package com.example.barrowing_system.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.Notification;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    private final Context context;
    private List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(Context context, List<Notification> notifications,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification n = notifications.get(position);

        holder.tvTitle.setText(n.getTitle());
        holder.tvMessage.setText(n.getMessage());

        // Format time
        Timestamp timestamp = n.getCreatedAt();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(date));
        } else {
            holder.tvTime.setText("Just now");
        }

        // Show/hide unread dot and set background based on read status
        if (!n.isRead()) {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(0xFFF3F4F6); // #F3F4F6 light gray
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(n, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return notifications.size(); }

    public void setData(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View viewUnreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
