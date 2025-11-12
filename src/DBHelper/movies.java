package DBHelper;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

/**
 * A specific Data Access Object (DAO) that extends {@link DBHelper} to provide structured
 * CRUD and query operations specifically for the **'movies'** table in the database.
 *
 * <p>This class separates the SQL complexity away from the MovieManager ensuring
 * that all database calls are correctly formatted for the 'movies' schema.</p>
 *
 * @author [Ramirez,Christopher]
 * @version 1.0
 */
public class movies extends DBHelper {
	private final String TABLE_NAME = "movies";
	// Public constants representing column names for type safety.
	public static final String id = "id";
	public static final String title = "title";
	public static final String releaseDate = "releaseDate";
	public static final String phase = "phase";
	public static final String director = "director";
	public static final String runningTimeMin = "runningTimeMin";
	public static final String imdbRating = "imdbRating";

	/**
	 * Constructs a full SQL SELECT query string based on provided filtering and sorting parameters.
	 *
	 * @param fields A comma-separated list of column names to select (e.g., "title, director"). Use {@code null} or empty string for all columns (*).
	 * @param whatField The column name for the WHERE clause (e.g., "phase"). Use {@code null} to skip WHERE clause.
	 * @param whatValue The value to filter by in the WHERE clause (e.g., "3"). Requires {@code whatField}.
	 * @param sortField The column name to sort by. Use {@code null} to skip sorting.
	 * @param sort The sort order ("ASC" or "DESC"). Requires {@code sortField}.
	 * @return The fully constructed SQL SELECT query string.
	 */
	private String prepareSQL(String fields, String whatField, String whatValue, String sortField, String sort) {
		String query = "SELECT ";
		query += fields == null ? " * FROM " + TABLE_NAME : fields + " FROM " + TABLE_NAME;
		query += whatField != null && whatValue != null ? " WHERE " + whatField + " = \"" + whatValue + "\"" : "";
		query += sort != null && sortField != null ? " order by " + sortField + " " + sort : "";
		return query;
	}

	/**
	 * Inserts a new movie record into the 'movies' table.
	 * It dynamically builds the INSERT query, safely handling string values and ignoring null inputs.
	 *
	 * @param id The unique identifier (usually autoincrement, so often {@code null}).
	 * @param title The title of the movie (String).
	 * @param releaseDate The release date (YYYY-MM-DD format, String).
	 * @param phase The MCU phase number (Integer).
	 * @param director The director's name (String).
	 * @param runningTimeMin The running time in minutes (Integer).
	 * @param imdbRating The IMDb rating (Double).
	 */
	public void insert(Integer id, String title, String releaseDate, Integer phase, String director, Integer runningTimeMin, Double imdbRating) {
		// Escape string values for SQL
		title = title != null ? "\"" + title + "\"" : null;
		releaseDate = releaseDate != null ? "\"" + releaseDate + "\"" : null;
		director = director != null ? "\"" + director + "\"" : null;

		Object[] values_ar = {id, title, releaseDate, phase, director, runningTimeMin, imdbRating};
		String[] fields_ar = {movies.id, movies.title, movies.releaseDate, movies.phase, movies.director, movies.runningTimeMin, movies.imdbRating};
		String values = "", fields = "";

		// Build dynamic field/value list
		for (int i = 0; i < values_ar.length; i++) {
			if (values_ar[i] != null) {
				values += values_ar[i] + ", ";
				fields += fields_ar[i] + ", ";
			}
		}

		if (!values.isEmpty()) {
			values = values.substring(0, values.length() - 2);
			fields = fields.substring(0, fields.length() - 2);
			super.execute("INSERT INTO " + TABLE_NAME + "(" + fields + ") values(" + values + ");");
		}
	}

	/**
	 * Deletes one or more records from the 'movies' table based on a single column filter.
	 *
	 * @param whatField The column name to use in the WHERE clause (e.g., "title").
	 * @param whatValue The value to match (e.g., "'Iron Man'"). **Note: String values must be copied by the caller.**
	 */
	public void delete(String whatField, String whatValue) {
		super.execute("DELETE from " + TABLE_NAME + " where " + whatField + " = " + whatValue + ";");
	}

	/**
	 * Updates a single field for one or more records in the 'movies' table based on a WHERE condition.
	 *
	 * @param whatField The column name to update (SET clause).
	 * @param whatValue The new value for the column.
	 * @param whereField The column name for the WHERE clause.
	 * @param whereValue The value to match in the WHERE clause.
	 */
	public void update(String whatField, String whatValue, String whereField, String whereValue) {
		super.execute("UPDATE " + TABLE_NAME + " set " + whatField + " = \"" + whatValue + "\" where " + whereField + " = \"" + whereValue + "\";");
	}

	/**
	 * Executes a SELECT query and returns the result as a raw 2D {@link ArrayList}.
	 * This uses the standard parameter structure to build the query.
	 *
	 * @param fields A comma-separated list of columns.
	 * @param whatField The column name for the WHERE clause.
	 * @param whatValue The value to filter by in the WHERE clause.
	 * @param sortField The column name to sort by.
	 * @param sort The sort order ("ASC" or "DESC").
	 * @return An ArrayList of ArrayLists containing the query results.
	 */
	public ArrayList<ArrayList<Object>> select(String fields, String whatField, String whatValue, String sortField, String sort) {
		return super.executeQuery(prepareSQL(fields, whatField, whatValue, sortField, sort));
	}

	/**
	 * Executes a raw SQL SELECT query provided by the caller and returns the result as a raw 2D {@link ArrayList}.
	 *
	 * @param query The complete SQL query string.
	 * @return An ArrayList of ArrayLists containing the query results.
	 */
	public ArrayList<ArrayList<Object>> getExecuteResult(String query) {
		return super.executeQuery(query);
	}

	/**
	 * Executes a raw SQL non-query command (INSERT, UPDATE, DELETE, CREATE) provided by the caller.
	 *
	 * @param query The complete SQL non-query command string.
	 */
	public void execute(String query) {
		super.execute(query);
	}

	/**
	 * Executes a SELECT query and formats the result into a {@link DefaultTableModel} for direct use in a {@link javax.swing.JTable}.
	 *
	 * @param fields A comma-separated list of columns.
	 * @param whatField The column name for the WHERE clause.
	 * @param whatValue The value to filter by in the WHERE clause.
	 * @param sortField The column name to sort by.
	 * @param sort The sort order ("ASC" or "DESC").
	 * @return A DefaultTableModel containing the query results and column headers.
	 */
	public DefaultTableModel selectToTable(String fields, String whatField, String whatValue, String sortField, String sort) {
		return super.executeQueryToTable(prepareSQL(fields, whatField, whatValue, sortField, sort));
	}

}