package com.example.barrowing_system.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.Resident;

import java.util.ArrayList;
import java.util.List;

public class ResidentAdapter extends RecyclerView.Adapter<ResidentAdapter.ViewHolder> {

    public interface OnResidentActionListener {
        void onResidentClick(Resident resident, int position);
        void onToggleStatus(Resident resident, int position);
    }

    private final Context context;
    private List<Resident> fullList;
    private List<Resident> displayList;
    private final OnResidentActionListener listener;

    public ResidentAdapter(Context context, List<Resident> residents,
                           OnResidentActionListener listener) {
        this.context     = context;
        this.fullList    = new ArrayList<>(residents);
        this.displayList = new ArrayList<>(residents);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_resident, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resident res = displayList.get(position);

        String name = res.getFullName();
        holder.tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
        holder.tvName.setText(name);
        holder.tvEmail.setText(res.getEmail());
        holder.tvBorrows.setText(res.getTotalBorrows() + " total borrows");
        holder.tvStatus.setText(res.getStatus());

        boolean isActive = "Active".equals(res.getStatus());

        // Status badge color
        holder.tvStatus.setBackgroundResource(
                isActive ? R.drawable.badge_status_done : R.drawable.badge_status_pending);

        // Toggle action label
        holder.tvToggle.setText(isActive ? "Suspend" : "Unsuspend");
        holder.tvToggle.setTextColor(context.getColor(
                isActive ? R.color.red_overdue : R.color.green_active));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onResidentClick(res, holder.getAdapterPosition());
        });

        holder.tvToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(res, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    /**
     * Filter by status tab AND search query simultaneously.
     * @param statusFilter "All", "Active", or "Suspended"
     * @param query        search text (name or email)
     */
    public void filterAndSearch(String statusFilter, String query) {
        displayList.clear();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        for (Resident r : fullList) {
            boolean matchesStatus = "All".equals(statusFilter)
                    || r.getStatus().equals(statusFilter);
            boolean matchesQuery  = q.isEmpty()
                    || r.getFullName().toLowerCase().contains(q)
                    || r.getEmail().toLowerCase().contains(q);
            if (matchesStatus && matchesQuery) displayList.add(r);
        }
        notifyDataSetChanged();
    }

    /** Toggle a resident's status and refresh that row */
    public void toggleStatus(int position) {
        if (position < 0 || position >= displayList.size()) return;
        Resident r      = displayList.get(position);
        String newStatus = "Active".equals(r.getStatus()) ? "Suspended" : "Active";
        r.setStatus(newStatus);
        // Sync fullList
        for (Resident fr : fullList) {
            if (fr.getId().equals(r.getId())) { fr.setStatus(newStatus); break; }
        }
        notifyItemChanged(position);
    }

    public void setData(List<Resident> residents) {
        fullList    = new ArrayList<>(residents);
        displayList = new ArrayList<>(residents);
        notifyDataSetChanged();
    }

    /** Count residents by status */
    public int countByStatus(String status) {
        if ("All".equals(status)) return fullList.size();
        int count = 0;
        for (Resident r : fullList) if (r.getStatus().equals(status)) count++;
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvEmail, tvBorrows, tvStatus, tvToggle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvResInitial);
            tvName    = itemView.findViewById(R.id.tvResName);
            tvEmail   = itemView.findViewById(R.id.tvResEmail);
            tvBorrows = itemView.findViewById(R.id.tvResTotalBorrows);
            tvStatus  = itemView.findViewById(R.id.tvResStatus);
            tvToggle  = itemView.findViewById(R.id.tvToggleStatus);
        }
    }
}