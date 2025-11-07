package DBHelper;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class MovieDatabaseDemo {
    public static void main(String[] args) {

        // Create an instance of the movies DBHelper
        movies db = new movies();

        // Step 1: Print all current movies
        System.out.println("=== Initial Movie List ===");
        ArrayList<ArrayList<Object>> data = db.getExecuteResult("SELECT * FROM movies;");
        printDatabase(data);

        // Step 2: Delete a movie (safe: check if it exists first)
        System.out.println("\n=== Deleting a Movie ===");
        if (!db.getExecuteResult("SELECT * FROM movies WHERE title = 'The Incredible Hulk';").isEmpty()) {
            db.delete("title", "\"The Incredible Hulk\"");
        }
        data = db.getExecuteResult("SELECT * FROM movies;");
        printDatabase(data);

        // Step 3: Insert new movies safely (avoid duplicates)
        System.out.println("\n=== Inserting New Movies ===");

        if (db.getExecuteResult("SELECT * FROM movies WHERE title = 'Black Widow';").isEmpty()) {
            db.insert(null, "Black Widow", "2021-07-09", 4, "Cate Shortland", 134, 6.8);
        }

        if (db.getExecuteResult("SELECT * FROM movies WHERE title = 'Shang-Chi';").isEmpty()) {
            db.insert(null, "Shang-Chi", "2021-09-03", 4, "Destin Daniel Cretton", 132, 7.5);
        }

        data = db.getExecuteResult("SELECT * FROM movies;");
        printDatabase(data);

        // Step 4: Update a movie safely
        System.out.println("\n=== Updating a Movie Rating ===");
        if (!db.getExecuteResult("SELECT * FROM movies WHERE title = 'Thor: Ragnarok';").isEmpty()) {
            db.update("imdbRating", "8.1", "title", "Thor: Ragnarok");
        }
        data = db.getExecuteResult("SELECT * FROM movies;");
        printDatabase(data);

        // Step 5: Select filtered/sorted query
        System.out.println("\n=== Filtered & Sorted Query ===");
        data = db.select("title, releaseDate, phase, director, imdbRating",
                "phase", "3", "imdbRating", "DESC");
        printDatabase(data);

        // Step 6: Demonstrate DefaultTableModel usage
        System.out.println("\n=== JTable TableModel Demo ===");
        DefaultTableModel table = db.selectToTable("title, releaseDate, phase, director, imdbRating",
                "phase", "3", "imdbRating", "DESC");
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                System.out.print(table.getValueAt(row, col) + " | ");
            }
            System.out.println();
        }
    }

    // Helper method to print ArrayList<ArrayList<Object>> in table form
    private static void printDatabase(ArrayList<ArrayList<Object>> data) {
        for (ArrayList<Object> row : data) {
            for (Object obj : row) {
                System.out.print(obj + " | ");
            }
            System.out.println();
        }
    }
}
