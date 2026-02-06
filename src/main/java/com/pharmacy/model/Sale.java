package com.pharmacy.model;

import java.time.LocalDate;

public class Sale {
    private int id;
    private LocalDate saleDate;
    private int productSerial;
    private String productName;
    private int quantity;
    private double amount;

    public Sale() {
    }

    public Sale(LocalDate saleDate, int productSerial, String productName, 
                int quantity, double amount) {
        this.saleDate = saleDate;
        this.productSerial = productSerial;
        this.productName = productName;
        this.quantity = quantity;
        this.amount = amount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public int getProductSerial() {
        return productSerial;
    }

    public void setProductSerial(int productSerial) {
        this.productSerial = productSerial;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
