package com.pharmacy.dao;

import com.pharmacy.database.DatabaseManager;

import java.sql.*;

public class SettingsDAO {
    public static final String KEY_OWNER_PASSWORD = "owner_password";
    public static final String KEY_UI_THEME = "ui_theme";
    
    public String getOwnerPassword() {
        return getSetting(KEY_OWNER_PASSWORD, "owner123");
    }
    
    public boolean updateOwnerPassword(String newPassword) {
        return setSetting(KEY_OWNER_PASSWORD, newPassword);
    }
    
    public boolean verifyPassword(String password) {
        return password.equals(getOwnerPassword());
    }

    public String getUiTheme() {
        return getSetting(KEY_UI_THEME, "light");
    }

    public boolean setUiTheme(String theme) {
        if (theme == null || theme.trim().isEmpty()) theme = "light";
        theme = theme.trim().toLowerCase();
        if (!theme.equals("light") && !theme.equals("dark")) theme = "light";
        return setSetting(KEY_UI_THEME, theme);
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public boolean setSetting(String key, String value) {
        String sql = "INSERT INTO settings (key, value) VALUES (?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
