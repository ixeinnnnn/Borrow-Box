package com.example.barrowing_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout llRecentRequests;
    private TextView tvViewAll;
    private BottomNavigationView bottomNav;
    private android.widget.ImageButton btnNotification;

    private FirebaseFirestore db;
    private ListenerRegistration pendingListener;
    private ListenerRegistration activeListener;
    private ListenerRegistration resourcesListener;
    private ListenerRegistration recentRequestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply dark mode preference as the VERY FIRST thing
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        setupStatCards();
        setupQuickActions();
        setupRecentRequests();
        setupBottomNav();
        setupNotificationBell();
        loadRealtimeStats();
    }

    private void setupStatCards() {
        View cardPending = findViewById(R.id.cardPending);
        setStatCard(cardPending, R.drawable.ic_clock, R.drawable.badge_yellow,
                R.color.badge_yellow_icon, "0", "Pending Requests");

        View cardActive = findViewById(R.id.cardActive);
        setStatCard(cardActive, R.drawable.ic_check_circle, R.drawable.badge_green,
                R.color.badge_green_icon, "0", "Active Borrowings");

        View cardResources = findViewById(R.id.cardResources);
        setStatCard(cardResources, R.drawable.ic_inventory, R.drawable.badge_blue,
                R.color.badge_blue_icon, "0", "Total Resources");

        View cardOverdue = findViewById(R.id.cardOverdue);
        setStatCard(cardOverdue, R.drawable.ic_warning, R.drawable.badge_red,
                R.color.badge_red_icon, "0", "Overdue Returns");
    }

    private void setStatCard(View card, int iconRes, int badgeBg, int iconTint,
                             String count, String label) {
        if (card == null) return;
        android.widget.ImageView icon = card.findViewById(R.id.statIcon);
        android.widget.FrameLayout badge = card.findViewById(R.id.statIconBadge);
        TextView tvCount = card.findViewById(R.id.statCount);
        TextView tvLabel = card.findViewById(R.id.statLabel);

        if (icon != null) icon.setImageResource(iconRes);
        if (badge != null) badge.setBackgroundResource(badgeBg);
        if (tvCount != null) tvCount.setText(count);
        if (tvLabel != null) tvLabel.setText(label);
    }

    private void loadRealtimeStats() {
        // Pending requests count
        pendingListener = db.collection("requests")
                .whereEqualTo("status", "Pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading pending requests: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        View cardPending = findViewById(R.id.cardPending);
                        setStatCard(cardPending, R.drawable.ic_clock, R.drawable.badge_yellow,
                                R.color.badge_yellow_icon,
                                String.valueOf(snapshots.size()), "Pending Requests");
                    }
                });

        // Active borrowings (Viewed status)
        activeListener = db.collection("requests")
                .whereEqualTo("status", "Viewed")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading active borrowings: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        View cardActive = findViewById(R.id.cardActive);
                        setStatCard(cardActive, R.drawable.ic_check_circle, R.drawable.badge_green,
                                R.color.badge_green_icon,
                                String.valueOf(snapshots.size()), "Active Borrowings");
                    }
                });

        // Total resources count
        resourcesListener = db.collection("inventory")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading resources: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        View cardResources = findViewById(R.id.cardResources);
                        setStatCard(cardResources, R.drawable.ic_inventory, R.drawable.badge_blue,
                                R.color.badge_blue_icon,
                                String.valueOf(snapshots.size()), "Total Resources");
                    }
                });

        // Overdue returns
        db.collection("requests")
                .whereEqualTo("status", "Pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading overdue returns: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        View cardOverdue = findViewById(R.id.cardOverdue);
                        setStatCard(cardOverdue, R.drawable.ic_warning, R.drawable.badge_red,
                                R.color.badge_red_icon,
                                String.valueOf(snapshots.size()), "Overdue Returns");
                    }
                });
    }

    private void setupQuickActions() {
        findViewById(R.id.qaRequests).setOnClickListener(v ->
                startActivity(new Intent(this, RequestsActivity.class)));

        findViewById(R.id.qaInventory).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));

        findViewById(R.id.qaResidents).setOnClickListener(v ->
                startActivity(new Intent(this, ResidentsActivity.class)));

        findViewById(R.id.qaPenalties).setOnClickListener(v ->
                startActivity(new Intent(this, PenaltiesActivity.class)));
    }

    private void setupRecentRequests() {
        llRecentRequests = findViewById(R.id.llRecentRequests);
        tvViewAll = findViewById(R.id.tvViewAll);

        tvViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, RequestsActivity.class)));

        recentRequestsListener = db.collection("requests")
                .orderBy("borrowDate", Query.Direction.DESCENDING)
                .limit(3)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading recent requests: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        llRecentRequests.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(this);

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            View itemView = inflater.inflate(
                                    R.layout.item_request, llRecentRequests, false);

                            TextView tvInitial = itemView.findViewById(R.id.tvInitial);
                            TextView tvName    = itemView.findViewById(R.id.tvRequesterName);
                            TextView tvItem    = itemView.findViewById(R.id.tvItemName);
                            TextView tvDate    = itemView.findViewById(R.id.tvDate);
                            TextView tvStatus  = itemView.findViewById(R.id.tvStatus);

                            String name   = doc.getString("requesterName");
                            String item   = doc.getString("itemName");
                            String status = doc.getString("status");
                            int qty = doc.getLong("quantity") != null
                                    ? doc.getLong("quantity").intValue() : 1;

                            // Handle borrowDate as either Timestamp or String
                            String date = "";
                            try {
                                Object borrowDateObj = doc.get("borrowDate");
                                if (borrowDateObj instanceof Timestamp) {
                                    Date d = ((Timestamp) borrowDateObj).toDate();
                                    date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                            .format(d);
                                } else if (borrowDateObj instanceof String) {
                                    date = (String) borrowDateObj;
                                }
                            } catch (Exception ex) {
                                date = "";
                            }

                            if (name   == null) name   = "Unknown";
                            if (item   == null) item   = "Unknown";
                            if (status == null) status = "Pending";

                            tvInitial.setText(!name.isEmpty()
                                    ? String.valueOf(name.charAt(0)).toUpperCase() : "?");
                            tvName.setText(name);
                            tvItem.setText(item + " (x" + qty + ")");
                            tvDate.setText(date);
                            tvStatus.setText(status);

                            // Color-code status badge
                            switch (status) {
                                case "Viewed":
                                    tvStatus.setBackgroundResource(R.drawable.badge_status_viewed);
                                    break;
                                case "Done":
                                    tvStatus.setBackgroundResource(R.drawable.badge_status_done);
                                    break;
                                case "Returned":
                                    tvStatus.setBackgroundResource(R.drawable.badge_status_returned);
                                    break;
                                default:
                                    tvStatus.setBackgroundResource(R.drawable.badge_status_pending);
                            }

                            // Click: navigate to detail
                            final String docId   = doc.getId();
                            final String fName   = name;
                            final String fItem   = item;
                            final String fDate   = date;
                            final String fStatus = status;
                            final int    fQty    = qty;

                            itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(this, RequestDetailActivity.class);
                                intent.putExtra("id",         docId);
                                intent.putExtra("name",       fName);
                                intent.putExtra("email",      doc.getString("requesterEmail"));
                                intent.putExtra("item",       fItem + " (x" + fQty + ")");
                                intent.putExtra("qty",        fQty);
                                intent.putExtra("date",       fDate);
                                intent.putExtra("returnDate", doc.getString("returnDate"));
                                intent.putExtra("purpose",    doc.getString("purpose"));
                                intent.putExtra("status",     fStatus);
                                startActivity(intent);
                            });

                            llRecentRequests.addView(itemView);
                        }
                    }
                });
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_requests) {
                startActivity(new Intent(this, RequestsActivity.class));
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
                return true;
            } else if (id == R.id.nav_residents) {
                startActivity(new Intent(this, ResidentsActivity.class));
                return true;
            } else if (id == R.id.nav_penalties) {
                startActivity(new Intent(this, PenaltiesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupNotificationBell() {
        btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationActivity.class)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingListener        != null) pendingListener.remove();
        if (activeListener         != null) activeListener.remove();
        if (resourcesListener      != null) resourcesListener.remove();
        if (recentRequestsListener != null) recentRequestsListener.remove();
    }

    // ── Send notification to a user ───────────────────────
    public void sendNotificationToUser(String userId, String title,
                                       String message, String type) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId",    userId);
        notificationData.put("title",     title);
        notificationData.put("message",   message);
        notificationData.put("type",      type);
        notificationData.put("isRead",    false);
        notificationData.put("createdAt", FieldValue.serverTimestamp()); // ← FIXED

        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(ref -> {
                    // Notification sent successfully — silent
                })
                .addOnFailureListener(e -> {
                    // Silent fail — notification is not critical
                });
    }
}