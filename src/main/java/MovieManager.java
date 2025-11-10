import java.util.ArrayList;
import java.util.List;
import java.sql.*; // Import all SQL classes needed
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MovieManager {
    // We no longer store the list in memory.
    // private List<MarvelMovie> movies; <-- REMOVED THIS

    // Keep the date bounds for server-side validation (though validation is mainly GUI-side)
    private final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    private final LocalDate MAX_DATE = LocalDate.of(2025, 12, 31);

    // SQL Query Constants
    private static final String TABLE_NAME = "movies";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    public MovieManager() {
        // The MovieManager no longer initializes an in-memory list.
        // It's ready to interact with the database set up by IntroScreen.main().
    }

    /**
     * READ: Fetches all movie records from the database.
     * @return A list of MarvelMovie objects retrieved from the database.
     */
    public List<MarvelMovie> getMovies() {
        List<MarvelMovie> movies = new ArrayList<>();
        Connection con = JDBC.openConnection();
        if (con == null) return movies; // Return empty list if connection fails

        // ORDER BY title for consistent display
        String sql = "SELECT title, releaseDate, phase, director, runningTimeMin, imdbRating FROM " + TABLE_NAME + " ORDER BY title ASC";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MarvelMovie movie = new MarvelMovie(
                        rs.getString("title"),
                        rs.getString("releaseDate"),
                        rs.getInt("phase"),
                        rs.getString("director"),
                        rs.getInt("runningTimeMin"),
                        rs.getDouble("imdbRating")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error retrieving all movies: " + e.getMessage());
            // In a production app, you might re-throw a custom exception here.
        } finally {
            JDBC.closeConnection(con);
        }
        return movies;
    }

    // ---------- CREATE (Using PreparedStatement for security and data integrity) ----------
    /**
     * Adds a new movie record to the database.
     * @return true if the movie was added, false on failure (e.g., SQL error, duplicate key).
     */
    public boolean addMovie(MarvelMovie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank()) return false;

        // Final server-side validation before sending to DB
        if (!isValidMovie(movie)) return false;

        // Note: The database should enforce uniqueness on the 'title' column.
        // If the title is unique, the INSERT should succeed.

        Connection con = JDBC.openConnection();
        if (con == null) return false;

        String sql = "INSERT INTO " + TABLE_NAME + " (title, releaseDate, phase, director, runningTimeMin, imdbRating) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getReleaseDate());
            pstmt.setInt(3, movie.getPhase());
            pstmt.setString(4, movie.getDirector());
            pstmt.setInt(5, movie.getRunningTimeMin());
            pstmt.setDouble(6, movie.getImdbRating());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Check for specific SQLite constraint violation (e.g., duplicate title)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Attempted to add duplicate movie: " + movie.getTitle());
                // Return false if a duplicate title prevented the add
                return false;
            }
            System.err.println("SQL Error during addMovie: " + e.getMessage());
            return false;
        } finally {
            JDBC.closeConnection(con);
        }
    }

    // Overloaded method to simplify calls from GUI
    public boolean addMovie(String title, String releaseDate, int phase, String director, int runningTimeMin, double imdbRating) {
        if (title == null || title.isBlank() || director == null || director.isBlank()) return false;
        MarvelMovie movie = new MarvelMovie(title.trim(), releaseDate.trim(), phase, director.trim(), runningTimeMin, imdbRating);
        return addMovie(movie);
    }

    // ---------- DELETE ----------
    /**
     * REMOVE: Deletes a movie record from the database based on its title.
     * @return true if a record was successfully deleted, false otherwise.
     */
    public boolean removeMovie(String title) {
        if (title == null || title.isBlank()) return false;

        Connection con = JDBC.openConnection();
        if (con == null) return false;

        String sql = "DELETE FROM " + TABLE_NAME + " WHERE title = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, title);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during removeMovie: " + e.getMessage());
            return false;
        } finally {
            JDBC.closeConnection(con);
        }
    }

    // ---------- FIND (READ ONE) ----------
    /**
     * READ: Searches the database for a movie by its title.
     * @return The MarvelMovie object if found, otherwise null.
     */
    public MarvelMovie findMovieByTitle(String title) {
        if (title == null || title.isBlank()) return null;

        Connection con = JDBC.openConnection();
        if (con == null) return null;

        String sql = "SELECT title, releaseDate, phase, director, runningTimeMin, imdbRating FROM " + TABLE_NAME + " WHERE title = ?";
        MarvelMovie movie = null;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, title);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movie = new MarvelMovie(
                            rs.getString("title"),
                            rs.getString("releaseDate"),
                            rs.getInt("phase"),
                            rs.getString("director"),
                            rs.getInt("runningTimeMin"),
                            rs.getDouble("imdbRating")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during findMovieByTitle: " + e.getMessage());
        } finally {
            JDBC.closeConnection(con);
        }
        return movie;
    }

    // ---------- UPDATE (Refactoring Required) ----------
    // Since the GUI logic (handleUpdate) first finds a movie, sets it to movieBeingEdited,
    // and then calls this method, we need to pass the *original* title for the WHERE clause.

    /**
     * UPDATE: Updates a single field for a movie record in the database.
     * The movie object passed here should contain the original title to find the record, and the new value to update.
     */
    public boolean updateMovieField(MarvelMovie movie, String field, Object value) {
        if (movie == null || field == null) return false;

        Connection con = JDBC.openConnection();
        if (con == null) return false;

        // This is the old title used to find the record in the database
        String originalTitle = movie.getTitle();
        String dbColumn;

        // 1. Validate input and map field name to database column
        try {
            switch (field.toLowerCase()) {
                case "title":
                    // Crucial: If updating the title, validate it's not a duplicate
                    // of an existing movie (other than itself). DB unique constraint will handle this too.
                    String t = (String) value;
                    if (t == null || t.isBlank()) return false;
                    dbColumn = "title";
                    break;
                case "releasedate":
                    if (!isValidDate((String) value)) return false;
                    dbColumn = "releaseDate";
                    break;
                case "phase":
                    if ((int) value <= 0) return false;
                    dbColumn = "phase";
                    break;
                case "director":
                    if ((String) value == null || ((String) value).isBlank()) return false;
                    dbColumn = "director";
                    break;
                case "runningtimemin":
                    if (!isValidRuntime((int) value)) return false;
                    dbColumn = "runningTimeMin";
                    break;
                case "imdbrating":
                    double r = (double) value;
                    if (r < 1.0 || r > 10.0) return false;
                    dbColumn = "imdbRating";
                    break;
                default:
                    return false;
            }
        } catch (ClassCastException | NullPointerException ex) {
            return false;
        }

        // 2. Execute the update
        String sql = "UPDATE " + TABLE_NAME + " SET " + dbColumn + " = ? WHERE title = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // Set the value (use switch to handle different types)
            if (value instanceof String) {
                pstmt.setString(1, (String) value);
            } else if (value instanceof Integer) {
                pstmt.setInt(1, (Integer) value);
            } else if (value instanceof Double) {
                pstmt.setDouble(1, (Double) value);
            } else {
                return false; // Type not supported
            }

            // The WHERE clause always uses the original title from the MovieManager's movieBeingEdited state
            pstmt.setString(2, originalTitle);

            int rowsAffected = pstmt.executeUpdate();

            // If the title was updated, we need to update the in-memory movie object's title
            // so subsequent updateMovieField calls use the new title in the WHERE clause.
            // Mirrors your original in-memory logic.
            if (dbColumn.equals("title") && rowsAffected > 0) {
                movie.setTitle((String) value);
            }

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error during updateMovieField: " + e.getMessage());
            return false;
        } finally {
            JDBC.closeConnection(con);
        }
    }

    // The updateMovieByTitle method can be removed as it's not used by the GUI update flow.
    // public boolean updateMovieByTitle(String title, String field, Object value) { ... }

    // ---------- CUSTOM ACTION (Aggregate Function) ----------
    /**
     * CUSTOM ACTION: Calculates the average IMDb rating for all movies in a specific phase.
     */
    public double calculateAverageRating(int phase) {
        if (phase <= 0) return 0.0;

        Connection con = JDBC.openConnection();
        if (con == null) return 0.0;

        // Use the SQL AVG aggregate function for efficiency
        String sql = "SELECT AVG(imdbRating) AS avg_rating FROM " + TABLE_NAME + " WHERE phase = ?";
        double average = 0.0;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, phase);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Use getDouble and check for SQL NULL (which returns 0.0 if there are no rows)
                    average = rs.getDouble("avg_rating");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error calculating average rating: " + e.getMessage());
        } finally {
            JDBC.closeConnection(con);
        }
        // If no movies were found in that phase, AVG() returns 0.0 (or null, which getDouble converts to 0.0)
        return average;
    }

    // ---------- BATCH LOAD (DISABLED/REMOVED) ----------
    public String loadBatchData(String filePath) {
        return "Batch Load is disabled in the database version. Use the 'ADD' function to insert movies.";
    }

    // ---------- VALIDATION HELPERS (KEPT FOR SERVER-SIDE CHECK) ----------

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return !date.isBefore(MIN_DATE) && !date.isAfter(MAX_DATE);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidRuntime(int runtime) {
        return runtime >= 30 && runtime <= 300;
    }

    private boolean isValidMovie(MarvelMovie movie) {
        if (movie.getPhase() <= 0) return false;
        if (!isValidRuntime(movie.getRunningTimeMin())) return false;
        if (movie.getImdbRating() < 1.0 || movie.getImdbRating() > 10.0) return false;
        return isValidDate(movie.getReleaseDate());
    }

    // ---------- UTILITY ----------
    /**
     * UTILITY: Deletes ALL records from the movie table.
     */
    public void clearAll() {
        Connection con = JDBC.openConnection();
        if (con == null) return;

        String sql = "DELETE FROM " + TABLE_NAME;

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("All movie records deleted from database.");
        } catch (SQLException e) {
            System.err.println("SQL Error during clearAll: " + e.getMessage());
        } finally {
            JDBC.closeConnection(con);
        }
    }
}