// File: Order.java
package com.example.vlxd3.model;

import java.io.Serializable; // Để có thể truyền qua Intent nếu cần

public class Order implements Serializable {
    private int id;
    private int userId;
    private String orderDate; // Ngày đặt hàng
    private double totalAmount; // Tổng tiền của đơn hàng
    private String customerName; // Tên khách hàng
    private String customerAddress; // Địa chỉ giao hàng
    private String customerPhone; // Số điện thoại
    private String paymentMethod; // Phương thức thanh toán (COD, Bank Transfer)
    private String status; // Trạng thái đơn hàng (e.g., "Pending", "Confirmed", "Delivered")

    public Order(int id, int userId, String orderDate, double totalAmount, String customerName, String customerAddress, String customerPhone, String paymentMethod, String status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public Order(int userId, String orderDate, double totalAmount, String customerName, String customerAddress, String customerPhone, String paymentMethod, String status) {
        this(-1, userId, orderDate, totalAmount, customerName, customerAddress, customerPhone, paymentMethod, status);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}