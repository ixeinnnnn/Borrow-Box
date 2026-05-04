package com.example.barrowing_system.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class BorrowResourceAdapter
        extends RecyclerView.Adapter<BorrowResourceAdapter.ViewHolder> {

    public interface OnBorrowClickListener {
        void onBorrowClick(InventoryItem item);
    }

    private final Context context;
    private List<InventoryItem> fullList;
    private List<InventoryItem> displayList;
    private final OnBorrowClickListener listener;

    public BorrowResourceAdapter(Context context, List<InventoryItem> items,
                                 OnBorrowClickListener listener) {
        this.context     = context;
        this.fullList    = new ArrayList<>(items);
        this.displayList = new ArrayList<>(items);
        this.listener    = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_borrow_resource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = displayList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());

        int avail = item.getAvailableQty();
        int total = item.getQuantity();
        holder.tvAvailability.setText(avail + "/" + total);

        // Green if available, red if 0
        if (avail <= 0) {
            holder.tvAvailability.setTextColor(
                    context.getColor(R.color.red_overdue));
            holder.btnBorrow.setEnabled(false);
            holder.btnBorrow.setAlpha(0.4f);
            holder.btnBorrow.setText("Unavailable");
        } else {
            holder.tvAvailability.setTextColor(
                    context.getColor(R.color.green_active));
            holder.btnBorrow.setEnabled(true);
            holder.btnBorrow.setAlpha(1f);
            holder.btnBorrow.setText("Borrow");
        }

        // Load image from Base64
        loadBase64Image(item.getImageBase64(), holder.ivImage);

        holder.btnBorrow.setOnClickListener(v -> {
            if (listener != null) listener.onBorrowClick(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBorrowClick(item);
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    // ── Combined category + search filter ─────────────────
    public void filterAndSearch(String category, String query) {
        displayList.clear();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        for (InventoryItem item : fullList) {
            boolean matchCat = "All".equals(category)
                    || (item.getCategory() != null
                    && item.getCategory().equalsIgnoreCase(category));
            boolean matchQ = q.isEmpty()
                    || (item.getName() != null
                    && item.getName().toLowerCase().contains(q));
            if (matchCat && matchQ) displayList.add(item);
        }
        notifyDataSetChanged();
    }

    public void setData(List<InventoryItem> items) {
        fullList    = new ArrayList<>(items);
        displayList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    // ── Decode Base64 → Bitmap → ImageView ───────────────
    private void loadBase64Image(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_inventory);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            return;
        }
        try {
            byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_inventory);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvName, tvCategory, tvAvailability;
        Button    btnBorrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage        = itemView.findViewById(R.id.ivItemImage);
            tvName         = itemView.findViewById(R.id.tvItemName);
            tvCategory     = itemView.findViewById(R.id.tvItemCategory);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            btnBorrow      = itemView.findViewById(R.id.btnBorrow);
        }
    }
}