import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MovieManager {
    private List<MarvelMovie> movies;
    private final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    private final LocalDate MAX_DATE = LocalDate.of(2025, 12, 31);

    public MovieManager() {
        this.movies = new ArrayList<>();
    }

    public List<MarvelMovie> getMovies() {
        return new ArrayList<>(this.movies);
    }

    // ---------- CREATE ----------
    public boolean addMovie(MarvelMovie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank()) return false;
        if (!isValidMovie(movie)) return false;
        if (findMovieByTitle(movie.getTitle()) != null) return false;
        this.movies.add(movie);
        return true;
    }

    public boolean addMovie(String title, String releaseDate, int phase, String director, int runningTimeMin, double imdbRating) {
        if (title == null || title.isBlank() || director == null || director.isBlank()) return false;
        MarvelMovie movie = new MarvelMovie(title.trim(), releaseDate.trim(), phase, director.trim(), runningTimeMin, imdbRating);
        return addMovie(movie);
    }

    // ---------- REMOVE ----------
    public boolean removeMovie(String title) {
        MarvelMovie movieToRemove = findMovieByTitle(title);
        if (movieToRemove != null) {
            this.movies.remove(movieToRemove);
            return true;
        }
        return false;
    }

    // ---------- UPDATE ----------
    public MarvelMovie findMovieByTitle(String title) {
        if (title == null) return null;
        String searchTitle = title.trim().toLowerCase();
        for (MarvelMovie movie : this.movies) {
            if (movie.getTitle().toLowerCase().equals(searchTitle)) return movie;
        }
        return null;
    }

    public boolean updateMovieField(MarvelMovie movie, String field, Object value) {
        if (movie == null || field == null) return false;
        try {
            switch (field.toLowerCase()) {
                case "title":
                    String t = (String) value;
                    if (t == null || t.isBlank()) return false;
                    MarvelMovie other = findMovieByTitle(t);
                    if (other != null && other != movie) return false;
                    movie.setTitle(t);
                    break;
                case "releasedate":
                    String d = (String) value;
                    if (!isValidDate(d)) return false;
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
                    if (!isValidRuntime(rt)) return false;
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

    public boolean updateMovieByTitle(String title, String field, Object value) {
        MarvelMovie m = findMovieByTitle(title);
        if (m == null) return false;
        return updateMovieField(m, field, value);
    }

    // ---------- CUSTOM ACTION ----------
    public double calculateAverageRating(int phase) {
        if (phase <= 0) return 0.0;
        double total = 0.0;
        int count = 0;
        for (MarvelMovie movie : movies) {
            if (movie.getPhase() == phase) {
                total += movie.getImdbRating();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    // ---------- BATCH LOAD ----------
    public String loadBatchData(String filePath) {
        int added = 0, failed = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length != 6) {
                    failed++;
                    continue;
                }
                String title = parts[0].trim();
                String date = parts[1].trim();
                int phase = Integer.parseInt(parts[2].trim());
                String director = parts[3].trim();
                int runtime = Integer.parseInt(parts[4].trim());
                double rating = Double.parseDouble(parts[5].trim());
                if (!addMovie(title, date, phase, director, runtime, rating)) failed++;
                else added++;
            }
        } catch (IOException e) {
            return "ERROR: Unable to read file.";
        }
        return String.format("Batch Load Complete: %d added, %d failed.", added, failed);
    }

    // ---------- VALIDATION HELPERS ----------
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
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
    public void clearAll() {
        this.movies.clear();
    }
}
