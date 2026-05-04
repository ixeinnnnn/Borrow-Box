package com.example.barrowing_system;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.PenaltyAdapter;
import com.example.barrowing_system.models.Penalty;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PenaltiesActivity extends AppCompatActivity
        implements PenaltyAdapter.OnPenaltyClickListener {

    private RecyclerView   rvPenalties;
    private PenaltyAdapter adapter;
    private ChipGroup      chipGroup;
    private ProgressBar    progressBar;
    private TextView       tvEmptyState;
    private TextView       tvTotalUnpaid, tvTotalCollected;

    private FirebaseFirestore db;
    private ListenerRegistration penaltiesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penalties);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalUnpaid    = findViewById(R.id.tvTotalUnpaid);
        tvTotalCollected = findViewById(R.id.tvTotalCollected);

        rvPenalties = findViewById(R.id.rvPenalties);
        rvPenalties.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Initialize adapter with empty list
        adapter = new PenaltyAdapter(this, new ArrayList<>(), this);
        rvPenalties.setAdapter(adapter);

        updateStatCards();
        setupChips();

        // Add Penalty button
        ImageButton btnAdd = findViewById(R.id.btnAddPenalty);
        btnAdd.setOnClickListener(v -> showAddPenaltySheet());

        loadPenalties();
    }

    private void loadPenalties() {
        progressBar.setVisibility(View.VISIBLE);

        penaltiesListener = db.collection("penalties")
                .orderBy("borrowDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Toast.makeText(this, "Error loading penalties: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        List<Penalty> penalties = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Penalty penalty = new Penalty(
                                    doc.getId(),
                                    doc.getString("userName"),
                                    doc.getString("userEmail"),
                                    doc.getString("itemName"),
                                    doc.getString("borrowDate"),
                                    doc.getString("dueDate"),
                                    doc.getDouble("penaltyCost") != null ? doc.getDouble("penaltyCost") : 0.0,
                                    doc.getString("paymentStatus")
                            );
                            penalties.add(penalty);
                        }

                        adapter.setData(penalties);
                        updateStatCards();

                        if (penalties.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvPenalties.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvPenalties.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setupChips() {
        chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String filter = "All";
            if      (id == R.id.chipUnpaid) filter = "Unpaid";
            else if (id == R.id.chipPaid)   filter = "Paid";
            else if (id == R.id.chipWaived) filter = "Waived";
            adapter.filter(filter);
        });
    }

    private void updateStatCards() {
        double unpaid    = adapter.sumByStatus("Unpaid");
        double collected = adapter.sumByStatus("Paid");
        tvTotalUnpaid.setText(String.format(Locale.getDefault(),    "₱%.2f", unpaid));
        tvTotalCollected.setText(String.format(Locale.getDefault(), "₱%.2f", collected));
    }

    @Override
    public void onPenaltyClick(Penalty penalty, int position) {
        Toast.makeText(this, penalty.getUserName() + " — " + penalty.getPaymentStatus(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkPaidClick(Penalty penalty, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Payment?")
                .setMessage("Mark this penalty as paid?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("penalties")
                            .document(penalty.getId())
                            .update("paymentStatus", "Paid")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Penalty marked as paid ✓", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position);
                                updateStatCards();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddPenaltySheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_add_penalty, null);
        dialog.setContentView(view);

        TextInputEditText etUser   = view.findViewById(R.id.etPenaltyUser);
        TextInputEditText etItem   = view.findViewById(R.id.etPenaltyItem);
        TextInputEditText etBorrow = view.findViewById(R.id.etBorrowDate);
        TextInputEditText etDue    = view.findViewById(R.id.etDueDate);
        TextInputEditText etAmount = view.findViewById(R.id.etPenaltyAmount);
        Button btnSave             = view.findViewById(R.id.btnSavePenalty);
        Button btnCancel           = view.findViewById(R.id.btnCancelPenalty);

        // Date pickers
        etBorrow.setOnClickListener(v -> showDatePicker(etBorrow));
        etDue.setOnClickListener(v   -> showDatePicker(etDue));

        btnSave.setOnClickListener(v -> {
            String user   = etUser.getText()   != null ? etUser.getText().toString().trim()   : "";
            String item   = etItem.getText()   != null ? etItem.getText().toString().trim()   : "";
            String borrow = etBorrow.getText() != null ? etBorrow.getText().toString().trim() : "";
            String due    = etDue.getText()    != null ? etDue.getText().toString().trim()    : "";
            String amount = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

            if (user.isEmpty())   { etUser.setError("Required");   return; }
            if (item.isEmpty())   { etItem.setError("Required");   return; }
            if (borrow.isEmpty()) { etBorrow.setError("Required"); return; }
            if (due.isEmpty())    { etDue.setError("Required");    return; }
            if (amount.isEmpty()) { etAmount.setError("Required"); return; }

            double cost;
            try {
                cost = Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }

            // Save to Firestore
            Map<String, Object> newPenalty = new HashMap<>();
            newPenalty.put("userName", user);
            newPenalty.put("userEmail", user.toLowerCase().replace(" ", ".") + "@email.com");
            newPenalty.put("itemName", item);
            newPenalty.put("borrowDate", borrow);
            newPenalty.put("dueDate", due);
            newPenalty.put("penaltyCost", cost);
            newPenalty.put("paymentStatus", "Unpaid");

            db.collection("penalties")
                    .add(newPenalty)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Penalty record added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add penalty: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(),
                    "%s %d, %d",
                    new String[]{"Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec"}[month],
                    dayOfMonth, year);
            target.setText(date);
        }, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (penaltiesListener != null) penaltiesListener.remove();
    }
}