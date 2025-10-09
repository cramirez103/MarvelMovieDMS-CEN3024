/**
 * MarvelMovie.java
 * Represents a single Marvel Cinematic Universe (MCU) movie object.
 * This class fulfills the requirement for storing at least 6 pieces of data of various types.
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
     * Constructor for creating a new MarvelMovie object.
     * @param title Title of the movie.
     * @param releaseDate Release date (YYYY-MM-DD).
     * @param phase MCU phase number.
     * @param director Director's name.
     * @param runningTimeMin Running time in minutes.
     * @param imdbRating IMDb rating.
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

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getPhase() {
        return phase;
    }

    public String getDirector() {
        return director;
    }

    public int getRunningTimeMin() {
        return runningTimeMin;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    // --- Setter Methods (Used by the Update operation) ---

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setRunningTimeMin(int runningTimeMin) {
        this.runningTimeMin = runningTimeMin;
    }

    public void setImdbRating(double imdbRating) {
        this.imdbRating = imdbRating;
    }

    // --- Utility Method ---

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