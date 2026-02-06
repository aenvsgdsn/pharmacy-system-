package com.pharmacy;

import com.pharmacy.dao.SettingsDAO;
import com.pharmacy.gui.MainWindow;
import com.pharmacy.gui.ThemeManager;

import javax.swing.*;

public class PharmacyManagementSystem {
    public static void main(String[] args) {
        // Apply persisted theme before creating any UI
        SettingsDAO settingsDAO = new SettingsDAO();
        ThemeManager.applyTheme(settingsDAO.getUiTheme());
        
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
