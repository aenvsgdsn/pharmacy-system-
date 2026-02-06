import java.sql.*;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:pharmacy.db";
        
        try {
            System.out.println("Testing database connection...");
            Class.forName("org.sqlite.JDBC");
            
            Connection conn = DriverManager.getConnection(dbUrl);
            System.out.println("✓ Connected to database successfully!");
            
            // Check tables
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("\nTables in database:");
            while (tables.next()) {
                System.out.println("  - " + tables.getString("TABLE_NAME"));
            }
            
            // Check products table structure
            System.out.println("\nProducts table columns:");
            ResultSet columns = metaData.getColumns(null, null, "products", null);
            while (columns.next()) {
                System.out.println("  - " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
            }
            
            // Check existing products
            System.out.println("\nExisting products:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products");
            if (!rs.isBeforeFirst()) {
                System.out.println("  (No products in database)");
            } else {
                while (rs.next()) {
                    System.out.println("  Serial: " + rs.getInt("serial") + 
                                       ", Name: " + rs.getString("name") +
                                       ", Price: " + rs.getDouble("price") +
                                       ", Qty: " + rs.getInt("quantity"));
                }
            }
            
            // Check serial counter
            System.out.println("\nSerial Counter:");
            rs = stmt.executeQuery("SELECT counter FROM serial_counter WHERE id = 1");
            if (rs.next()) {
                System.out.println("  Current counter: " + rs.getInt("counter"));
            }
            
            conn.close();
            System.out.println("\n✓ All checks passed!");
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
