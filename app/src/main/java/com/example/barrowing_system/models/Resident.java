package com.example.barrowing_system.models;

public class Resident {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String status;       // "Active", "Suspended"
    private String joinedDate;
    private int    totalBorrows;

    public Resident() {}

    public Resident(String id, String fullName, String email, String phone,
                    String address, String status, String joinedDate, int totalBorrows) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.joinedDate = joinedDate;
        this.totalBorrows = totalBorrows;
    }

    public String getId()           { return id; }
    public String getFullName()     { return fullName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getAddress()      { return address; }
    public String getStatus()       { return status; }
    public String getJoinedDate()   { return joinedDate; }
    public int    getTotalBorrows() { return totalBorrows; }

    public void setId(String id)               { this.id = id; }
    public void setFullName(String fullName)   { this.fullName = fullName; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
    public void setAddress(String address)     { this.address = address; }
    public void setStatus(String status)       { this.status = status; }
    public void setJoinedDate(String date)     { this.joinedDate = date; }
    public void setTotalBorrows(int n)         { this.totalBorrows = n; }
}
