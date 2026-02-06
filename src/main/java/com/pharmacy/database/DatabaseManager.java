package com.pharmacy.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Path DB_FILE = resolveDbFile();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath();
    private static DatabaseManager instance;
    private volatile String lastConnectionError;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection c = DriverManager.getConnection(DB_URL);
            lastConnectionError = null;
            return c;
        } catch (SQLException e) {
            lastConnectionError = e.getMessage();
            throw e;
        }
    }

    public String getDatabasePath() {
        return DB_FILE.toAbsolutePath().toString();
    }

    public String getLastConnectionError() {
        return lastConnectionError;
    }

    public boolean canConnect() {
        try (Connection c = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create products table
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    serial INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    salt TEXT,
                    company TEXT,
                    distributor TEXT,
                    batch TEXT,
                    purchase_date TEXT NOT NULL,
                    mfg_date TEXT,
                    exp_date TEXT NOT NULL,
                    price REAL NOT NULL,
                    quantity INTEGER NOT NULL DEFAULT 0
                )
            """;
            
            // Create sales table
            String createSalesTable = """
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_date TEXT NOT NULL,
                    product_serial INTEGER NOT NULL,
                    product_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    FOREIGN KEY (product_serial) REFERENCES products(serial)
                )
            """;
            
            // Create monthly_sales table
            String createMonthlySalesTable = """
                CREATE TABLE IF NOT EXISTS monthly_sales (
                    month INTEGER PRIMARY KEY,
                    amount REAL DEFAULT 0
                )
            """;
            
            // Create settings table for owner password
            String createSettingsTable = """
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT
                )
            """;
            
            // Create serial_counter table
            String createSerialCounterTable = """
                CREATE TABLE IF NOT EXISTS serial_counter (
                    id INTEGER PRIMARY KEY,
                    counter INTEGER DEFAULT 1000
                )
            """;
            
            stmt.execute(createProductsTable);
            stmt.execute(createSalesTable);
            stmt.execute(createMonthlySalesTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createSerialCounterTable);

            // Lightweight migration for older DBs (add missing columns)
            ensureProductsQuantityColumn(stmt);
            ensureProductsSaltColumn(stmt);
            ensureProductsMfgDateColumn(stmt);
            
            // Initialize serial counter if not exists
            stmt.execute("INSERT OR IGNORE INTO serial_counter (id, counter) VALUES (1, 1000)");
            
            // Initialize owner password if not exists
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('owner_password', 'owner123')");
            
            // Initialize monthly sales
            for (int i = 0; i < 12; i++) {
                stmt.execute(String.format(
                    "INSERT OR IGNORE INTO monthly_sales (month, amount) VALUES (%d, 0)", i));
            }

            // Ensure serial_counter is not behind existing products (prevents duplicate PK on insert)
            syncSerialCounterWithProducts(stmt);
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureProductsQuantityColumn(Statement stmt) throws SQLException {
        boolean hasQuantity = false;
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info('products')")) {
            while (rs.next()) {
                String col = rs.getString("name");
                if ("quantity".equalsIgnoreCase(col)) {
                    hasQuantity = true;
                    break;
                }
            }
        }

        if (!hasQuantity) {
            stmt.execute("ALTER TABLE products ADD COLUMN quantity INTEGER NOT NULL DEFAULT 0");
        }
    }

    private void ensureProductsSaltColumn(Statement stmt) throws SQLException {
        boolean hasSalt = false;
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info('products')")) {
            while (rs.next()) {
                String col = rs.getString("name");
                if ("salt".equalsIgnoreCase(col)) {
                    hasSalt = true;
                    break;
                }
            }
        }

        if (!hasSalt) {
            stmt.execute("ALTER TABLE products ADD COLUMN salt TEXT");
        }
    }

    private void ensureProductsMfgDateColumn(Statement stmt) throws SQLException {
        boolean hasMfgDate = false;
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info('products')")) {
            while (rs.next()) {
                String col = rs.getString("name");
                if ("mfg_date".equalsIgnoreCase(col)) {
                    hasMfgDate = true;
                    break;
                }
            }
        }

        if (!hasMfgDate) {
            stmt.execute("ALTER TABLE products ADD COLUMN mfg_date TEXT");
        }
    }

    private void syncSerialCounterWithProducts(Statement stmt) throws SQLException {
        int maxSerial = 0;
        try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(serial), 0) AS max_serial FROM products")) {
            if (rs.next()) maxSerial = rs.getInt("max_serial");
        }

        int counter = 1000;
        try (ResultSet rs = stmt.executeQuery("SELECT counter FROM serial_counter WHERE id = 1")) {
            if (rs.next()) counter = rs.getInt("counter");
        }

        if (maxSerial > counter) {
            stmt.execute("UPDATE serial_counter SET counter = " + maxSerial + " WHERE id = 1");
        }
    }

    private static Path resolveDbFile() {
        // If a DB exists in current working directory, use it (keeps existing data).
        Path cwdDb = Paths.get("pharmacy.db").toAbsolutePath();
        if (Files.exists(cwdDb)) return cwdDb;

        // Otherwise, store DB in a writable per-user folder (avoids permission issues).
        Path dir = Paths.get(System.getProperty("user.home"), ".pharmacy-management");
        try {
            Files.createDirectories(dir);
        } catch (Exception ignored) {
            // Last resort: use current dir
            return cwdDb;
        }
        return dir.resolve("pharmacy.db");
    }
}
