import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBC {
    public static void main(String[] args) {
        // SQLite connection string
        String url = "jdbc:sqlite:C:/sqlite/Myprojects/myproject.db";
        String query = "SELECT * FROM movies";

        try {
            // Connect to SQLite database
            Connection con = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");

            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery(query);

            // Print all data from the movies table
            while (result.next()) {
                String movieData = "";
                // 7 columns in your table
                for (int i = 1; i <= 7; i++) {
                    movieData += result.getString(i) + " : ";
                }
                System.out.println(movieData);
            }

            // Close the connection
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
