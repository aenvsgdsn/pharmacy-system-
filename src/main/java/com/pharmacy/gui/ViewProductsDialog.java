package com.pharmacy.gui;

import com.pharmacy.dao.ProductDAO;
import com.pharmacy.model.Product;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ViewProductsDialog extends JDialog {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private ProductDAO productDAO;
    private java.util.List<Product> productsList;
    
    public ViewProductsDialog(JFrame parent) {
        super(parent, "View Products", false);
        productDAO = new ProductDAO();
        initializeUI();
        loadProducts();
    }
    
    private void initializeUI() {
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Table
        String[] columns = {"Serial", "Name", "Salt", "Price (Rs)", "MFG Date", "EXP Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set custom renderer for row coloring
        productTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (productsList != null && row < productsList.size()) {
                    Product p = productsList.get(row);
                    if (DateUtil.isExpired(p.getExpDate())) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else if (DateUtil.isNearExpiry(p.getExpDate())) {
                        c.setBackground(new Color(128, 0, 128));
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    }
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                return c;
            }
        });
        
        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Note: Products near expiry (≤6 months) are highlighted in purple.");
        infoLabel.setForeground(new Color(128, 0, 128));
        infoPanel.add(infoLabel);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        add(mainPanel);
    }
    
    private void loadProducts() {
        tableModel.setRowCount(0);
        productsList = productDAO.getAllProducts();
        
        for (Product product : productsList) {
            String status;
            
            if (DateUtil.isExpired(product.getExpDate())) {
                status = "Expired";
            } else if (DateUtil.isNearExpiry(product.getExpDate())) {
                status = "Near Expiry";
            } else {
                status = "Valid";
            }
            
            Object[] row = {
                product.getSerial(),
                product.getName(),
                product.getSalt(),
                String.format("%.2f", product.getPrice()),
                DateUtil.formatDate(product.getMfgDate()),
                DateUtil.formatDate(product.getExpDate()),
                status
            };
            
            tableModel.addRow(row);
        }
    }
}
