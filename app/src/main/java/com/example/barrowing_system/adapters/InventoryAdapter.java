package com.example.barrowing_system.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEditClick(InventoryItem item, int position);
        void onItemClick(InventoryItem item, int position);
    }

    private final Context context;
    private List<InventoryItem> fullList;
    private List<InventoryItem> displayList;
    private OnItemActionListener listener;

    public InventoryAdapter(Context context, List<InventoryItem> items,
                            OnItemActionListener listener) {
        this.context     = context;
        this.fullList    = new ArrayList<>(items);
        this.displayList = new ArrayList<>(items);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = displayList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        int avail = item.getAvailableQty();
        holder.tvAvailable.setText(avail + " available");
        // Color red if stock is low (≤ 2)
        holder.tvAvailable.setTextColor(context.getColor(
                avail <= 2 ? R.color.red_overdue : R.color.green_active));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item, holder.getAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    /** Live search filter */
    public void search(String query) {
        displayList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String q = query.trim().toLowerCase();
            for (InventoryItem item : fullList) {
                if (item.getName().toLowerCase().contains(q)
                        || item.getCategory().toLowerCase().contains(q)) {
                    displayList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Add a newly created item to the top of the list */
    public void addItem(InventoryItem item) {
        fullList.add(0, item);
        displayList.add(0, item);
        notifyItemInserted(0);
    }

    /** Update an existing item by position */
    public void updateItem(int position, InventoryItem updated) {
        if (position < 0 || position >= displayList.size()) return;
        String id = displayList.get(position).getId();
        displayList.set(position, updated);
        for (int i = 0; i < fullList.size(); i++) {
            if (fullList.get(i).getId().equals(id)) {
                fullList.set(i, updated);
                break;
            }
        }
        notifyItemChanged(position);
    }

    public void setData(List<InventoryItem> items) {
        fullList    = new ArrayList<>(items);
        displayList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName, tvCategory, tvQty, tvAvailable;
        ImageButton btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvInvItemName);
            tvCategory  = itemView.findViewById(R.id.tvInvCategory);
            tvQty       = itemView.findViewById(R.id.tvInvQty);
            tvAvailable = itemView.findViewById(R.id.tvInvAvailable);
            btnEdit     = itemView.findViewById(R.id.btnEditItem);
        }
    }
}
