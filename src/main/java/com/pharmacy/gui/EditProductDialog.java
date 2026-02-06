package com.pharmacy.gui;

import com.pharmacy.dao.ProductDAO;
import com.pharmacy.model.Product;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class EditProductDialog extends JDialog {
    private Product product;
    private JTextField serialField;
    private JTextField nameField, saltField, companyField, distributorField, batchField;
    private JFormattedTextField purchaseDateField;
    private JFormattedTextField mfgDateField;
    private JFormattedTextField expDateField;
    private JSpinner priceSpinner;
    private JSpinner quantitySpinner;
    private ProductDAO productDAO;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    
    public EditProductDialog(JFrame parent) {
        super(parent, "Edit Product", true);
        productDAO = new ProductDAO();
        initializeUI();
        loadProductData();
    }
    
    public EditProductDialog(JFrame parent, Product product) {
        super(parent, "Edit Product", true);
        this.product = product;
        productDAO = new ProductDAO();
        initializeUI();
        loadProductData();
    }
    
    private void initializeUI() {
        setSize(520, 650);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Serial (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Serial:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        serialField = new JTextField(20);
        serialField.setEditable(false);
        formPanel.add(serialField, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Salt Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Salt Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        saltField = new JTextField(20);
        formPanel.add(saltField, gbc);
        
        // Company
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        companyField = new JTextField(20);
        formPanel.add(companyField, gbc);
        
        // Distributor
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Distributor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        distributorField = new JTextField(20);
        formPanel.add(distributorField, gbc);
        
        // Batch No
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Batch No:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        batchField = new JTextField(20);
        formPanel.add(batchField, gbc);
        
        // MFG Date
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("MFG Date (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mfgDateField = createDateField();
        formPanel.add(mfgDateField, gbc);
        
        // EXP Date
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("EXP Date (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        expDateField = createDateField();
        formPanel.add(expDateField, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Price (Rs):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 1.0));
        ((JSpinner.NumberEditor) priceSpinner.getEditor()).getTextField().setColumns(10);
        formPanel.add(priceSpinner, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantity (units):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        ((JSpinner.NumberEditor) quantitySpinner.getEditor()).getTextField().setColumns(10);
        formPanel.add(quantitySpinner, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update Product");
        JButton cancelButton = new JButton("Cancel");
        
        updateButton.addActionListener(e -> updateProduct());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        getRootPane().setDefaultButton(updateButton);
        // ESC closes
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JFormattedTextField createDateField() {
        try {
            javax.swing.text.MaskFormatter mask = new javax.swing.text.MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            JFormattedTextField f = new JFormattedTextField(mask);
            f.setColumns(10);
            f.putClientProperty("JTextField.placeholderText", "DD/MM/YYYY");
            return f;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private LocalDate parseDate(JFormattedTextField field, String label) {
        String txt = field.getText() == null ? "" : field.getText().trim();
        if (txt.contains("_") || txt.isEmpty()) {
            throw new IllegalArgumentException(label + " is required (format DD/MM/YYYY).");
        }
        return LocalDate.parse(txt, DATE_FMT);
    }
    
    private void loadProductData() {
        if (product == null) {
            String serialStr = JOptionPane.showInputDialog(this, "Enter Serial Number:");
            if (serialStr == null || serialStr.trim().isEmpty()) {
                dispose();
                return;
            }
            try {
                int serial = Integer.parseInt(serialStr.trim());
                product = productDAO.getProductBySerial(serial);
                if (product == null) {
                    JOptionPane.showMessageDialog(this, "Product not found!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid serial number!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        }
        
        // Load data into fields
        serialField.setText(String.valueOf(product.getSerial()));
        nameField.setText(product.getName());
        saltField.setText(product.getSalt());
        companyField.setText(product.getCompany());
        distributorField.setText(product.getDistributor());
        batchField.setText(product.getBatch());
        
        mfgDateField.setText(DATE_FMT.format(product.getMfgDate()));
        expDateField.setText(DATE_FMT.format(product.getExpDate()));

        priceSpinner.setValue(product.getPrice());
        quantitySpinner.setValue(product.getQuantity());
    }
    
    private void updateProduct() {
        try {
            if (product == null) return;
            
            String name = nameField.getText().trim();
            String salt = saltField.getText().trim();
            String company = companyField.getText().trim();
            String distributor = distributorField.getText().trim();
            String batch = batchField.getText().trim();
            
            if (name.isEmpty() || salt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in required fields (Name, Salt).",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate mfgDate = parseDate(mfgDateField, "MFG date");
            LocalDate expDate = parseDate(expDateField, "EXP date");
            
            if (!DateUtil.isValidDateRange(mfgDate, expDate)) {
                JOptionPane.showMessageDialog(this, "Invalid Expiry Date! Expiry must be after MFG date.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price = ((Number) priceSpinner.getValue()).doubleValue();
            int quantity = ((Number) quantitySpinner.getValue()).intValue();
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            product.setName(name);
            product.setSalt(salt);
            product.setCompany(company);
            product.setDistributor(distributor);
            product.setBatch(batch);
            product.setMfgDate(mfgDate);
            product.setExpDate(expDate);
            product.setPrice(price);
            product.setQuantity(quantity);
            
            if (productDAO.updateProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product Updated Successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
