package com.pharmacy.gui;

import com.pharmacy.dao.SaleDAO;
import com.pharmacy.dao.SettingsDAO;
import com.pharmacy.model.Sale;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ViewSalesDialog extends JDialog {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private SaleDAO saleDAO;
    private SettingsDAO settingsDAO;
    
    public ViewSalesDialog(JFrame parent) {
        super(parent, "View Sales", true);
        saleDAO = new SaleDAO();
        settingsDAO = new SettingsDAO();
        
        if (!verifyPassword()) {
            dispose();
            return;
        }
        
        initializeUI();
        loadSales();
    }
    
    private boolean verifyPassword() {
        JPasswordField passwordField = new JPasswordField(20);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Enter Owner Password:"), BorderLayout.NORTH);
        panel.add(passwordField, BorderLayout.CENTER);
        
        int option = JOptionPane.showConfirmDialog(this, panel, "Owner Authentication", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (settingsDAO.verifyPassword(password)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Wrong Password!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }
    
    private void initializeUI() {
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Table
        String[] columns = {"Date", "Serial", "Product Name", "Quantity", "Amount (Rs)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(25);
        salesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set column widths
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        double[] monthlySales = saleDAO.getMonthlySales();
        double totalSales = 0;
        for (double sales : monthlySales) {
            totalSales += sales;
        }
        JLabel summaryLabel = new JLabel(String.format("Total Sales: Rs %.2f", totalSales));
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(summaryLabel);
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSales() {
        tableModel.setRowCount(0);
        List<Sale> sales = saleDAO.getAllSales();
        
        for (Sale sale : sales) {
            Object[] row = {
                DateUtil.formatDate(sale.getSaleDate()),
                sale.getProductSerial(),
                sale.getProductName(),
                sale.getQuantity(),
                String.format("%.2f", sale.getAmount())
            };
            tableModel.addRow(row);
        }
    }
}
