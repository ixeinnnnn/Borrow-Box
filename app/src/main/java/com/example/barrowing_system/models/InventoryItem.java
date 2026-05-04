package com.example.barrowing_system.models;

/**
 * InventoryItem model
 * Uses imageBase64 (string) instead of Firebase Storage URL
 * so images are stored directly in Firestore — no paid plan needed.
 */
public class InventoryItem {

    private String id;
    private String name;
    private String category;
    private int    quantity;
    private int    availableQty;
    private String imageBase64;  // Base64-encoded JPEG string stored in Firestore

    // Required empty constructor for Firestore
    public InventoryItem() {}

    public InventoryItem(String id, String name, String category,
                         int quantity, int availableQty) {
        this.id           = id;
        this.name         = name;
        this.category     = category;
        this.quantity     = quantity;
        this.availableQty = availableQty;
    }

    public InventoryItem(String id, String name, String category,
                         int quantity, int availableQty, String imageBase64) {
        this.id           = id;
        this.name         = name;
        this.category     = category;
        this.quantity     = quantity;
        this.availableQty = availableQty;
        this.imageBase64  = imageBase64;
    }

    // Getters
    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getCategory()      { return category; }
    public int    getQuantity()      { return quantity; }
    public int    getAvailableQty()  { return availableQty; }
    public String getImageBase64()   { return imageBase64; }

    // Setters
    public void setId(String id)                   { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setCategory(String category)       { this.category = category; }
    public void setQuantity(int quantity)          { this.quantity = quantity; }
    public void setAvailableQty(int availableQty)  { this.availableQty = availableQty; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}