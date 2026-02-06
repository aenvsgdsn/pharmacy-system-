import java.sql.*;

public class ResetSerialCounter {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:pharmacy.db";
        
        try {
            System.out.println("Resetting serial counter...");
            Class.forName("org.sqlite.JDBC");
            
            Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            
            // Get max serial from products table
            ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(serial), 1000) AS max_serial FROM products");
            int maxSerial = 1000;
            if (rs.next()) {
                maxSerial = rs.getInt("max_serial");
            }
            
            System.out.println("Max serial in products table: " + maxSerial);
            
            // Reset counter to max_serial (so next product gets max_serial + 1)
            stmt.execute("UPDATE serial_counter SET counter = " + maxSerial + " WHERE id = 1");
            
            // Verify
            rs = stmt.executeQuery("SELECT counter FROM serial_counter WHERE id = 1");
            if (rs.next()) {
                System.out.println("New serial counter: " + rs.getInt("counter"));
            }
            
            System.out.println("Next product will get serial: " + (maxSerial + 1));
            conn.close();
            System.out.println("✓ Serial counter reset successfully!");
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
