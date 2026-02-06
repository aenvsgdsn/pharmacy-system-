package com.pharmacy.gui;

import com.pharmacy.dao.SettingsDAO;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private SettingsDAO settingsDAO;
    
    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Owner Password", true);
        settingsDAO = new SettingsDAO();
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        currentPasswordField = new JPasswordField(20);
        formPanel.add(currentPasswordField, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton changeButton = new JButton("Change Password");
        JButton cancelButton = new JButton("Cancel");
        
        changeButton.addActionListener(e -> changePassword());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (!settingsDAO.verifyPassword(currentPassword)) {
            JOptionPane.showMessageDialog(this, "Wrong current password!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New password cannot be empty!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (settingsDAO.updateOwnerPassword(newPassword)) {
            JOptionPane.showMessageDialog(this, "Password Changed Successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to change password!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
