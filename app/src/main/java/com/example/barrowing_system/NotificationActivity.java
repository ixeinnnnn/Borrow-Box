package com.example.barrowing_system;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.NotificationAdapter;
import com.example.barrowing_system.models.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private ListenerRegistration notificationsListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        adapter = new NotificationAdapter(this, new ArrayList<>(), this);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Please log in to view notifications.");
            return;
        }

        notificationsListener = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Toast.makeText(this, "Error loading notifications: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Notification notification = new Notification(
                                    doc.getId(),
                                    doc.getString("userId"),
                                    doc.getString("title"),
                                    doc.getString("message"),
                                    doc.getString("type"),
                                    doc.getBoolean("isRead") != null ? doc.getBoolean("isRead") : false,
                                    doc.getTimestamp("createdAt")
                            );
                            notifications.add(notification);
                        }

                        adapter.setData(notifications);

                        if (notifications.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvNotifications.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvNotifications.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification, int position) {
        // Mark as read if unread
        if (!notification.isRead()) {
            db.collection("notifications")
                    .document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        notification.setRead(true);
                        adapter.updateItem(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to mark as read: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) notificationsListener.remove();
    }
}
