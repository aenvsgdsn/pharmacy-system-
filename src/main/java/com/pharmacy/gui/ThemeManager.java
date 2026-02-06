package com.pharmacy.gui;

import javax.swing.*;
import java.awt.*;

public final class ThemeManager {
    private ThemeManager() {}

    public static void applyTheme(String theme) {
        String t = theme == null ? "light" : theme.trim().toLowerCase();
        boolean dark = "dark".equals(t);

        try {
            if (dark) {
                // FlatLaf (preferred)
                com.formdev.flatlaf.FlatDarkLaf.setup();
            } else {
                com.formdev.flatlaf.FlatLightLaf.setup();
            }
            return;
        } catch (Throwable ignored) {
            // FlatLaf not available -> fall back
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    public static void refreshAllWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.invalidate();
            w.validate();
            w.repaint();
        }
    }
}

