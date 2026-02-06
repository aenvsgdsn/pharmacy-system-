package com.pharmacy.dao;

import com.pharmacy.database.DatabaseManager;
import com.pharmacy.model.Sale;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaleDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static class TodaySalesSummary {
        private final int salesCount;
        private final double revenue;

        public TodaySalesSummary(int salesCount, double revenue) {
            this.salesCount = salesCount;
            this.revenue = revenue;
        }

        public int getSalesCount() {
            return salesCount;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static class TopSellingProduct {
        private final String productName;
        private final int quantitySold;

        public TopSellingProduct(String productName, int quantitySold) {
            this.productName = productName;
            this.quantitySold = quantitySold;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantitySold() {
            return quantitySold;
        }
    }

    public static class LowStockWarning {
        private final int productSerial;
        private final String productName;
        private final int remainingQuantity;

        public LowStockWarning(int productSerial, String productName, int remainingQuantity) {
            this.productSerial = productSerial;
            this.productName = productName;
            this.remainingQuantity = remainingQuantity;
        }

        public int getProductSerial() {
            return productSerial;
        }

        public String getProductName() {
            return productName;
        }

        public int getRemainingQuantity() {
            return remainingQuantity;
        }
    }

    public static class BillRecordResult {
        private final boolean success;
        private final String errorMessage;
        private final List<LowStockWarning> lowStockWarnings;

        private BillRecordResult(boolean success, String errorMessage, List<LowStockWarning> lowStockWarnings) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.lowStockWarnings = lowStockWarnings == null ? Collections.emptyList() : lowStockWarnings;
        }

        public static BillRecordResult ok(List<LowStockWarning> warnings) {
            return new BillRecordResult(true, null, warnings);
        }

        public static BillRecordResult error(String message) {
            return new BillRecordResult(false, message, Collections.emptyList());
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<LowStockWarning> getLowStockWarnings() {
            return lowStockWarnings;
        }
    }
    
    public boolean addSale(Sale sale) {
        String sql = "INSERT INTO sales (sale_date, product_serial, product_name, quantity, amount) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sale.getSaleDate().format(DATE_FORMATTER));
            pstmt.setInt(2, sale.getProductSerial());
            pstmt.setString(3, sale.getProductName());
            pstmt.setInt(4, sale.getQuantity());
            pstmt.setDouble(5, sale.getAmount());
            
            boolean result = pstmt.executeUpdate() > 0;
            
            if (result) {
                updateMonthlySales(sale.getSaleDate().getMonthValue() - 1, sale.getAmount());
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void updateMonthlySales(int month, double amount) {
        String sql = "UPDATE monthly_sales SET amount = amount + ? WHERE month = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, month);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC, id DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sale sale = new Sale();
                sale.setId(rs.getInt("id"));
                sale.setSaleDate(LocalDate.parse(rs.getString("sale_date"), DATE_FORMATTER));
                sale.setProductSerial(rs.getInt("product_serial"));
                sale.setProductName(rs.getString("product_name"));
                sale.setQuantity(rs.getInt("quantity"));
                sale.setAmount(rs.getDouble("amount"));
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    public double[] getMonthlySales() {
        double[] monthlySales = new double[12];
        String sql = "SELECT month, amount FROM monthly_sales ORDER BY month";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int month = rs.getInt("month");
                monthlySales[month] = rs.getDouble("amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlySales;
    }

    public BillRecordResult recordBill(LocalDate saleDate, List<Sale> salesLines) {
        if (salesLines == null || salesLines.isEmpty()) {
            return BillRecordResult.error("Bill is empty.");
        }

        String selectQtySql = "SELECT quantity FROM products WHERE serial = ?";
        String updateQtySql = "UPDATE products SET quantity = ? WHERE serial = ?";
        String insertSaleSql = "INSERT INTO sales (sale_date, product_serial, product_name, quantity, amount) VALUES (?, ?, ?, ?, ?)";
        String updateMonthlySql = "UPDATE monthly_sales SET amount = amount + ? WHERE month = ?";

        List<LowStockWarning> warnings = new ArrayList<>();
        int monthIndex = saleDate.getMonthValue() - 1;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectQty = conn.prepareStatement(selectQtySql);
                 PreparedStatement updateQty = conn.prepareStatement(updateQtySql);
                 PreparedStatement insertSale = conn.prepareStatement(insertSaleSql);
                 PreparedStatement updateMonthly = conn.prepareStatement(updateMonthlySql)) {

                for (Sale line : salesLines) {
                    if (line.getQuantity() <= 0) {
                        conn.rollback();
                        return BillRecordResult.error("Invalid quantity for " + line.getProductName());
                    }

                    // Check available stock
                    selectQty.setInt(1, line.getProductSerial());
                    try (ResultSet rs = selectQty.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return BillRecordResult.error("Product not found (S#" + line.getProductSerial() + ")");
                        }
                        int currentQty = rs.getInt("quantity");
                        if (currentQty <= 0) {
                            conn.rollback();
                            return BillRecordResult.error("Out of stock: " + line.getProductName() + " (S#" + line.getProductSerial() + ")");
                        }
                        if (currentQty < line.getQuantity()) {
                            conn.rollback();
                            return BillRecordResult.error("Not enough stock for " + line.getProductName()
                                    + " (S#" + line.getProductSerial() + "). Available: " + currentQty);
                        }

                        int newQty = currentQty - line.getQuantity();

                        // Reduce stock
                        updateQty.setInt(1, newQty);
                        updateQty.setInt(2, line.getProductSerial());
                        updateQty.executeUpdate();

                        // Insert sale
                        insertSale.setString(1, saleDate.format(DATE_FORMATTER));
                        insertSale.setInt(2, line.getProductSerial());
                        insertSale.setString(3, line.getProductName());
                        insertSale.setInt(4, line.getQuantity());
                        insertSale.setDouble(5, line.getAmount());
                        insertSale.executeUpdate();

                        // Update monthly sales
                        updateMonthly.setDouble(1, line.getAmount());
                        updateMonthly.setInt(2, monthIndex);
                        updateMonthly.executeUpdate();

                        // Low stock warning (< 5 but not zero)
                        if (newQty > 0 && newQty < 5) {
                            warnings.add(new LowStockWarning(line.getProductSerial(), line.getProductName(), newQty));
                        }
                    }
                }

                conn.commit();
                return BillRecordResult.ok(warnings);
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return BillRecordResult.error("Failed to record bill: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return BillRecordResult.error("Failed to record bill: " + e.getMessage());
        }
    }

    public TodaySalesSummary getTodaySalesSummary(LocalDate date) {
        String sql = "SELECT COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS revenue FROM sales WHERE sale_date = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.format(DATE_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new TodaySalesSummary(rs.getInt("cnt"), rs.getDouble("revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new TodaySalesSummary(0, 0);
    }

    public List<TopSellingProduct> getTopSellingProducts(int limit) {
        List<TopSellingProduct> result = new ArrayList<>();
        String sql = "SELECT product_name, SUM(quantity) AS qty_sold FROM sales GROUP BY product_name ORDER BY qty_sold DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Math.max(1, limit));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new TopSellingProduct(rs.getString("product_name"), rs.getInt("qty_sold")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
