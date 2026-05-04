package com.example.barrowing_system;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barrowing_system.adapters.SelectResourceAdapter;
import com.example.barrowing_system.models.InventoryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewBorrowRequestActivity extends AppCompatActivity {

    // Views
    private ImageView         ivSelectedItemImage;
    private TextView          tvSelectedItemName, tvSelectedItemCategory,
            tvSelectedItemAvail, tvResourceDropdown,
            tvQuantity, tvMaxQty;
    private CardView          cardSelectedItem, cardResourceDropdown;
    private Button            btnMinus, btnPlus, btnSubmitRequest;
    private TextInputEditText etBorrowDate, etReturnDate, etPurpose,
            etContactNumber, etNotes;

    // State
    private InventoryItem       selectedItem = null;
    private int                 quantity     = 1;
    private List<InventoryItem> allInventory = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth      mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_borrow_request);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadInventoryList();
        setListeners();
        handleIncomingItem();
    }

    private void initViews() {
        ivSelectedItemImage    = findViewById(R.id.ivSelectedItemImage);
        tvSelectedItemName     = findViewById(R.id.tvSelectedItemName);
        tvSelectedItemCategory = findViewById(R.id.tvSelectedItemCategory);
        tvSelectedItemAvail    = findViewById(R.id.tvSelectedItemAvail);
        tvResourceDropdown     = findViewById(R.id.tvResourceDropdown);
        tvQuantity             = findViewById(R.id.tvQuantity);
        tvMaxQty               = findViewById(R.id.tvMaxQty);
        cardSelectedItem       = findViewById(R.id.cardSelectedItem);
        cardResourceDropdown   = findViewById(R.id.cardResourceDropdown);
        btnMinus               = findViewById(R.id.btnMinus);
        btnPlus                = findViewById(R.id.btnPlus);
        btnSubmitRequest       = findViewById(R.id.btnSubmitRequest);
        etBorrowDate           = findViewById(R.id.etBorrowDate);
        etReturnDate           = findViewById(R.id.etReturnDate);
        etPurpose              = findViewById(R.id.etPurpose);
        etContactNumber        = findViewById(R.id.etContactNumber);
        etNotes                = findViewById(R.id.etNotes);
    }

    // ── Pre-fill if launched from BorrowActivity ──────────
    private void handleIncomingItem() {
        String id          = getIntent().getStringExtra("itemId");
        String name        = getIntent().getStringExtra("itemName");
        String category    = getIntent().getStringExtra("itemCategory");
        int    qty         = getIntent().getIntExtra("itemQty", 0);
        int    avail       = getIntent().getIntExtra("itemAvail", 0);
        String imageBase64 = getIntent().getStringExtra("itemImageBase64");

        if (id != null && name != null) {
            selectedItem = new InventoryItem(id, name, category, qty, avail);
            selectedItem.setImageBase64(imageBase64);
            updateSelectedItemUI();
        }
    }

    // ── Load all inventory for resource picker ────────────
    private void loadInventoryList() {
        db.collection("inventory").get()
                .addOnSuccessListener(snapshots -> {
                    allInventory.clear();
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
                        allInventory.add(item);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load resources",
                                Toast.LENGTH_SHORT).show());
    }

    private void setListeners() {
        cardSelectedItem.setOnClickListener(v -> showResourcePickerSheet());
        cardResourceDropdown.setOnClickListener(v -> showResourcePickerSheet());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            int max = selectedItem != null ? selectedItem.getAvailableQty() : 1;
            if (quantity < max) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this,
                        "Maximum available: " + max,
                        Toast.LENGTH_SHORT).show();
            }
        });

        etBorrowDate.setOnClickListener(v -> showDatePicker(etBorrowDate, false));
        etReturnDate.setOnClickListener(v -> showDatePicker(etReturnDate, true));
        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    // ── Resource picker bottom sheet ──────────────────────
    private void showResourcePickerSheet() {
        if (allInventory.isEmpty()) {
            Toast.makeText(this, "Loading resources...", Toast.LENGTH_SHORT).show();
            loadInventoryList();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_select_resource, null);
        dialog.setContentView(sheetView);

        RecyclerView rv = sheetView.findViewById(R.id.rvSelectResource);
        rv.setLayoutManager(new LinearLayoutManager(this));

        SelectResourceAdapter sheetAdapter = new SelectResourceAdapter(
                this, allInventory, item -> {
            selectedItem = item;
            quantity     = 1;
            tvQuantity.setText("1");
            updateSelectedItemUI();
            dialog.dismiss();
        });

        if (selectedItem != null) sheetAdapter.setSelectedId(selectedItem.getId());
        rv.setAdapter(sheetAdapter);

        ImageButton btnClose = sheetView.findViewById(R.id.btnCloseSheet);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ── Update preview card ───────────────────────────────
    private void updateSelectedItemUI() {
        if (selectedItem == null) return;

        tvSelectedItemName.setText(selectedItem.getName());
        tvSelectedItemCategory.setText(selectedItem.getCategory());
        tvSelectedItemAvail.setText(selectedItem.getAvailableQty()
                + " of " + selectedItem.getQuantity() + " available");
        tvResourceDropdown.setText(selectedItem.getName());
        tvResourceDropdown.setTextColor(getColor(R.color.text_primary));
        tvMaxQty.setText("Maximum: " + selectedItem.getAvailableQty() + " pcs");

        // Load Base64 image into preview
        String base64 = selectedItem.getImageBase64();
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ivSelectedItemImage.setImageBitmap(bitmap);
                ivSelectedItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivSelectedItemImage.setImageResource(R.drawable.ic_inventory);
            }
        } else {
            ivSelectedItemImage.setImageResource(R.drawable.ic_inventory);
        }
    }

    // ── Date picker ───────────────────────────────────────
    private void showDatePicker(TextInputEditText target, boolean mustBeAfterBorrow) {
        Calendar cal = Calendar.getInstance();
        if (mustBeAfterBorrow) cal.add(Calendar.DAY_OF_MONTH, 1);

        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d", month + 1, day, year);
            target.setText(date);
        }, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ── Validate + submit to Firestore ────────────────────
    private void submitRequest() {
        if (selectedItem == null) {
            Toast.makeText(this, "Please select a resource", Toast.LENGTH_SHORT).show();
            return;
        }

        String borrowDate = etBorrowDate.getText() != null
                ? etBorrowDate.getText().toString().trim() : "";
        String returnDate = etReturnDate.getText() != null
                ? etReturnDate.getText().toString().trim() : "";
        String purpose    = etPurpose.getText() != null
                ? etPurpose.getText().toString().trim() : "";
        String contact    = etContactNumber.getText() != null
                ? etContactNumber.getText().toString().trim() : "";
        String notes      = etNotes.getText() != null
                ? etNotes.getText().toString().trim() : "";

        if (borrowDate.isEmpty()) { etBorrowDate.setError("Required"); return; }
        if (returnDate.isEmpty()) { etReturnDate.setError("Required"); return; }
        if (purpose.isEmpty())    { etPurpose.setError("Required");    return; }
        if (contact.isEmpty())    { etContactNumber.setError("Required"); return; }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitRequest.setEnabled(false);
        btnSubmitRequest.setText("Submitting...");

        Map<String, Object> request = new HashMap<>();
        request.put("userId",         user.getUid());
        request.put("requesterEmail", user.getEmail());
        request.put("requesterName",  "");
        request.put("itemId",         selectedItem.getId());
        request.put("itemName",       selectedItem.getName());
        request.put("quantity",       quantity);
        request.put("borrowDate",     borrowDate);
        request.put("returnDate",     returnDate);
        request.put("purpose",        purpose);
        request.put("contactNumber",  contact);
        request.put("notes",          notes);
        request.put("status",         "Pending");

        // Fetch full name then save
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String fullName = doc.getString("fullName");
                    if (fullName != null) request.put("requesterName", fullName);
                    saveRequest(request);
                })
                .addOnFailureListener(e -> saveRequest(request));
    }

    private void saveRequest(Map<String, Object> request) {
        db.collection("requests").add(request)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this,
                            "Request submitted successfully! ✓",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnSubmitRequest.setEnabled(true);
                    btnSubmitRequest.setText("Submit Borrow Request");
                });
    }
}