package com.example.barrowing_system.models;
public class Penalty {
    private String id;
    private String userName;
    private String userEmail;
    private String itemName;
    private String borrowDate;
    private String dueDate;
    private double penaltyCost;
    private String paymentStatus; // "Unpaid", "Paid", "Waived"

    public Penalty() {}

    public Penalty(String id, String userName, String userEmail, String itemName,
                   String borrowDate, String dueDate,
                   double penaltyCost, String paymentStatus) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.itemName = itemName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.penaltyCost = penaltyCost;
        this.paymentStatus = paymentStatus;
    }

    public String getId()              { return id; }
    public String getUserName()        { return userName; }
    public String getUserEmail()       { return userEmail; }
    public String getItemName()        { return itemName; }
    public String getBorrowDate()      { return borrowDate; }
    public String getDueDate()         { return dueDate; }
    public double getPenaltyCost()     { return penaltyCost; }
    public String getPaymentStatus()   { return paymentStatus; }

    public void setId(String id)                       { this.id = id; }
    public void setUserName(String userName)           { this.userName = userName; }
    public void setUserEmail(String userEmail)         { this.userEmail = userEmail; }
    public void setItemName(String itemName)           { this.itemName = itemName; }
    public void setBorrowDate(String borrowDate)       { this.borrowDate = borrowDate; }
    public void setDueDate(String dueDate)             { this.dueDate = dueDate; }
    public void setPenaltyCost(double penaltyCost)     { this.penaltyCost = penaltyCost; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
