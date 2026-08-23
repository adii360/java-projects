import java.sql.*;
public class App {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/college",
                "root",
                "123"
            );

            String sql = "SELECT * FROM student";

PreparedStatement ps = con.prepareStatement(sql); // insert, update
ResultSet rs = ps.executeQuery();

System.out.println("Student records:");

while (rs.next()) {
    System.out.println(
        rs.getInt(1) + " " +
        rs.getString(2) + " " +
        rs.getString(3)
    );
}
     con.close();
      } catch (Exception e) {
            e.printStackTrace();
             }
    }
}