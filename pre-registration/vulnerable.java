import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.MessageDigest;
import java.util.Base64;

public class VulnerableCode {
    public static void main(String[] args) {
        // Simulated malicious input for SQL Injection
        String userInput = "admin' OR '1'='1";

        try {
            // Vulnerable SQL Query
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "root", "password");
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
            ResultSet resultSet = statement.executeQuery(query);

            // Print the results
            while (resultSet.next()) {
                System.out.println("User: " + resultSet.getString("username"));
            }

            // Insecure Cryptography Example: MD5 for hashing passwords
            String password = "supersecretpassword";
            MessageDigest md = MessageDigest.getInstance("MD5"); // MD5 is cryptographically broken
            byte[] hash = md.digest(password.getBytes());
            System.out.println("MD5 Hash of password: " + Base64.getEncoder().encodeToString(hash));

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
