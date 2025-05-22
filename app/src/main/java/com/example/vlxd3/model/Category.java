// File: Category.java
package com.example.vlxd3.model;

public class Category {
    private int id;
    private String name;
    private String image; // <-- THÊM THUỘC TÍNH NÀY

    public Category(int id, String name, String image) { // <-- SỬA CONSTRUCTOR
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public Category(String name, String image) { // <-- SỬA CONSTRUCTOR
        this(-1, name, image);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; } // <-- THÊM GETTER
    public void setImage(String image) { this.image = image; } // <-- THÊM SETTER
}