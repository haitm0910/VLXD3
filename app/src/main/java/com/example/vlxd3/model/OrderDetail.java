// File: OrderDetail.java
package com.example.vlxd3.model;

public class OrderDetail {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice; // Giá sản phẩm tại thời điểm đặt hàng (có thể là giá sale)

    public OrderDetail(int id, int orderId, int productId, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public OrderDetail(int orderId, int productId, int quantity, double unitPrice) {
        this(-1, orderId, productId, quantity, unitPrice);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}