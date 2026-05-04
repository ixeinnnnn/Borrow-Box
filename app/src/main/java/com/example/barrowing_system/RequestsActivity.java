package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.RequestAdapter;
import com.example.barrowing_system.models.Request;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity
        implements RequestAdapter.OnRequestClickListener {

    private RecyclerView       rvRequests;
    private RequestAdapter     adapter;
    private ChipGroup          chipGroup;
    private ProgressBar        progressBar;
    private TextView           tvEmptyState;
    private Button             btnLoadMore;
    private String             activeFilter = "All";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ListenerRegistration requestsListener;

    // Pagination
    private static final int PAGE_SIZE = 20;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private List<Request> allRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            finish(); // Close activity if not logged in
            return;
        }

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Check for history filter from UserDashboardActivity
        String filter = getIntent().getStringExtra("filter");
        if ("history".equals(filter)) {
            activeFilter = "Done"; // Show completed/returned items for history
            // Update toolbar title
            toolbar.setTitle("History");
        }

        // RecyclerView
        rvRequests = findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnLoadMore = findViewById(R.id.btnLoadMore);

        // Initialize adapter with empty list
        adapter = new RequestAdapter(this, new ArrayList<>(), this);
        rvRequests.setAdapter(adapter);

        // Chip filter
        chipGroup = findViewById(R.id.chipGroupFilter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipAll)     activeFilter = "All";
            else if (id == R.id.chipPending) activeFilter = "Pending";
            else if (id == R.id.chipViewed)  activeFilter = "Viewed";
            else if (id == R.id.chipDone)    activeFilter = "Done";
            adapter.filter(activeFilter);
        });

        // Load More button
        btnLoadMore.setOnClickListener(v -> loadMoreRequests());

        // Load real-time data from Firestore
        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;
        lastVisible = null;
        isLastPage = false;
        allRequests.clear();

        Query query = db.collection("requests")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("borrowDate", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        requestsListener = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            isLoading = false;

            if (e != null) {
                Toast.makeText(this, "Error loading requests: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                allRequests.clear();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Request request = new Request(
                            doc.getId(),
                            doc.getString("requesterName"),
                            doc.getString("requesterEmail"),
                            doc.getString("itemName"),
                            doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1,
                            doc.getString("borrowDate"),
                            doc.getString("returnDate"),
                            doc.getString("purpose"),
                            doc.getString("status")
                    );
                    allRequests.add(request);
                }

                lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                isLastPage = snapshots.size() < PAGE_SIZE;

                adapter.setData(allRequests);
                adapter.filter(activeFilter);

                if (allRequests.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvRequests.setVisibility(View.GONE);
                    btnLoadMore.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvRequests.setVisibility(View.VISIBLE);
                    btnLoadMore.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    private void loadMoreRequests() {
        if (isLoading || isLastPage || lastVisible == null) return;

        isLoading = true;
        btnLoadMore.setText("Loading...");
        btnLoadMore.setEnabled(false);

        Query query = db.collection("requests")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("borrowDate", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(PAGE_SIZE);

        query.get().addOnSuccessListener(snapshots -> {
            isLoading = false;
            btnLoadMore.setText("Load More");
            btnLoadMore.setEnabled(true);

            if (snapshots.isEmpty()) {
                isLastPage = true;
                btnLoadMore.setVisibility(View.GONE);
                return;
            }

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Request request = new Request(
                        doc.getId(),
                        doc.getString("requesterName"),
                        doc.getString("requesterEmail"),
                        doc.getString("itemName"),
                        doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1,
                        doc.getString("borrowDate"),
                        doc.getString("returnDate"),
                        doc.getString("purpose"),
                        doc.getString("status")
                );
                allRequests.add(request);
            }

            lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
            isLastPage = snapshots.size() < PAGE_SIZE;

            adapter.setData(allRequests);
            adapter.filter(activeFilter);

            btnLoadMore.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
        }).addOnFailureListener(e -> {
            isLoading = false;
            btnLoadMore.setText("Load More");
            btnLoadMore.setEnabled(true);
            Toast.makeText(this, "Failed to load more: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestClick(Request request, int position) {
        // Auto-update to "Viewed" if currently "Pending"
        if ("Pending".equals(request.getStatus())) {
            db.collection("requests").document(request.getId())
                    .update("status", "Viewed")
                    .addOnSuccessListener(aVoid -> {
                        adapter.updateStatus(position, "Viewed");
                        navigateToDetail(request, position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update status: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        navigateToDetail(request, position);
                    });
        } else {
            navigateToDetail(request, position);
        }
    }

    private void navigateToDetail(Request request, int position) {
        Intent intent = new Intent(this, RequestDetailActivity.class);
        intent.putExtra("id", request.getId());
        intent.putExtra("name", request.getRequesterName());
        intent.putExtra("email", request.getRequesterEmail());
        intent.putExtra("item", request.getItemName() + " (x" + request.getQuantity() + ")");
        intent.putExtra("qty", request.getQuantity());
        intent.putExtra("date", request.getBorrowDate());
        intent.putExtra("returnDate", request.getReturnDate());
        intent.putExtra("purpose", request.getPurpose());
        intent.putExtra("status", request.getStatus());
        intent.putExtra("position", position);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refresh list if status changed inside detail view
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int  pos       = data.getIntExtra("position", -1);
            String status  = data.getStringExtra("newStatus");
            if (pos >= 0 && status != null) {
                adapter.updateStatus(pos, status);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestsListener != null) requestsListener.remove();
    }
}