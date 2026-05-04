package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.BorrowResourceAdapter;
import com.example.barrowing_system.models.InventoryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BorrowActivity extends AppCompatActivity
        implements BorrowResourceAdapter.OnBorrowClickListener {

    private RecyclerView          rvItems;
    private BorrowResourceAdapter adapter;
    private ChipGroup             chipGroupCategory;
    private TextInputEditText     etSearch;
    private ProgressBar           progressBar;
    private TextView              tvEmptyState;

    private FirebaseFirestore    db;
    private ListenerRegistration inventoryListener;

    private String activeCategory = "All";
    private String activeSearch   = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar  = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvItems = findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BorrowResourceAdapter(this, new ArrayList<>(), this);
        rvItems.setAdapter(adapter);

        setupSearch();
        setupCategoryChips();
        loadInventory();
    }

    private void setupSearch() {
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                activeSearch = s.toString();
                adapter.filterAndSearch(activeCategory, activeSearch);
                showEmptyIfNeeded();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryChips() {
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipCatChairs)    activeCategory = "Chairs";
            else if (id == R.id.chipCatTables)    activeCategory = "Tables";
            else if (id == R.id.chipCatTents)     activeCategory = "Tents";
            else if (id == R.id.chipCatEquipment) activeCategory = "Equipment";
            else if (id == R.id.chipCatOthers)    activeCategory = "Others";
            else                                   activeCategory = "All";
            adapter.filterAndSearch(activeCategory, activeSearch);
            showEmptyIfNeeded();
        });
    }

    private void loadInventory() {
        progressBar.setVisibility(View.VISIBLE);

        inventoryListener = db.collection("inventory")
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    List<InventoryItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        InventoryItem item = new InventoryItem(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("category"),
                                doc.getLong("quantity") != null
                                        ? doc.getLong("quantity").intValue() : 0,
                                doc.getLong("availableQty") != null
                                        ? doc.getLong("availableQty").intValue() : 0
                        );
                        item.setImageBase64(doc.getString("imageBase64"));
                        items.add(item);
                    }
                    adapter.setData(items);
                    adapter.filterAndSearch(activeCategory, activeSearch);
                    showEmptyIfNeeded();
                });
    }

    private void showEmptyIfNeeded() {
        boolean empty = adapter.getItemCount() == 0;
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBorrowClick(InventoryItem item) {
        Intent intent = new Intent(this, NewBorrowRequestActivity.class);
        intent.putExtra("itemId",          item.getId());
        intent.putExtra("itemName",        item.getName());
        intent.putExtra("itemCategory",    item.getCategory());
        intent.putExtra("itemQty",         item.getQuantity());
        intent.putExtra("itemAvail",       item.getAvailableQty());
        intent.putExtra("itemImageBase64", item.getImageBase64());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inventoryListener != null) inventoryListener.remove();
    }
}