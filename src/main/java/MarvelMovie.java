/**
 * Represents the data model for a Marvel Cinematic Universe (MCU) movie.
 * This class stores all relevant attributes of a single movie record (title, date, phase, director, runtime, rating).
 *
 * <p>Role in System: Acts as the Plain Old Java Object (POJO) used to transfer data between the
 * GUI layer and the MovieManager (database controller) layer.</p>
 *
 * @author [Ramirez,Christopher]
 * @version 1.0
 */
public class MarvelMovie {
    // Attributes (private for encapsulation, following UML)
    private String title;           // 1. String: Unique identifier
    private String releaseDate;     // 2. String: Release date (YYYY-MM-DD)
    private int phase;              // 3. int: MCU phase number
    private String director;        // 4. String: Director's name
    private int runningTimeMin;     // 5. int: Running time in minutes
    private double imdbRating;      // 6. double: IMDb rating

    /**
     * Constructs a new MarvelMovie object, initializing all six required attributes.
     * @param title The official title of the movie (used as the unique identifier in the database).
     * @param releaseDate The release date in "YYYY-MM-DD" format.
     * @param phase The MCU phase number the movie belongs to.
     * @param director The director's name.
     * @param runningTimeMin The runtime of the movie in minutes.
     * @param imdbRating The IMDb rating of the movie (1.0-10.0).
     */
    public MarvelMovie(String title, String releaseDate, int phase, String director, int runningTimeMin, double imdbRating) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.phase = phase;
        this.director = director;
        this.runningTimeMin = runningTimeMin;
        this.imdbRating = imdbRating;
    }

    // --- Getter Methods ---

    /**
     * Retrieves the title of the movie.
     * @return The movie's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the release date of the movie.
     * @return The release date in "YYYY-MM-DD" format.
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Retrieves the MCU phase the movie belongs to.
     * @return The phase number.
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Retrieves the director of the movie.
     * @return The director's name.
     */
    public String getDirector() {
        return director;
    }

    /**
     * Retrieves the running time of the movie.
     * @return The running time in minutes.
     */
    public int getRunningTimeMin() {
        return runningTimeMin;
    }

    /**
     * Retrieves the IMDb rating of the movie.
     * @return The IMDb rating as a double.
     */
    public double getImdbRating() {
        return imdbRating;
    }

    // --- Setter Methods (Used by the Update operation) ---

    /**
     * Sets a new title for the movie.
     * @param title The new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets a new release date for the movie.
     * @param releaseDate The new release date in "YYYY-MM-DD" format.
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Sets a new MCU phase for the movie.
     * @param phase The new phase number.
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Sets a new director for the movie.
     * @param director The new director's name.
     */
    public void setDirector(String director) {
        this.director = director;
    }

    /**
     * Sets a new running time for the movie.
     * @param runningTimeMin The new running time in minutes.
     */
    public void setRunningTimeMin(int runningTimeMin) {
        this.runningTimeMin = runningTimeMin;
    }

    /**
     * Sets a new IMDb rating for the movie.
     * @param imdbRating The new IMDb rating.
     */
    public void setImdbRating(double imdbRating) {
        this.imdbRating = imdbRating;
    }

    // --- Utility Method ---

    /**
     * Provides a formatted string representation of the MarvelMovie object.
     * This is typically used for console or debugging output.
     * @return A multi-line string containing the movie's title, phase, rating, director, release date, and runtime.
     */
    @Override
    public String toString() {
        // Formats the movie data for easy display in the CLI
        return String.format(
                "| Title: %-30s | Phase: %-2d | Rating: %.1f\n" +
                        "| Director: %-26s | Released: %-10s | Runtime: %-3d min",
                title, phase, imdbRating, director, releaseDate, runningTimeMin
        );
    }
}