import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for managing the connection to the SQLite database.
 * This class handles driver loading, creating the JDBC connection string, and providing
 * reusable methods for opening and safely closing database connections.
 *
 * <p>Role in System: Provides the essential database access layer, used exclusively by the MovieManager.</p>
 *
 * @author [Ramirez,Christopher]
 * @version 1.0
 */
public class JDBC {

    // Private static variable to hold the database file path (URL) once set.
    private static String dbPath;

    /**
     * Private constructor to prevent instantiation, as this is a utility class
     * containing only static methods.
     */
    private JDBC() {
        // No implementation needed
    }

    /**
     * Loads the SQLite JDBC driver and sets the database file path.
     * This method must be called once at application startup to configure the database URL.
     * @param filePath The user-supplied absolute path to the SQLite database file.
     * @throws ClassNotFoundException If the {@code org.sqlite.JDBC} driver is not found in the classpath.
     */
    public static void setDatabasePath(String filePath) throws ClassNotFoundException {
        // 1. Load the SQLite JDBC Driver
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            // Re-throw or handle the exception to alert the calling method (IntroScreen)
            System.err.println("FATAL: SQLite JDBC Driver not found in library path.");
            e.printStackTrace();
            throw e; // Re-throw the exception so IntroScreen can handle the failure.
        }

        // 2. Format the path into a proper JDBC connection string
        JDBC.dbPath = "jdbc:sqlite:" + filePath;
        System.out.println("Database URL set to: " + JDBC.dbPath);
    }

    /**
     * Opens and returns a new {@link java.sql.Connection} to the database.
     * The database path must have been successfully set using {@link #setDatabasePath(String)}.
     * @return A valid {@code Connection} object, or {@code null} if the connection fails due to a locked, missing, or corrupt database file.
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
     * Closes the provided database connection safely, suppressing any {@link java.sql.SQLException}.
     * @param connection The {@code Connection} object to be closed.
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