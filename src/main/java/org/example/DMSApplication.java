// Christopher Ramirez, CEN-3024, 10/9/2025

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;

/**
 * DMSApplication.java
 * The main application class handling the CLI and user interaction.
 * explicitly asks the user for the file path when selecting Batch Load.
 */
public class DMSApplication {

    private final MovieManager manager;
    private final Scanner scanner;

    public DMSApplication() {
        this.manager = new MovieManager();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("\n--- Marvel Movie DMS (Phase 1) Started ---");
        System.out.println("System is currently empty. Please use Option 2 or 3 to load data.");

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getValidatedIntInput("Enter your choice: ", 0, 6);

            switch (choice) {
                case 1:
                    displayData();
                    break;
                case 2:
                    manualCreate();
                    break;
                case 3:
                    loadBatchDataFromPath();
                    break;
                case 4:
                    updateRecord();
                    break;
                case 5:
                    removeRecord();
                    break;
                case 6:
                    runCustomAction();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nExiting Marvel Movie DMS. Goodbye!");
                    break;
            }
        }
        scanner.close();
    }

    /**
     * Prompts user for a file path and loads data using MovieManager.
     */
    private void loadBatchDataFromPath() {
        System.out.println("\n--- Batch Load Data ---");
        System.out.print("Please enter the full file path to your batch data file: ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("ERROR: File path cannot be empty. Returning to main menu.");
            return;
        }

        String result = manager.loadBatchData(filePath);
        System.out.println(result);
    }

    private void displayMenu() {
        System.out.println("\n--- Marvel Movie DMS Menu (Phase 1) ---");
        System.out.println("1. Display All Data (Read)");
        System.out.println("2. Create Data Manually");
        System.out.println("3. Batch Load Data (Provide File Path)");
        System.out.println("4. Update Record");
        System.out.println("5. Remove Data");
        System.out.println("6. Run Custom Action (Avg Rating by Phase)");
        System.out.println("0. Exit Program");
        System.out.println("---------------------------------------");
    }

    // --- Input Validation Helpers ---

    private int getValidatedIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                if (value >= min && (max == -1 || value <= max)) {
                    scanner.nextLine();
                    return value;
                } else {
                    System.out.printf("Error: Input must be between %d and %s.\n", min, (max == -1 ? "infinity" : max));
                }
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
                    scanner.nextLine();
                    return value;
                } else {
                    System.out.printf("Error: Input must be between %.1f and %.1f.\n", min, max);
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a valid number (e.g., 7.5).");
                scanner.nextLine();
            }
        }
    }

    private String getValidatedStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            } else {
                System.out.println("Error: Input cannot be empty.");
            }
        }
    }

    // --- CRUD + Custom Action Handlers ---

    private void manualCreate() {
        System.out.println("\n--- Enter New Movie Details ---");

        String title = getValidatedStringInput("Enter Title: ");
        String releaseDate = getValidatedStringInput("Enter Release Date (YYYY-MM-DD): ");
        while (!releaseDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Error: Date format must be YYYY-MM-DD.");
            releaseDate = getValidatedStringInput("Enter Release Date (YYYY-MM-DD): ");
        }

        int phase = getValidatedIntInput("Enter Phase (e.g., 4): ", 1, -1);
        String director = getValidatedStringInput("Enter Director: ");
        int runningTimeMin = getValidatedIntInput("Enter Running Time (min): ", 30, -1);
        double imdbRating = getValidatedDoubleInput("Enter IMDb Rating (1.0-10.0): ", 1.0, 10.0);

        MarvelMovie newMovie = new MarvelMovie(title, releaseDate, phase, director, runningTimeMin, imdbRating);

        if (manager.addMovie(newMovie)) {
            System.out.println("\nSUCCESS: Record created and added to the system:");
            System.out.println(newMovie);
        } else {
            System.out.println("\nERROR: A movie with the title '" + title + "' already exists. Record NOT added.");
        }
    }

    private void displayData() {
        List<MarvelMovie> movies = manager.getMovies();
        System.out.println("\n--- Current Data Set (Total: " + movies.size() + " Records) ---");
        if (movies.isEmpty()) {
            System.out.println("The system is empty.");
            return;
        }

        for (int i = 0; i < movies.size(); i++) {
            System.out.println("Record #" + (i + 1) + ":");
            System.out.println(movies.get(i).toString());
            System.out.println("---------------------------------------");
        }
    }

    private void removeRecord() {
        System.out.println("\n--- Remove Record ---");
        String title = getValidatedStringInput("Enter the title of the movie to remove: ");

        if (manager.removeMovie(title)) {
            System.out.println("\nSUCCESS: The movie '" + title + "' has been removed.");
        } else {
            System.out.println("\nERROR: Movie with title '" + title + "' was not found. Nothing removed.");
        }
    }

    private void updateRecord() {
        System.out.println("\n--- Update Record ---");
        String title = getValidatedStringInput("Enter the title of the movie to update: ");

        MarvelMovie movieToUpdate = manager.findMovieByTitle(title);
        if (movieToUpdate == null) {
            System.out.println("\nERROR: Movie with title '" + title + "' was not found. Cannot update.");
            return;
        }

        System.out.println("\nCurrently updating: " + movieToUpdate.getTitle());
        System.out.println("Available fields: title, releaseDate, phase, director, runningTimeMin, imdbRating");

        String field = getValidatedStringInput("Enter the field name to update: ").toLowerCase();
        Object newValue = null;

        try {
            switch (field) {
                case "title":
                case "director":
                    newValue = getValidatedStringInput("Enter new " + field + ": ");
                    break;
                case "releasedate":
                    String date = getValidatedStringInput("Enter new Release Date (YYYY-MM-DD): ");
                    while (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        System.out.println("Error: Date format must be YYYY-MM-DD.");
                        date = getValidatedStringInput("Enter new Release Date (YYYY-MM-DD): ");
                    }
                    newValue = date;
                    break;
                case "phase":
                    newValue = getValidatedIntInput("Enter new Phase: ", 1, -1);
                    break;
                case "runningtimemin":
                    newValue = getValidatedIntInput("Enter new Running Time (min): ", 30, -1);
                    break;
                case "imdbrating":
                    newValue = getValidatedDoubleInput("Enter new IMDb Rating (1.0-10.0): ", 1.0, 10.0);
                    break;
                default:
                    System.out.println("\nERROR: Invalid field name entered. Update aborted.");
                    return;
            }

            if (manager.updateMovieField(movieToUpdate, field, newValue)) {
                System.out.println("\nSUCCESS: '" + movieToUpdate.getTitle() + "' " + field + " updated.");
                System.out.println("New record data:\n" + movieToUpdate.toString());
            }

        } catch (Exception e) {
            System.out.println("An unexpected error occurred during update validation. Update failed.");
        }
    }

    private void runCustomAction() {
        System.out.println("\n--- Custom Action: Calculate Average IMDb Rating ---");
        int phase = getValidatedIntInput("Enter the MCU Phase number to analyze: ", 1, -1);

        double average = manager.calculateAverageRating(phase);

        if (average > 0.0) {
            System.out.printf("\nRESULT: The average IMDb rating for all movies in Phase %d is: %.2f\n", phase, average);
        } else if (manager.getMovies().isEmpty()) {
            System.out.println("\nNOTICE: The system is empty, so no calculation could be performed.");
        } else {
            System.out.printf("\nNOTICE: No movies found in Phase %d to calculate an average.\n", phase);
        }
    }

    public static void main(String[] args) {
        DMSApplication app = new DMSApplication();
        app.run();
    }
}