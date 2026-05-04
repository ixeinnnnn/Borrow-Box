package com.example.barrowing_system.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.R;
import com.example.barrowing_system.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class SelectResourceAdapter
        extends RecyclerView.Adapter<SelectResourceAdapter.ViewHolder> {

    public interface OnResourceSelectedListener {
        void onResourceSelected(InventoryItem item);
    }

    private final Context context;
    private final List<InventoryItem> items;
    private final OnResourceSelectedListener listener;
    private String selectedId = null;

    public SelectResourceAdapter(Context context, List<InventoryItem> items,
                                 OnResourceSelectedListener listener) {
        this.context  = context;
        this.items    = new ArrayList<>(items);
        this.listener = listener;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_select_resource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item       = items.get(position);
        boolean       isSelected = item.getId().equals(selectedId);

        holder.tvName.setText(item.getName());
        holder.tvInfo.setText(
                item.getCategory() + " · " + item.getAvailableQty() + " available");

        // Highlight selected row with dark background
        if (isSelected) {
            holder.itemView.setBackgroundColor(
                    context.getColor(R.color.text_primary));
            holder.tvName.setTextColor(Color.WHITE);
            holder.tvInfo.setTextColor(Color.WHITE);
            holder.ivCheck.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.card_rounded_white);
            holder.tvName.setTextColor(context.getColor(R.color.text_primary));
            holder.tvInfo.setTextColor(context.getColor(R.color.text_secondary));
            holder.ivCheck.setVisibility(View.GONE);
        }

        // Load Base64 image
        loadBase64Image(item.getImageBase64(), holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();
            if (listener != null) listener.onResourceSelected(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

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
        ImageView ivImage, ivCheck;
        TextView  tvName, tvInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage  = itemView.findViewById(R.id.ivSelectItemImage);
            ivCheck  = itemView.findViewById(R.id.ivCheckmark);
            tvName   = itemView.findViewById(R.id.tvSelectItemName);
            tvInfo   = itemView.findViewById(R.id.tvSelectItemInfo);
        }
    }
}