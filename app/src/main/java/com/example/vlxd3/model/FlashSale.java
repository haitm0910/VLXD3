package com.example.vlxd3.model;

public class FlashSale {
    private int id;
    private int productId;
    private double salePrice;
    private String startDate;
    private String endDate;

    public FlashSale(int id, int productId, double salePrice, String startDate, String endDate) {
        this.id = id;
        this.productId = productId;
        this.salePrice = salePrice;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public FlashSale(int productId, double salePrice, String startDate, String endDate) {
        this(-1, productId, salePrice, startDate, endDate);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
