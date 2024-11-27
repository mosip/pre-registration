import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class VulnerableApp {
    public static void main(String[] args) {
        String userInput = "test'; DROP TABLE users; --"; // Simulated malicious input

        try {
            // Connect to the database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "root", "password");

            // Vulnerable: User input directly concatenated into SQL query
            String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Print the results
            while (resultSet.next()) {
                System.out.println("User: " + resultSet.getString("username"));
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
