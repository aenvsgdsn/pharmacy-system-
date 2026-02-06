package com.pharmacy.dao;

import com.pharmacy.database.DatabaseManager;
import com.pharmacy.model.Product;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static class OperationResult {
        private final boolean success;
        private final String errorMessage;

        private OperationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static OperationResult ok() {
            return new OperationResult(true, null);
        }

        public static OperationResult error(String message) {
            return new OperationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    public int getNextSerial() {
        String selectSql = "SELECT counter FROM serial_counter WHERE id = 1";
        String updateSql = "UPDATE serial_counter SET counter = ? WHERE id = 1";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Use single connection for both select and update to avoid locking
            conn.setAutoCommit(false);
            
            // Select current counter
            int counter = 1000;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                if (rs.next()) {
                    counter = rs.getInt("counter");
                }
            }
            
            // Update counter atomically
            int nextSerial = counter + 1;
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, nextSerial);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            conn.setAutoCommit(true);
            return nextSerial;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1000;
    }
    
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (serial, name, salt, company, distributor, batch, purchase_date, mfg_date, exp_date, price, quantity) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, product.getSerial());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getSalt());
            pstmt.setString(4, product.getCompany());
            pstmt.setString(5, product.getDistributor());
            pstmt.setString(6, product.getBatch());
            pstmt.setString(7, product.getPurchaseDate().format(DATE_FORMATTER));
            pstmt.setString(8, product.getMfgDate() != null ? product.getMfgDate().format(DATE_FORMATTER) : null);
            pstmt.setString(9, product.getExpDate().format(DATE_FORMATTER));
            pstmt.setDouble(10, product.getPrice());
            pstmt.setInt(11, product.getQuantity());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public OperationResult addProductDetailed(Product product) {
        String sql = "INSERT INTO products (serial, name, salt, company, distributor, batch, purchase_date, mfg_date, exp_date, price, quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getSerial());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getSalt());
            pstmt.setString(4, product.getCompany());
            pstmt.setString(5, product.getDistributor());
            pstmt.setString(6, product.getBatch());
            pstmt.setString(7, product.getPurchaseDate().format(DATE_FORMATTER));
            pstmt.setString(8, product.getMfgDate() != null ? product.getMfgDate().format(DATE_FORMATTER) : null);
            pstmt.setString(9, product.getExpDate().format(DATE_FORMATTER));
            pstmt.setDouble(10, product.getPrice());
            pstmt.setInt(11, product.getQuantity());

            int rows = pstmt.executeUpdate();
            if (rows > 0) return OperationResult.ok();
            return OperationResult.error("Insert did not affect any rows.");
        } catch (SQLException e) {
            // Common SQLite errors we want to show clearly to the user
            String msg = e.getMessage() == null ? "Unknown database error." : e.getMessage();
            if (msg.toLowerCase().contains("unique constraint failed") && msg.toLowerCase().contains("products.serial")) {
                msg = "Serial number conflict (existing product has same serial). " +
                        "This is usually fixed by restarting the app (serial counter sync). Details: " + e.getMessage();
            }
            return OperationResult.error(msg);
        }
    }
    
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, salt = ?, company = ?, distributor = ?, " +
                     "batch = ?, purchase_date = ?, mfg_date = ?, exp_date = ?, price = ?, quantity = ? WHERE serial = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getSalt());
            pstmt.setString(3, product.getCompany());
            pstmt.setString(4, product.getDistributor());
            pstmt.setString(5, product.getBatch());
            pstmt.setString(6, product.getPurchaseDate().format(DATE_FORMATTER));
            pstmt.setString(7, product.getMfgDate() != null ? product.getMfgDate().format(DATE_FORMATTER) : null);
            pstmt.setString(8, product.getExpDate().format(DATE_FORMATTER));
            pstmt.setDouble(9, product.getPrice());
            pstmt.setInt(10, product.getQuantity());
            pstmt.setInt(11, product.getSerial());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Product getProductBySerial(int serial) {
        String sql = "SELECT * FROM products WHERE serial = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, serial);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY serial";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateQuantity(int serial, int newQuantity) {
        String sql = "UPDATE products SET quantity = ? WHERE serial = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, serial);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getDistinctCompanies() {
        return getDistinctNonEmptyValues("company");
    }

    public List<String> getDistinctDistributors() {
        return getDistinctNonEmptyValues("distributor");
    }

    private List<String> getDistinctNonEmptyValues(String column) {
        // Column name is controlled by code (not user input).
        String sql = "SELECT DISTINCT " + column + " AS v FROM products WHERE " + column + " IS NOT NULL AND TRIM(" + column + ") <> '' ORDER BY v";
        Set<String> values = new LinkedHashSet<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String v = rs.getString("v");
                if (v != null && !v.trim().isEmpty()) {
                    values.add(v.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(values);
    }
    
    public List<Product> findDuplicateProducts(String name, String batch) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name = ? AND batch = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, batch);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setSerial(rs.getInt("serial"));
        product.setName(rs.getString("name"));
        try {
            product.setSalt(rs.getString("salt"));
        } catch (SQLException ignored) {
            // Backward compatibility if column does not exist
            product.setSalt("");
        }
        product.setCompany(rs.getString("company"));
        product.setDistributor(rs.getString("distributor"));
        product.setBatch(rs.getString("batch"));
        product.setPurchaseDate(LocalDate.parse(rs.getString("purchase_date"), DATE_FORMATTER));
        try {
            String mfgDateStr = rs.getString("mfg_date");
            if (mfgDateStr != null && !mfgDateStr.isEmpty()) {
                product.setMfgDate(LocalDate.parse(mfgDateStr, DATE_FORMATTER));
            } else {
                product.setMfgDate(product.getPurchaseDate());
            }
        } catch (SQLException ignored) {
            // Backward compatibility if column does not exist
            product.setMfgDate(product.getPurchaseDate());
        }
        product.setExpDate(LocalDate.parse(rs.getString("exp_date"), DATE_FORMATTER));
        product.setPrice(rs.getDouble("price"));
        try {
            product.setQuantity(rs.getInt("quantity"));
        } catch (SQLException ignored) {
            // Backward compatibility if column does not exist for some reason.
            product.setQuantity(0);
        }
        return product;
    }
}
