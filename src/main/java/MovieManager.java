import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * MovieManager.java
 * Manages the collection of MarvelMovie objects and implements all CRUD operations,
 * input validation, and the custom action. Refactored to expose methods that accept
 * parameters and return results for easier unit testing.
 */
public class MovieManager {
    private List<MarvelMovie> movies;

    public MovieManager() {
        this.movies = new ArrayList<>();
    }

    // --- R: Read/Display Operation ---
    public List<MarvelMovie> getMovies() {
        return new ArrayList<>(this.movies);
    }

    // --- C: Create Data (object) ---
    /**
     * Adds a MarvelMovie object if title is unique.
     * @param movie the MarvelMovie object
     * @return true if added, false if duplicate or invalid
     */
    public boolean addMovie(MarvelMovie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank()) {
            return false;
        }
        if (findMovieByTitle(movie.getTitle()) != null) {
            return false;
        }
        this.movies.add(movie);
        return true;
    }

    // --- C: Create Data (params) - easier for tests ---
    public boolean addMovie(String title, String releaseDate, int phase, String director, int runningTimeMin, double imdbRating) {
        if (title == null || title.isBlank()) return false;
        if (director == null || director.isBlank()) return false;
        if (!releaseDate.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        if (phase <= 0 || runningTimeMin <= 0 || imdbRating < 1.0 || imdbRating > 10.0) return false;

        MarvelMovie movie = new MarvelMovie(title.trim(), releaseDate.trim(), phase, director.trim(), runningTimeMin, imdbRating);
        return addMovie(movie);
    }

    // --- C: Create Data (Batch/Text File) ---
    public String loadBatchData(String filePath) {
        int addedCount = 0;
        int failedCount = 0;

        try (BufferedReader br = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 6) {
                    System.out.println("-> Skipping line (Incorrect field count): " + line);
                    failedCount++;
                    continue;
                }

                try {
                    String title = parts[0].trim();
                    String date = parts[1].trim();
                    int phase = Integer.parseInt(parts[2].trim());
                    String director = parts[3].trim();
                    int runtime = Integer.parseInt(parts[4].trim());
                    double rating = Double.parseDouble(parts[5].trim());

                    if (phase <= 0 || runtime <= 0 || rating < 1.0 || rating > 10.0 || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        System.out.println("-> Skipping line (Invalid value range or format): " + line);
                        failedCount++;
                        continue;
                    }

                    MarvelMovie movie = new MarvelMovie(title, date, phase, director, runtime, rating);
                    if (addMovie(movie)) {
                        addedCount++;
                    } else {
                        System.out.println("-> Skipping line (Duplicate title found): " + title);
                        failedCount++;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("-> Skipping line (Type error in batch data): " + line);
                    failedCount++;
                }
            }

        } catch (IOException e) {
            return "ERROR: Unable to read file. Please check the provided path.";
        } catch (Exception e) {
            return "ERROR: An unexpected error occurred during batch load.";
        }

        return String.format("Batch Load Complete: %d added, %d failed.", addedCount, failedCount);
    }

    // --- D: Remove Data ---
    /**
     * Remove movie by title.
     * @param title title to remove
     * @return true if removed, false otherwise
     */
    public boolean removeMovie(String title) {
        MarvelMovie movieToRemove = findMovieByTitle(title);
        if (movieToRemove != null) {
            this.movies.remove(movieToRemove);
            return true;
        }
        return false;
    }

    // --- U: Update Data ---
    public MarvelMovie findMovieByTitle(String title) {
        if (title == null) return null;
        String searchTitle = title.trim().toLowerCase();
        for (MarvelMovie movie : this.movies) {
            if (movie.getTitle().toLowerCase().equals(searchTitle)) {
                return movie;
            }
        }
        return null;
    }

    /**
     * Updates a field on a given MarvelMovie. Returns true if update successful.
     * @param movie movie object
     * @param field field name (title, releaseDate, phase, director, runningTimeMin, imdbRating)
     * @param value new value (type must match)
     * @return true if updated, false if invalid field or cast error
     */
    public boolean updateMovieField(MarvelMovie movie, String field, Object value) {
        if (movie == null || field == null) return false;
        try {
            switch (field.toLowerCase()) {
                case "title":
                    String t = (String) value;
                    if (t == null || t.isBlank()) return false;
                    // ensure new title doesn't conflict with existing movie (unless same movie)
                    MarvelMovie other = findMovieByTitle(t);
                    if (other != null && other != movie) return false;
                    movie.setTitle(t);
                    break;
                case "releasedate":
                    String d = (String) value;
                    if (d == null || !d.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
                    movie.setReleaseDate(d);
                    break;
                case "phase":
                    int p = (int) value;
                    if (p <= 0) return false;
                    movie.setPhase(p);
                    break;
                case "director":
                    String dir = (String) value;
                    if (dir == null || dir.isBlank()) return false;
                    movie.setDirector(dir);
                    break;
                case "runningtimemin":
                    int rt = (int) value;
                    if (rt <= 0) return false;
                    movie.setRunningTimeMin(rt);
                    break;
                case "imdbrating":
                    double r = (double) value;
                    if (r < 1.0 || r > 10.0) return false;
                    movie.setImdbRating(r);
                    break;
                default:
                    return false;
            }
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * Update movie by title. This is convenient for tests and CLI calls.
     * @param title existing title
     * @param field field to update
     * @param value new value
     * @return true if updated
     */
    public boolean updateMovieByTitle(String title, String field, Object value) {
        MarvelMovie m = findMovieByTitle(title);
        if (m == null) return false;
        return updateMovieField(m, field, value);
    }

    // --- Custom Action ---
    public double calculateAverageRating(int phase) {
        if (phase <= 0) {
            return 0.0;
        }

        double totalRating = 0.0;
        int movieCount = 0;

        for (MarvelMovie movie : this.movies) {
            if (movie.getPhase() == phase) {
                totalRating += movie.getImdbRating();
                movieCount++;
            }
        }

        if (movieCount == 0) {
            return 0.0;
        }

        return totalRating / movieCount;
    }

    // Utility: clear all (helpful for tests)
    public void clearAll() {
        this.movies.clear();
    }
}
