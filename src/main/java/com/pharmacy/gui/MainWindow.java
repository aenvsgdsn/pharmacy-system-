package com.pharmacy.gui;

import com.pharmacy.database.DatabaseManager;
import com.pharmacy.gui.panels.DashboardPanel;
import com.pharmacy.gui.panels.ProductsPanel;
import com.pharmacy.gui.panels.SalesPanel;
import com.pharmacy.gui.panels.SettingsPanel;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    
    public MainWindow() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Pharmacy Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        DashboardPanel dashboardPanel = new DashboardPanel();
        ProductsPanel productsPanel = new ProductsPanel(this);
        SalesPanel salesPanel = new SalesPanel(this);
        SettingsPanel settingsPanel = new SettingsPanel(this);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Products", productsPanel);
        tabs.addTab("Sales", salesPanel);
        tabs.addTab("Settings", settingsPanel);

        // Live refresh when switching tabs
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == dashboardPanel) {
                dashboardPanel.refresh();
            } else if (selected == productsPanel) {
                productsPanel.refreshData();
            } else if (selected == settingsPanel) {
                settingsPanel.loadSettings();
            }

            // Update DB status on tab changes too
            updateDbStatus();
        });

        add(tabs, BorderLayout.CENTER);

        // Status bar (shows DB path + connection status)
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JLabel dbStatus = new JLabel();
        dbStatus.setName("dbStatusLabel");
        statusBar.add(dbStatus, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);

        updateDbStatus();
    }

    private void updateDbStatus() {
        JLabel label = findDbStatusLabel();
        if (label == null) return;

        DatabaseManager db = DatabaseManager.getInstance();
        boolean ok = db.canConnect();
        if (ok) {
            label.setText("Database: " + db.getDatabasePath() + " | Status: Connected");
            label.setForeground(new Color(0, 110, 0));
        } else {
            String err = db.getLastConnectionError();
            label.setText("Database: " + db.getDatabasePath() + " | Status: ERROR" + (err == null ? "" : " - " + err));
            label.setForeground(new Color(180, 0, 0));
        }
    }

    private JLabel findDbStatusLabel() {
        // Look for the named label in the SOUTH status bar.
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component c2 : p.getComponents()) {
                    if (c2 instanceof JLabel) {
                        JLabel l = (JLabel) c2;
                        if ("dbStatusLabel".equals(l.getName())) return l;
                    }
                }
            }
        }
        return null;
    }
}
