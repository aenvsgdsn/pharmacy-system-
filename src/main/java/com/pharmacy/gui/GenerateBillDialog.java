package com.pharmacy.gui;

import com.pharmacy.dao.ProductDAO;
import com.pharmacy.dao.SaleDAO;
import com.pharmacy.model.Product;
import com.pharmacy.model.Sale;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class GenerateBillDialog extends JDialog {
    private JTextField serialField, quantityField;
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private double totalAmount = 0.0;
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private java.util.List<BillItem> billItems;
    
    private static class BillItem {
        int serial;
        String productName;
        int quantity;
        double amount;
        
        BillItem(int serial, String productName, int quantity, double amount) {
            this.serial = serial;
            this.productName = productName;
            this.quantity = quantity;
            this.amount = amount;
        }
    }
    
    public GenerateBillDialog(JFrame parent) {
        super(parent, "Generate Bill", true);
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        billItems = new java.util.ArrayList<>();
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Serial No:"));
        serialField = new JTextField(10);
        inputPanel.add(serialField);
        
        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(10);
        inputPanel.add(quantityField);
        
        JButton addButton = new JButton("Add to Bill");
        addButton.addActionListener(e -> addToBill());
        inputPanel.add(addButton);
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Bill table
        String[] columns = {"Serial", "Product Name", "Quantity", "Price (Rs)", "Amount (Rs)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        billTable = new JTable(tableModel);
        billTable.setRowHeight(25);
        billTable.setFont(new Font("Arial", Font.PLAIN, 12));
        billTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(billTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Total and buttons panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        totalLabel = new JLabel("TOTAL BILL = Rs 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 100, 0));
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear Bill");
        JButton generateButton = new JButton("Generate Bill");
        JButton closeButton = new JButton("Close");
        
        clearButton.addActionListener(e -> clearBill());
        generateButton.addActionListener(e -> generateBill());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void addToBill() {
        try {
            int serial = Integer.parseInt(serialField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Product product = productDAO.getProductBySerial(serial);
            if (product == null) {
                JOptionPane.showMessageDialog(this, "Invalid Serial Number!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (DateUtil.isExpired(product.getExpDate())) {
                JOptionPane.showMessageDialog(this, "Expired Product! Cannot sell.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (product.getQuantity() <= 0) {
                JOptionPane.showMessageDialog(this, "Out of stock! Cannot sell.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int reserved = getReservedQuantityForSerial(serial);
            int available = product.getQuantity() - reserved;
            if (available <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock. Available (after items already in this bill): 0",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantity > available) {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock. Available (after items already in this bill): " + available,
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount = product.getPrice() * quantity;
            totalAmount += amount;
            
            // Store bill item
            billItems.add(new BillItem(product.getSerial(), product.getName(), quantity, amount));
            
            Object[] row = {
                product.getSerial(),
                product.getName(),
                quantity,
                String.format("%.2f", product.getPrice()),
                String.format("%.2f", amount)
            };
            
            tableModel.addRow(row);
            totalLabel.setText(String.format("TOTAL BILL = Rs %.2f", totalAmount));
            
            // Clear input fields
            serialField.setText("");
            quantityField.setText("");
            serialField.requestFocus();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getReservedQuantityForSerial(int serial) {
        int reserved = 0;
        for (BillItem item : billItems) {
            if (item.serial == serial) {
                reserved += item.quantity;
            }
        }
        return reserved;
    }
    
    private void clearBill() {
        tableModel.setRowCount(0);
        billItems.clear();
        totalAmount = 0.0;
        totalLabel.setText("TOTAL BILL = Rs 0.00");
        serialField.setText("");
        quantityField.setText("");
    }
    
    private void generateBill() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Bill is empty!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LocalDate saleDate = LocalDate.now();

        // Convert bill items to sales lines
        java.util.List<Sale> lines = new java.util.ArrayList<>();
        for (BillItem item : billItems) {
            lines.add(new Sale(saleDate, item.serial, item.productName, item.quantity, item.amount));
        }

        var result = saleDAO.recordBill(saleDate, lines);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                result.getErrorMessage() == null ? "Failed to generate bill." : result.getErrorMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Bill Generated Successfully!\nTotal Amount: Rs %.2f", totalAmount));
        if (!result.getLowStockWarnings().isEmpty()) {
            msg.append("\n\nLow stock warning (<5 units remaining):");
            for (var w : result.getLowStockWarnings()) {
                msg.append("\n- ").append(w.getProductName())
                        .append(" (S#").append(w.getProductSerial()).append(") => ")
                        .append(w.getRemainingQuantity()).append(" left");
            }
        }

        JOptionPane.showMessageDialog(this, msg.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);
        
        clearBill();
    }
}
