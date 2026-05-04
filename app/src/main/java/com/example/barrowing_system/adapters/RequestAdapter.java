package com.example.barrowing_system.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnRequestClickListener {
        void onRequestClick(Request request, int position);
    }

    private final Context context;
    private List<Request> fullList;   // unfiltered master list
    private List<Request> displayList; // filtered list shown in RecyclerView
    private OnRequestClickListener listener;

    public RequestAdapter(Context context, List<Request> requests,
                          OnRequestClickListener listener) {
        this.context = context;
        this.fullList    = new ArrayList<>(requests);
        this.displayList = new ArrayList<>(requests);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request req = displayList.get(position);

        // Initial avatar letter
        String name = req.getRequesterName();
        holder.tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
        holder.tvName.setText(name);
        holder.tvItem.setText(req.getItemName() + " (x" + req.getQuantity() + ")");
        holder.tvDate.setText(req.getBorrowDate());
        holder.tvStatus.setText(req.getStatus());

        // Status badge color
        switch (req.getStatus()) {
            case "Viewed":
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_viewed);
                break;
            case "Done":
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_done);
                break;
            case "Returned":
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_returned);
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_pending);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRequestClick(req, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    /** Filter by status. Pass "All" to show everything. */
    public void filter(String status) {
        displayList.clear();
        if ("All".equals(status)) {
            displayList.addAll(fullList);
        } else {
            for (Request r : fullList) {
                if (r.getStatus().equals(status)) displayList.add(r);
            }
        }
        notifyDataSetChanged();
    }

    /** Update the full dataset (e.g. after Firebase fetch) */
    public void setData(List<Request> requests) {
        fullList    = new ArrayList<>(requests);
        displayList = new ArrayList<>(requests);
        notifyDataSetChanged();
    }

    /** Update a single item's status in place */
    public void updateStatus(int position, String newStatus) {
        if (position < 0 || position >= displayList.size()) return;
        displayList.get(position).setStatus(newStatus);
        // Sync back to fullList
        String id = displayList.get(position).getId();
        for (Request r : fullList) {
            if (r.getId().equals(id)) { r.setStatus(newStatus); break; }
        }
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvItem, tvDate, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvName    = itemView.findViewById(R.id.tvRequesterName);
            tvItem    = itemView.findViewById(R.id.tvItemName);
            tvDate    = itemView.findViewById(R.id.tvDate);
            tvStatus  = itemView.findViewById(R.id.tvStatus);
        }
    }
}
