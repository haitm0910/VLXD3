// File: User.java
package com.example.vlxd3.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;   // <-- THÊM THUỘC TÍNH NÀY
    private String address; // <-- THÊM THUỘC TÍNH NÀY

    // Constructor đầy đủ
    public User(int id, String username, String password, String fullName, String phone, String email, String address) { // <-- CẬP NHẬT CONSTRUCTOR
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // Constructor khi tạo User mới (id tự động tăng)
    public User(String username, String password, String fullName, String phone, String email, String address) { // <-- CẬP NHẬT CONSTRUCTOR
        this(-1, username, password, fullName, phone, email, address);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }   // <-- THÊM GETTER
    public void setEmail(String email) { this.email = email; } // <-- THÊM SETTER
    public String getAddress() { return address; } // <-- THÊM GETTER
    public void setAddress(String address) { this.address = address; } // <-- THÊM SETTER
}