import java.sql.*;

public class FixDatabase {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:pharmacy.db";
        
        try {
            System.out.println("Fixing database schema...");
            Class.forName("org.sqlite.JDBC");
            
            Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            
            // Check if quantity column exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "products", "quantity");
            
            if (!columns.next()) {
                System.out.println("Adding missing 'quantity' column to products table...");
                stmt.execute("ALTER TABLE products ADD COLUMN quantity INTEGER NOT NULL DEFAULT 0");
                System.out.println("✓ Column added successfully!");
            } else {
                System.out.println("✓ Quantity column already exists.");
            }
            
            // Verify the fix
            System.out.println("\nProducts table columns after fix:");
            columns = metaData.getColumns(null, null, "products", null);
            while (columns.next()) {
                System.out.println("  - " + columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
            }
            
            conn.close();
            System.out.println("\n✓ Database fixed!");
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
