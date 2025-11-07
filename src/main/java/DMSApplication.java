import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * DMSApplication.java
 * Main application class handling CLI and user interaction.
 * Added proper validation for release date and running time.
 */
public class DMSApplication {

    private final MovieManager manager;
    private final Scanner scanner;

    public DMSApplication() {
        this.manager = new MovieManager();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("\n--- Marvel Movie DMS (Phase 3 CLI) Started ---");
        System.out.println("System is currently empty. Please use Option 2 or 3 to load data.");

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getValidatedIntInput("Enter your choice: ", 0, 6);

            switch (choice) {
                case 1 -> displayData();
                case 2 -> manualCreate();
                case 3 -> loadBatchDataFromPath();
                case 4 -> updateRecord();
                case 5 -> removeRecord();
                case 6 -> runCustomAction();
                case 0 -> {
                    running = false;
                    System.out.println("\nExiting Marvel Movie DMS. Goodbye!");
                }
            }
        }
        scanner.close();
    }

    private void displayMenu() {
        System.out.println("\n--- Marvel Movie DMS Menu ---");
        System.out.println("1. Display All Data (Read)");
        System.out.println("2. Create Data Manually");
        System.out.println("3. Batch Load Data (Provide File Path)");
        System.out.println("4. Update Record");
        System.out.println("5. Remove Data");
        System.out.println("6. Run Custom Action (Avg Rating by Phase)");
        System.out.println("0. Exit Program");
    }

    // ------------------ Input Validation Helpers ------------------

    private int getValidatedIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                if (value >= min && (max == -1 || value <= max)) {
                    scanner.nextLine(); // consume leftover newline
                    return value;
                }
                System.out.printf("Error: Input must be between %d and %s.\n", min, (max == -1 ? "infinity" : max));
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a whole number.");
                scanner.nextLine();
            }
        }
    }

    private double getValidatedDoubleInput(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                if (value >= min && value <= max) {
                    scanner.nextLine(); // consume leftover newline
                    return value;
                }
                System.out.printf("Error: Input must be between %.1f and %.1f.\n", min, max);
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }

    private String getValidatedStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Error: Input cannot be empty.");
        }
    }

    // ------------------ Custom Validation Methods ------------------

    private boolean isValidReleaseDate(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) return false;

        String[] parts = dateStr.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        if (year < 1900 || year > 2025) return false;
        if (month < 1 || month > 12) return false;

        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        // Leap year check
        if (month == 2 && ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0))) {
            daysInMonth[1] = 29;
        }
        return day >= 1 && day <= daysInMonth[month - 1];
    }

    private boolean isValidRunningTime(int minutes) {
        return minutes >= 30 && minutes <= 300; // 30 minutes to 5 hours max
    }

    // ------------------ CRUD and Custom Actions ------------------

    private void manualCreate() {
        System.out.println("\n--- Enter New Movie Details ---");

        String title = getValidatedStringInput("Enter Title: ");

        String releaseDate = getValidatedStringInput("Enter Release Date (YYYY-MM-DD): ");
        while (!isValidReleaseDate(releaseDate)) {
            System.out.println("Error: Invalid date. Must be a real date between 1900-2025.");
            releaseDate = getValidatedStringInput("Enter Release Date (YYYY-MM-DD): ");
        }

        int phase = getValidatedIntInput("Enter Phase (e.g., 4): ", 1, -1);
        String director = getValidatedStringInput("Enter Director: ");

        int runningTimeMin = getValidatedIntInput("Enter Running Time (min): ", 1, 10000);
        while (!isValidRunningTime(runningTimeMin)) {
            System.out.println("Error: Running time must be between 30 and 300 minutes.");
            runningTimeMin = getValidatedIntInput("Enter Running Time (min): ", 1, 10000);
        }

        double imdbRating = getValidatedDoubleInput("Enter IMDb Rating (1.0-10.0): ", 1.0, 10.0);

        if (manager.addMovie(title, releaseDate, phase, director, runningTimeMin, imdbRating)) {
            System.out.println("\nSUCCESS: Record created and added to the system.");
        } else {
            System.out.println("\nERROR: Could not add movie (duplicate or invalid data).");
        }
    }

    private void displayData() {
        List<MarvelMovie> movies = manager.getMovies();
        System.out.println("\n--- Current Data Set (Total: " + movies.size() + " Records) ---");
        if (movies.isEmpty()) {
            System.out.println("The system is empty.");
            return;
        }
        for (MarvelMovie movie : movies) {
            System.out.println(movie);
            System.out.println("--------------------------------");
        }
    }

    private void removeRecord() {
        String title = getValidatedStringInput("Enter the title of the movie to remove: ");
        if (manager.removeMovie(title)) {
            System.out.println("SUCCESS: Movie removed.");
        } else {
            System.out.println("ERROR: Movie not found.");
        }
    }

    private void updateRecord() {
        String title = getValidatedStringInput("Enter the title of the movie to update: ");
        MarvelMovie movie = manager.findMovieByTitle(title);
        if (movie == null) {
            System.out.println("ERROR: Movie not found.");
            return;
        }

        System.out.println("Available fields: title, releaseDate, phase, director, runningTimeMin, imdbRating");
        String field = getValidatedStringInput("Enter the field name to update: ").toLowerCase();
        Object newValue;

        switch (field) {
            case "title", "director" -> newValue = getValidatedStringInput("Enter new " + field + ": ");
            case "releasedate" -> {
                String date = getValidatedStringInput("Enter new Release Date (YYYY-MM-DD): ");
                while (!isValidReleaseDate(date)) {
                    System.out.println("Error: Invalid date. Must be a real date between 1900-2025.");
                    date = getValidatedStringInput("Enter new Release Date (YYYY-MM-DD): ");
                }
                newValue = date;
            }
            case "phase" -> newValue = getValidatedIntInput("Enter new Phase: ", 1, -1);
            case "runningtimemin" -> {
                int runtime = getValidatedIntInput("Enter new Running Time (min): ", 1, 10000);
                while (!isValidRunningTime(runtime)) {
                    System.out.println("Error: Running time must be between 30 and 300 minutes.");
                    runtime = getValidatedIntInput("Enter new Running Time (min): ", 1, 10000);
                }
                newValue = runtime;
            }
            case "imdbrating" -> newValue = getValidatedDoubleInput("Enter new IMDb Rating (1.0-10.0): ", 1.0, 10.0);
            default -> {
                System.out.println("Invalid field name. Update aborted.");
                return;
            }
        }

        if (manager.updateMovieField(movie, field, newValue)) {
            System.out.println("SUCCESS: Update complete.");
        } else {
            System.out.println("ERROR: Update failed.");
        }
    }

    private void runCustomAction() {
        int phase = getValidatedIntInput("Enter MCU Phase number to analyze: ", 1, -1);
        double average = manager.calculateAverageRating(phase);
        if (average > 0.0) {
            System.out.printf("Average IMDb rating for Phase %d: %.2f\n", phase, average);
        } else {
            System.out.println("No movies found in this phase.");
        }
    }

    private void loadBatchDataFromPath() {
        System.out.print("Enter full path for batch data file: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("ERROR: File path cannot be empty.");
            return;
        }
        System.out.println(manager.loadBatchData(path));
    }

    public static void main(String[] args) {
        new DMSApplication().run();

    }
}
