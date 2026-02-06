package com.pharmacy.gui.panels;

import com.pharmacy.dao.SettingsDAO;
import com.pharmacy.gui.ChangePasswordDialog;
import com.pharmacy.gui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final JFrame parent;
    private final SettingsDAO settingsDAO;

    private JCheckBox darkModeCheck;

    public SettingsPanel(JFrame parent) {
        this.parent = parent;
        this.settingsDAO = new SettingsDAO();
        initUI();
        loadSettings();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Settings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        darkModeCheck = new JCheckBox("Dark mode");
        darkModeCheck.addActionListener(e -> {
            boolean dark = darkModeCheck.isSelected();
            settingsDAO.setUiTheme(dark ? "dark" : "light");
            ThemeManager.applyTheme(dark ? "dark" : "light");
            ThemeManager.refreshAllWindows();
        });
        themePanel.add(darkModeCheck);
        content.add(themePanel);

        JPanel securityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        securityPanel.setBorder(BorderFactory.createTitledBorder("Security"));
        JButton changePassBtn = new JButton("Change owner password");
        changePassBtn.addActionListener(e -> new ChangePasswordDialog(parent).setVisible(true));
        securityPanel.add(changePassBtn);
        content.add(securityPanel);

        add(content, BorderLayout.CENTER);
    }

    public void loadSettings() {
        String theme = settingsDAO.getUiTheme();
        darkModeCheck.setSelected("dark".equalsIgnoreCase(theme));
    }
}

