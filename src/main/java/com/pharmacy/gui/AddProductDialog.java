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

public class AddProductDialog extends JDialog {
    private JTextField nameField, companyField, distributorField, batchField;
    private JFormattedTextField purchaseDateField;
    private JFormattedTextField expDateField;
    private JSpinner priceSpinner;
    private JSpinner quantitySpinner;
    private ProductDAO productDAO;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    
    public AddProductDialog(JFrame parent) {
        super(parent, "Add Product", true);
        productDAO = new ProductDAO();
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(520, 650);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel hint = new JLabel("Required: Name, Purchase Date, EXP Date, Price");
        hint.setForeground(new Color(90, 90, 90));
        mainPanel.add(hint, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Company
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        companyField = new JTextField(20);
        formPanel.add(companyField, gbc);
        
        // Distributor
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Distributor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        distributorField = new JTextField(20);
        formPanel.add(distributorField, gbc);
        
        // Batch No
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Batch No:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        batchField = new JTextField(20);
        formPanel.add(batchField, gbc);
        
        // Purchase Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Purchase Date (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        purchaseDateField = createDateField();
        formPanel.add(purchaseDateField, gbc);
        
        // EXP Date
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("EXP Date (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        expDateField = createDateField();
        formPanel.add(expDateField, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Price (Rs):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 1.0));
        ((JSpinner.NumberEditor) priceSpinner.getEditor()).getTextField().setColumns(10);
        formPanel.add(priceSpinner, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantity (units):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        ((JSpinner.NumberEditor) quantitySpinner.getEditor()).getTextField().setColumns(10);
        formPanel.add(quantitySpinner, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Product");
        JButton cancelButton = new JButton("Cancel");
        
        addButton.addActionListener(e -> addProduct());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        getRootPane().setDefaultButton(addButton);
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
            // Fallback
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
    
    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String company = companyField.getText().trim();
            String distributor = distributorField.getText().trim();
            String batch = batchField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in required fields (Name).",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate purchaseDate = parseDate(purchaseDateField, "Purchase date");
            LocalDate expDate = parseDate(expDateField, "EXP date");
            
            if (!DateUtil.isValidDateRange(purchaseDate, expDate)) {
                JOptionPane.showMessageDialog(this, "Invalid Expiry Date! Expiry must be after Purchase date.", 
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
            
            // Check for duplicates
            var duplicates = productDAO.findDuplicateProducts(name, batch);
            if (!duplicates.isEmpty()) {
                int option = JOptionPane.showConfirmDialog(this, 
                    "Same product exists. Edit existing product?", 
                    "Duplicate Found", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                    new EditProductDialog((JFrame) getParent(), duplicates.get(0)).setVisible(true);
                    return;
                }
            }
            
            Product product = new Product();
            product.setSerial(productDAO.getNextSerial());
            product.setName(name);
            product.setCompany(company);
            product.setDistributor(distributor);
            product.setBatch(batch);
            product.setPurchaseDate(purchaseDate);
            product.setExpDate(expDate);
            product.setPrice(price);
            product.setQuantity(quantity);
            
            var res = productDAO.addProductDetailed(product);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, 
                    "Product Added Successfully!\nSerial: " + product.getSerial(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to add product!\n\n" + res.getErrorMessage(),
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
