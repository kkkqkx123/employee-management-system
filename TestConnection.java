import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/employee_management";
        String username = "employee_admin";
        String password = "dev_password123";
        
        try {
            System.out.println("尝试连接数据库...");
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("连接成功！");
            conn.close();
        } catch (SQLException e) {
            System.err.println("连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}