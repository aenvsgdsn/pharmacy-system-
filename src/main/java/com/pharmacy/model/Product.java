package com.pharmacy.model;

import java.time.LocalDate;

public class Product {
    private int serial;
    private String name;
    private String salt;
    private String company;
    private String distributor;
    private String batch;
    private LocalDate purchaseDate;
    private LocalDate mfgDate;
    private LocalDate expDate;
    private double price;
    private int quantity;

    public Product() {
    }

    public Product(int serial, String name, String company, 
                   String distributor, String batch, LocalDate purchaseDate, 
                   LocalDate expDate, double price, int quantity) {
        this.serial = serial;
        this.name = name;
        this.company = company;
        this.distributor = distributor;
        this.batch = batch;
        this.purchaseDate = purchaseDate;
        this.expDate = expDate;
        this.price = price;
        this.quantity = quantity;
        this.salt = "";
        this.mfgDate = purchaseDate;
    }

    // Getters and Setters
    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(LocalDate mfgDate) {
        this.mfgDate = mfgDate;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
