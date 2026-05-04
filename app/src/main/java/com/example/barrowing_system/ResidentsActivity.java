package com.example.barrowing_system;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.ResidentAdapter;
import com.example.barrowing_system.models.Resident;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResidentsActivity extends AppCompatActivity
        implements ResidentAdapter.OnResidentActionListener {

    private RecyclerView      rvResidents;
    private ResidentAdapter   adapter;
    private ChipGroup         chipGroup;
    private TextInputEditText etSearch;
    private ProgressBar       progressBar;
    private TextView          tvEmptyState;

    private TextView tvTotalUsers, tvActiveUsers, tvSuspendedUsers;

    private String activeFilter = "All";
    private String activeSearch = "";

    private FirebaseFirestore db;
    private ListenerRegistration residentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_residents);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalUsers     = findViewById(R.id.tvTotalUsers);
        tvActiveUsers    = findViewById(R.id.tvActiveUsers);
        tvSuspendedUsers = findViewById(R.id.tvSuspendedUsers);

        rvResidents  = findViewById(R.id.rvResidents);
        rvResidents.setLayoutManager(new LinearLayoutManager(this));

        progressBar  = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        etSearch     = findViewById(R.id.etSearch);

        adapter = new ResidentAdapter(this, new ArrayList<>(), this);
        rvResidents.setAdapter(adapter);

        updateStatCards();
        setupSearch();
        setupChips();

        loadResidents();
    }

    private void loadResidents() {
        progressBar.setVisibility(View.VISIBLE);

        residentsListener = db.collection("users")
                .whereEqualTo("role", "resident")
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Toast.makeText(this, "Error loading residents: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        List<Resident> residents = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {

                            // FIXED: totalBorrows may be stored as String or Number — handle both safely
                            int totalBorrows = 0;
                            try {
                                Object totalBorrowsObj = doc.get("totalBorrows");
                                if (totalBorrowsObj instanceof Number) {
                                    totalBorrows = ((Number) totalBorrowsObj).intValue();
                                } else if (totalBorrowsObj instanceof String) {
                                    totalBorrows = Integer.parseInt((String) totalBorrowsObj);
                                }
                            } catch (Exception ex) {
                                totalBorrows = 0;
                            }

                            Resident resident = new Resident(
                                    doc.getId(),
                                    doc.getString("fullName"),
                                    doc.getString("email"),
                                    doc.getString("phone"),
                                    doc.getString("address"),
                                    doc.getString("status"),
                                    doc.getString("joinedDate"),
                                    totalBorrows
                            );
                            residents.add(resident);
                        }

                        adapter.setData(residents);
                        adapter.filterAndSearch(activeFilter, activeSearch);
                        updateStatCards();

                        if (residents.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvResidents.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvResidents.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                activeSearch = s.toString();
                adapter.filterAndSearch(activeFilter, activeSearch);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips() {
        chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipAll)       activeFilter = "All";
            else if (id == R.id.chipActive)    activeFilter = "Active";
            else if (id == R.id.chipSuspended) activeFilter = "Suspended";
            adapter.filterAndSearch(activeFilter, activeSearch);
        });
    }

    private void updateStatCards() {
        tvTotalUsers.setText(String.valueOf(adapter.countByStatus("All")));
        tvActiveUsers.setText(String.valueOf(adapter.countByStatus("Active")));
        tvSuspendedUsers.setText(String.valueOf(adapter.countByStatus("Suspended")));
    }

    @Override
    public void onResidentClick(Resident resident, int position) {
        Toast.makeText(this, "Viewing: " + resident.getFullName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToggleStatus(Resident resident, int position) {
        String newStatus = "Active".equals(resident.getStatus()) ? "Suspended" : "Active";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        db.collection("users").document(resident.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    adapter.toggleStatus(position);
                    updateStatCards();
                    Toast.makeText(this,
                            resident.getFullName() + " is now " + newStatus,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (residentsListener != null) residentsListener.remove();
    }
}