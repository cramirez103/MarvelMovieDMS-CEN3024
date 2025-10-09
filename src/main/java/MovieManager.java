import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * MovieManager.java
 * Manages the collection of MarvelMovie objects and implements all CRUD operations,
 * input validation, and the custom action. This represents the Business Logic Layer.
 *
 * The loadBatchData method reads from a user-provided file path using FileReader.
 */
public class MovieManager {
    // In-memory storage for Phase 1
    private List<MarvelMovie> movies;

    /**
     * Constructor initializes the movie list.
     */
    public MovieManager() {
        this.movies = new ArrayList<>();
    }

    // --- R: Read/Display Operation (Rubric 3) ---

    /**
     * Returns the list of all movies in the system.
     * @return List of MarvelMovie objects.
     */
    public List<MarvelMovie> getMovies() {
        return new ArrayList<>(this.movies); // Return a copy for immutability
    }

    // --- C: Create Data (Manual) (Rubric 4) ---

    /**
     * Adds a single, validated movie object to the collection.
     * @param movie The MarvelMovie object to add.
     * @return boolean true if the movie was added, false if a movie with the same title already exists.
     */
    public boolean addMovie(MarvelMovie movie) {
        if (findMovieByTitle(movie.getTitle()) != null) {
            return false;
        }
        this.movies.add(movie);
        return true;
    }

    // --- C: Create Data (Batch/Text File) (Rubric 2) ---

    /**
     * Reads data from a user-provided file path using FileReader.
     * @param filePath The path to the text file containing movie data.
     * @return String detailing the result (success/failure count).
     */
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

    // --- D: Remove Data (Rubric 5) ---

    public boolean removeMovie(String title) {
        MarvelMovie movieToRemove = findMovieByTitle(title);
        if (movieToRemove != null) {
            this.movies.remove(movieToRemove);
            return true;
        }
        return false;
    }

    // --- U: Update Data (Rubric 6) ---

    public MarvelMovie findMovieByTitle(String title) {
        String searchTitle = title.trim().toLowerCase();
        for (MarvelMovie movie : this.movies) {
            if (movie.getTitle().toLowerCase().equals(searchTitle)) {
                return movie;
            }
        }
        return null;
    }

    public boolean updateMovieField(MarvelMovie movie, String field, Object value) {
        switch (field.toLowerCase()) {
            case "title":
                movie.setTitle((String) value);
                break;
            case "releasedate":
                movie.setReleaseDate((String) value);
                break;
            case "phase":
                movie.setPhase((int) value);
                break;
            case "director":
                movie.setDirector((String) value);
                break;
            case "runningtimemin":
                movie.setRunningTimeMin((int) value);
                break;
            case "imdbrating":
                movie.setImdbRating((double) value);
                break;
            default:
                return false;
        }
        return true;
    }

    // --- Custom Action (Rubric 7) ---

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
}

