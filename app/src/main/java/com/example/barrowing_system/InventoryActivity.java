package com.example.barrowing_system;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.barrowing_system.adapters.InventoryAdapter;
import com.example.barrowing_system.models.InventoryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends AppCompatActivity
        implements InventoryAdapter.OnItemActionListener {

    private RecyclerView      rvInventory;
    private InventoryAdapter  adapter;
    private TextInputEditText etSearch;
    private ProgressBar       progressBar;
    private TextView          tvEmptyState;

    private FirebaseFirestore    db;
    private ListenerRegistration inventoryListener;

    // Sheet state
    private String    selectedBase64Image = null;
    private String    existingBase64      = null;
    private ImageView ivItemImagePreview;

    // Image picker launcher
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        String base64 = uriToBase64(uri);
                        if (base64 != null) {
                            selectedBase64Image = base64;
                            if (ivItemImagePreview != null) {
                                Glide.with(this).load(uri).centerCrop()
                                        .into(ivItemImagePreview);
                            }
                            Toast.makeText(this, "Image selected ✓",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Image too large. Please choose a smaller image.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply dark mode preference as the VERY FIRST thing
        android.content.SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_inventory);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar  = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvInventory = findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InventoryAdapter(this, new ArrayList<>(), this);
        rvInventory.setAdapter(adapter);

        // Live search
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.search(s.toString());
                showEmptyIfNeeded();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnAddItem).setOnClickListener(
                v -> showAddItemSheet(null, -1));

        loadInventory();
    }

    // ── Load inventory from Firestore ─────────────────────
    private void loadInventory() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        inventoryListener = db.collection("inventory")
                .addSnapshotListener((snapshots, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this,
                                "Error: " + error.getMessage(),
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
                    showEmptyIfNeeded();
                });
    }

    private void showEmptyIfNeeded() {
        if (tvEmptyState == null) return;
        boolean empty = adapter.getItemCount() == 0;
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvInventory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClick(InventoryItem item, int position) {
        showAddItemSheet(item, position);
    }

    @Override
    public void onItemClick(InventoryItem item, int position) {
        Toast.makeText(this,
                item.getName() + " — qty: " + item.getQuantity(),
                Toast.LENGTH_SHORT).show();
    }

    // ── Add / Edit item bottom sheet ──────────────────────
    private void showAddItemSheet(InventoryItem existingItem, int editPosition) {
        boolean isEdit      = existingItem != null;
        selectedBase64Image = null;
        existingBase64      = isEdit ? existingItem.getImageBase64() : null;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_add_item, null);
        dialog.setContentView(sheetView);

        // Set soft input mode to adjust resize
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ivItemImagePreview  = sheetView.findViewById(R.id.ivItemImagePreview);
        Button btnPickImage = sheetView.findViewById(R.id.btnPickImage);

        // Show existing image if editing
        if (existingBase64 != null && !existingBase64.isEmpty()) {
            loadBase64IntoImageView(existingBase64, ivItemImagePreview);
        }

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Category dropdown
        AutoCompleteTextView actvCategory = sheetView.findViewById(R.id.actvCategory);
        String[] categories = {"Furniture", "Chairs", "Tables", "Tents",
                "Electronics", "Sports Equipment", "Kitchen", "Tools", "Others"};
        actvCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        actvCategory.setFocusable(false);
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());

        TextInputEditText etName = sheetView.findViewById(R.id.etItemName);
        TextInputEditText etQty  = sheetView.findViewById(R.id.etQuantity);
        Button btnAdd    = sheetView.findViewById(R.id.btnAddItemSubmit);
        Button btnCancel = sheetView.findViewById(R.id.btnCancel);

        if (isEdit) {
            etName.setText(existingItem.getName());
            actvCategory.setText(existingItem.getCategory(), false);
            etQty.setText(String.valueOf(existingItem.getQuantity()));
            btnAdd.setText("Update Item");
        }

        btnAdd.setOnClickListener(v -> {
            String name     = etName.getText() != null
                    ? etName.getText().toString().trim() : "";
            String category = actvCategory.getText().toString().trim();
            String qtyStr   = etQty.getText() != null
                    ? etQty.getText().toString().trim() : "";

            if (name.isEmpty())     { etName.setError("Required");      return; }
            if (category.isEmpty()) { actvCategory.setError("Required"); return; }
            if (qtyStr.isEmpty())   { etQty.setError("Required");       return; }

            int qty;
            try { qty = Integer.parseInt(qtyStr); }
            catch (NumberFormatException e) { etQty.setError("Invalid"); return; }

            btnAdd.setEnabled(false);
            btnAdd.setText("Saving...");

            // Decide which Base64 to use
            String imageToSave = "";
            if (selectedBase64Image != null) {
                imageToSave = selectedBase64Image;       // new image picked
            } else if (existingBase64 != null) {
                imageToSave = existingBase64;            // keep existing
            }

            saveToFirestore(name, category, qty, imageToSave,
                    isEdit, existingItem, dialog, btnAdd);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ── Save to Firestore ─────────────────────────────────
    private void saveToFirestore(String name, String category, int qty,
                                 String imageBase64, boolean isEdit,
                                 InventoryItem existing,
                                 BottomSheetDialog dialog, Button btnAdd) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",        name);
        data.put("category",    category);
        data.put("quantity",    qty);
        data.put("imageBase64", imageBase64);

        if (isEdit && existing != null) {
            data.put("availableQty", existing.getAvailableQty());
            db.collection("inventory").document(existing.getId())
                    .update(data)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, name + " updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnAdd.setEnabled(true);
                        btnAdd.setText("Update Item");
                    });
        } else {
            data.put("availableQty", qty);
            db.collection("inventory").add(data)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, name + " added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnAdd.setEnabled(true);
                        btnAdd.setText("Add Item");
                    });
        }
    }

    // ── URI → compressed Base64 string ────────────────────
    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);

            // Resize to max 500px wide to keep size under 1MB Firestore limit
            int maxWidth = 500;
            int w = original.getWidth();
            int h = original.getHeight();
            if (w > maxWidth) {
                float ratio = (float) maxWidth / w;
                w = maxWidth;
                h = (int) (h * ratio);
            }
            Bitmap resized = Bitmap.createScaledBitmap(original, w, h, true);

            // Compress at 65% quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 65, baos);
            byte[] bytes = baos.toByteArray();

            // Firestore limit is 1MB per document — reject if too large
            if (bytes.length > 900_000) {
                return null;
            }

            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Base64 string → Bitmap → ImageView ───────────────
    public static void loadBase64IntoImageView(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty()) return;
        try {
            byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inventoryListener != null) inventoryListener.remove();
    }
}