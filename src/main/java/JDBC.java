import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {

    // Private static variable to hold the database file path (URL) once set.
    private static String dbPath;

    // Private constructor to prevent instantiation, as this is a utility class
    private JDBC() {
        // No implementation needed
    }

    /**
     * Loads the SQLite JDBC driver and sets the database file path.
     * This method must be called once at application startup.
     * @param filePath The user-supplied absolute path to the SQLite database file.
     */
    public static void setDatabasePath(String filePath) {
        // 1. Load the SQLite JDBC Driver
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            // This is critical. The program cannot run without the driver.
            System.err.println("FATAL: SQLite JDBC Driver not found in library path.");
            e.printStackTrace();
            // Optional: Exit the application here if the driver is mandatory
        }

        // 2. Format the path into a proper JDBC connection string
        JDBC.dbPath = "jdbc:sqlite:" + filePath;
        System.out.println("Database URL set to: " + JDBC.dbPath);
    }

    /**
     * Opens and returns a new connection to the database.
     * @return A valid Connection object, or null if the connection fails.
     */
    public static Connection openConnection() {
        if (dbPath == null) {
            // Path must be set first. This is a check against programmer error.
            System.err.println("ERROR: Database path has not been set. Call setDatabasePath() first.");
            return null;
        }

        Connection connection = null;
        try {
            // DriverManager will use the loaded driver to establish the connection
            connection = DriverManager.getConnection(dbPath);
            return connection;
        } catch (SQLException e) {
            // Catch specific errors related to connection (e.g., file not found, corrupt DB)
            System.err.println("Connection Failed: Could not connect to database at " + dbPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the provided database connection.
     * @param connection The Connection object to be closed.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection.");
                e.printStackTrace();
            }
        }
    }
}