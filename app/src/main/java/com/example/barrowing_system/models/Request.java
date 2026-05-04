package com.example.barrowing_system.models;
public class Request {
    private String id;
    private String requesterName;
    private String requesterEmail;
    private String itemName;
    private int quantity;
    private String borrowDate;
    private String returnDate;
    private String purpose;
    private String status; // "Pending", "Viewed", "Done"

    // Required empty constructor for Firebase
    public Request() {}

    public Request(String id, String requesterName, String requesterEmail,
                   String itemName, int quantity, String borrowDate,
                   String returnDate, String purpose, String status) {
        this.id = id;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.itemName = itemName;
        this.quantity = quantity;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.purpose = purpose;
        this.status = status;
    }

    public String getId()             { return id; }
    public String getRequesterName()  { return requesterName; }
    public String getRequesterEmail() { return requesterEmail; }
    public String getItemName()       { return itemName; }
    public int    getQuantity()       { return quantity; }
    public String getBorrowDate()     { return borrowDate; }
    public String getReturnDate()     { return returnDate; }
    public String getPurpose()        { return purpose; }
    public String getStatus()         { return status; }

    public void setId(String id)                   { this.id = id; }
    public void setStatus(String status)           { this.status = status; }
    public void setRequesterName(String name)      { this.requesterName = name; }
    public void setRequesterEmail(String email)    { this.requesterEmail = email; }
    public void setItemName(String itemName)       { this.itemName = itemName; }
    public void setQuantity(int quantity)          { this.quantity = quantity; }
    public void setBorrowDate(String borrowDate)   { this.borrowDate = borrowDate; }
    public void setReturnDate(String returnDate)   { this.returnDate = returnDate; }
    public void setPurpose(String purpose)         { this.purpose = purpose; }
}
