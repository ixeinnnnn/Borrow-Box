package com.example.barrowing_system.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.Penalty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PenaltyAdapter extends RecyclerView.Adapter<PenaltyAdapter.ViewHolder> {

    public interface OnPenaltyClickListener {
        void onPenaltyClick(Penalty penalty, int position);
        void onMarkPaidClick(Penalty penalty, int position);
    }

    private final Context context;
    private List<Penalty> fullList;
    private List<Penalty> displayList;
    private final OnPenaltyClickListener listener;

    public PenaltyAdapter(Context context, List<Penalty> penalties,
                          OnPenaltyClickListener listener) {
        this.context     = context;
        this.fullList    = new ArrayList<>(penalties);
        this.displayList = new ArrayList<>(penalties);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_penalty, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Penalty p = displayList.get(position);

        holder.tvName.setText(p.getUserName());
        holder.tvItem.setText(p.getItemName());
        holder.tvBorrowDate.setText(p.getBorrowDate());
        holder.tvDueDate.setText(p.getDueDate());
        holder.tvCost.setText(String.format(Locale.getDefault(), "₱%.2f", p.getPenaltyCost()));
        holder.tvStatus.setText(p.getPaymentStatus());

        // Status badge color
        switch (p.getPaymentStatus()) {
            case "Paid":
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_done);
                break;
            case "Waived":
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_viewed);
                break;
            default: // Unpaid
                holder.tvStatus.setBackgroundResource(R.drawable.badge_status_pending);
        }

        // Show/hide Mark as Paid button and Paid label based on status
        if ("Paid".equals(p.getPaymentStatus())) {
            holder.btnMarkPaid.setVisibility(View.GONE);
            holder.tvPaidLabel.setVisibility(View.VISIBLE);
        } else {
            holder.btnMarkPaid.setVisibility(View.VISIBLE);
            holder.tvPaidLabel.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPenaltyClick(p, holder.getAdapterPosition());
        });

        holder.btnMarkPaid.setOnClickListener(v -> {
            if (listener != null) listener.onMarkPaidClick(p, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    /** Filter by payment status tab. Pass "All" to show everything. */
    public void filter(String status) {
        displayList.clear();
        if ("All".equals(status)) {
            displayList.addAll(fullList);
        } else {
            for (Penalty p : fullList) {
                if (p.getPaymentStatus().equals(status)) displayList.add(p);
            }
        }
        notifyDataSetChanged();
    }

    /** Compute total amount for a given status (for stat cards) */
    public double sumByStatus(String status) {
        double total = 0;
        for (Penalty p : fullList) {
            if (p.getPaymentStatus().equals(status)) total += p.getPenaltyCost();
        }
        return total;
    }

    public void addPenalty(Penalty penalty) {
        fullList.add(0, penalty);
        displayList.add(0, penalty);
        notifyItemInserted(0);
    }

    public void setData(List<Penalty> penalties) {
        fullList    = new ArrayList<>(penalties);
        displayList = new ArrayList<>(penalties);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvItem, tvBorrowDate, tvDueDate, tvCost, tvStatus, tvPaidLabel;
        Button btnMarkPaid;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName       = itemView.findViewById(R.id.tvPenaltyUserName);
            tvItem       = itemView.findViewById(R.id.tvPenaltyItem);
            tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);
            tvDueDate    = itemView.findViewById(R.id.tvDueDate);
            tvCost       = itemView.findViewById(R.id.tvPenaltyCost);
            tvStatus     = itemView.findViewById(R.id.tvPaymentStatus);
            btnMarkPaid  = itemView.findViewById(R.id.btnMarkPaid);
            tvPaidLabel  = itemView.findViewById(R.id.tvPaidLabel);
        }
    }
}