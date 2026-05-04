package com.example.barrowing_system.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.Request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private final Context context;
    private List<Request> items;

    public RecentActivityAdapter(Context context, List<Request> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = items.get(position);

        holder.tvItemName.setText(request.getItemName());

        // Format date from borrowDate string
        String dateStr = request.getBorrowDate();
        if (dateStr != null && !dateStr.isEmpty()) {
            holder.tvRequestDate.setText(dateStr);
        } else {
            holder.tvRequestDate.setText("Unknown date");
        }

        // Set status badge with color
        String status = request.getStatus();
        holder.tvStatusBadge.setText(status != null ? status : "Pending");

        int badgeColor;
        switch (status) {
            case "Approved":
                badgeColor = Color.parseColor("#4CAF50");
                break;
            case "Pending":
                badgeColor = Color.parseColor("#FF9800");
                break;
            case "Rejected":
                badgeColor = Color.parseColor("#F44336");
                break;
            case "Returned":
            case "Completed":
                badgeColor = Color.parseColor("#9E9E9E");
                break;
            default:
                badgeColor = Color.parseColor("#FF9800");
        }
        holder.tvStatusBadge.setBackgroundColor(badgeColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<Request> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        TextView tvRequestDate;
        TextView tvStatusBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
