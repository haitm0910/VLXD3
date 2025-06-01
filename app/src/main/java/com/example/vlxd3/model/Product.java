package com.example.vlxd3.model;

public class Product {
    private int id;
    private String name;
    private int categoryId;
    private double price;
    private String image;
    private String description;
    private int stock;
    private double discountedPrice;
    private String unit;

    public Product(int id, String name, int categoryId, double price, String image, String description, int stock, String unit) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.image = image;
        this.description = description;
        this.stock = stock;
        this.unit = unit;
    }

    public Product(String name, int categoryId, double price, String image, String description, int stock, String unit) {
        this(-1, name, categoryId, price, image, description, stock, unit);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
