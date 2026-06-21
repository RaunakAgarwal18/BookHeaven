import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckAdmin {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library_db", "postgres", "root");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT email FROM users WHERE role = 'ADMIN'");
            while (rs.next()) {
                System.out.println("Admin email: " + rs.getString("email"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
