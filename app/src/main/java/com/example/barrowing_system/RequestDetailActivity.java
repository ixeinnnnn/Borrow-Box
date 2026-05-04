package com.example.barrowing_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvDetailInitial, tvDetailName, tvDetailEmail;
    private TextView tvDetailStatus, tvDetailItem, tvDetailQty;
    private TextView tvDetailBorrowDate, tvDetailReturnDate, tvDetailPurpose;
    private Button btnMarkDone, btnMarkReturned;

    private String currentStatus;
    private String requestId;
    private String requesterUserId;
    private String itemName;
    private int position;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        db = FirebaseFirestore.getInstance();

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadData();
        autoMarkViewed();
    }

    private void initViews() {
        tvDetailInitial     = findViewById(R.id.tvDetailInitial);
        tvDetailName        = findViewById(R.id.tvDetailName);
        tvDetailEmail       = findViewById(R.id.tvDetailEmail);
        tvDetailStatus      = findViewById(R.id.tvDetailStatus);
        tvDetailItem        = findViewById(R.id.tvDetailItem);
        tvDetailQty         = findViewById(R.id.tvDetailQty);
        tvDetailBorrowDate  = findViewById(R.id.tvDetailBorrowDate);
        tvDetailReturnDate  = findViewById(R.id.tvDetailReturnDate);
        tvDetailPurpose     = findViewById(R.id.tvDetailPurpose);
        btnMarkDone         = findViewById(R.id.btnMarkDone);
        btnMarkReturned     = findViewById(R.id.btnMarkReturned);
    }

    private void loadData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;

        requestId = extras.getString("id");
        String name   = extras.getString("name",   "Unknown");
        String email  = extras.getString("email",   "");
        String item   = extras.getString("item",   "Unknown");
        int qty       = extras.getInt("qty", 1);
        String date   = extras.getString("date",   "");
        String returnDate = extras.getString("returnDate", "");
        String purpose = extras.getString("purpose", "");
        currentStatus = extras.getString("status", "Pending");
        position      = extras.getInt("position", -1);
        requesterUserId = extras.getString("userId", "");
        itemName = item;

        tvDetailInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)));
        tvDetailName.setText(name);
        tvDetailEmail.setText(email != null ? email : name.toLowerCase().replace(" ", ".") + "@example.com");
        tvDetailItem.setText(item);
        tvDetailQty.setText(String.valueOf(qty));
        tvDetailBorrowDate.setText(date);
        tvDetailReturnDate.setText(returnDate);
        tvDetailPurpose.setText(purpose);

        updateStatusBadge(currentStatus);

        // Show/hide buttons based on status
        if ("Done".equals(currentStatus)) {
            btnMarkDone.setEnabled(false);
            btnMarkDone.setText("Already Completed");
            btnMarkDone.setAlpha(0.5f);
            btnMarkReturned.setVisibility(View.VISIBLE);
        } else if ("Returned".equals(currentStatus)) {
            btnMarkDone.setEnabled(false);
            btnMarkDone.setText("Already Completed");
            btnMarkDone.setAlpha(0.5f);
            btnMarkReturned.setEnabled(false);
            btnMarkReturned.setText("Already Returned");
            btnMarkReturned.setAlpha(0.5f);
            btnMarkReturned.setVisibility(View.VISIBLE);
        } else {
            btnMarkReturned.setVisibility(View.GONE);
        }

        btnMarkDone.setOnClickListener(v -> markAsDone());
        btnMarkReturned.setOnClickListener(v -> markAsReturned());
    }

    // Automatically update status to "Viewed" when admin opens the detail
    private void autoMarkViewed() {
        if ("Pending".equals(currentStatus) && requestId != null) {
            db.collection("requests").document(requestId)
                    .update("status", "Viewed")
                    .addOnSuccessListener(aVoid -> {
                        currentStatus = "Viewed";
                        updateStatusBadge("Viewed");

                        // Send notification to user
                        if (requesterUserId != null && !requesterUserId.isEmpty()) {
                            sendNotification(requesterUserId,
                                    "Request Received",
                                    "Your borrow request for " + itemName + " is being reviewed.",
                                    "request_update");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update status: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void markAsDone() {
        if (requestId != null) {
            db.collection("requests").document(requestId)
                    .update("status", "Done")
                    .addOnSuccessListener(aVoid -> {
                        currentStatus = "Done";
                        updateStatusBadge("Done");
                        btnMarkDone.setEnabled(false);
                        btnMarkDone.setText("Completed ✓");
                        btnMarkDone.setAlpha(0.5f);
                        btnMarkReturned.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Request marked as Done", Toast.LENGTH_SHORT).show();

                        // Send notification to user
                        if (requesterUserId != null && !requesterUserId.isEmpty()) {
                            sendNotification(requesterUserId,
                                    "Request Approved",
                                    "Your request for " + itemName + " has been approved and is ready.",
                                    "request_update");
                        }

                        // Return result to RequestsActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newStatus", "Done");
                        resultIntent.putExtra("position", position);
                        setResult(RESULT_OK, resultIntent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to mark as done: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void markAsReturned() {
        if (requestId != null) {
            db.collection("requests").document(requestId)
                    .update("status", "Returned")
                    .addOnSuccessListener(aVoid -> {
                        currentStatus = "Returned";
                        updateStatusBadge("Returned");
                        btnMarkReturned.setEnabled(false);
                        btnMarkReturned.setText("Returned ✓");
                        btnMarkReturned.setAlpha(0.5f);

                        Toast.makeText(this, "Request marked as Returned", Toast.LENGTH_SHORT).show();

                        // Send notification to user
                        if (requesterUserId != null && !requesterUserId.isEmpty()) {
                            sendNotification(requesterUserId,
                                    "Item Returned",
                                    "Your borrowed item " + itemName + " has been marked as returned.",
                                    "request_update");
                        }

                        // Return result to RequestsActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newStatus", "Returned");
                        resultIntent.putExtra("position", position);
                        setResult(RESULT_OK, resultIntent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to mark as returned: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void updateStatusBadge(String status) {
        tvDetailStatus.setText(status);
        switch (status) {
            case "Viewed":
                tvDetailStatus.setBackgroundResource(R.drawable.badge_status_viewed);
                break;
            case "Done":
                tvDetailStatus.setBackgroundResource(R.drawable.badge_status_done);
                break;
            case "Returned":
                tvDetailStatus.setBackgroundResource(R.drawable.badge_status_returned);
                break;
            default:
                tvDetailStatus.setBackgroundResource(R.drawable.badge_status_pending);
        }
    }

    // ── Helper method to send notification to a user ───────
    private void sendNotification(String userId, String title, String message, String type) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("isRead", false);
        notificationData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    // Notification sent successfully
                })
                .addOnFailureListener(e -> {
                    // Silent fail - notification is not critical
                });
    }
}