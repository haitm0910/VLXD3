package com.example.vlxd3.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String phone;

    public User(int id, String username, String password, String fullName, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }

    public User(String username, String password, String fullName, String phone) {
        this(-1, username, password, fullName, phone);
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
}
