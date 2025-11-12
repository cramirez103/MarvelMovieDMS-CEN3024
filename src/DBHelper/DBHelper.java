package DBHelper;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;

/**
 * Provides low-level, generic utility functions for connecting to, executing statements against,
 * and retrieving results from the SQLite database using JDBC.
 *
 * <p>Role in System: Acts as the base class for other, more specific database helpers (if any)
 * or as a foundational layer for direct SQL command execution.
 *
 * @author [Ramirez,Christopher]
 * @version 1.0
 */
public class DBHelper {
	private final String DATABASE_NAME = "C:\\sqlite\\Myprojects\\myproject.db";
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	/**
	 * Constructs a DBHelper instance, initializing JDBC resources to null.
	 */
	public DBHelper() {
		connection = null;
		statement = null;
		resultSet = null;
	}

	/**
	 * Establishes a connection to the SQLite database defined by {DATABASE_NAME}.
	 * Loads the SQLite JDBC driver and creates a {@link Statement} object for execution.
	 */
	private void connect() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Safely closes the database connection, statement, and result set (if not null).
	 */
	private void close() {
		try {
			if (connection != null) connection.close();
			if (statement != null) statement.close();
			if (resultSet != null) resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a two-dimensional {@link ArrayList} of {@link Object}s into a two-dimensional {@link Object} array.
	 * This format is required for populating Swing components like {@link DefaultTableModel}.
	 *
	 * @param list The ArrayList of ArrayLists to convert.
	 * @return A 2D array representing the data.
	 */
	private Object[][] arrayListTo2DArray(ArrayList<ArrayList<Object>> list) {
		Object[][] array = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			ArrayList<Object> row = list.get(i);
			array[i] = row.toArray(new Object[row.size()]);
		}
		return array;
	}

	/**
	 * Executes a non-query SQL command (e.g., INSERT, UPDATE, DELETE, CREATE) against the database.
	 * Handles connection, execution, and cleanup internally.
	 *
	 * @param sql The SQL command string to execute.
	 */
	protected void execute(String sql) {
		try {
			connect();
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}

	/**
	 * Executes a SELECT query and formats the results into a {@link DefaultTableModel} for direct use in a {@link javax.swing.JTable}.
	 *
	 * @param sql The SQL SELECT query string.
	 * @return A DefaultTableModel containing the query results and column headers.
	 */
	protected DefaultTableModel executeQueryToTable(String sql) {
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> columns = new ArrayList<Object>();
		connect();
		try {
			resultSet = statement.executeQuery(sql);
			int columnCount = resultSet.getMetaData().getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(resultSet.getMetaData().getColumnName(i));
			while (resultSet.next()) {
				ArrayList<Object> subresult = new ArrayList<Object>();
				for (int i = 1; i <= columnCount; i++)
					subresult.add(resultSet.getObject(i));
				result.add(subresult);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return new DefaultTableModel(arrayListTo2DArray(result), columns.toArray());
	}

	/**
	 * Executes a SELECT query and returns the results as a raw two-dimensional {@link ArrayList}.
	 * The outer list represents rows, and the inner list represents column data for that row.
	 *
	 * @param sql The SQL SELECT query string.
	 * @return An ArrayList of ArrayLists containing the query results.
	 */
	protected ArrayList<ArrayList<Object>> executeQuery(String sql) {
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		connect();
		try {
			resultSet = statement.executeQuery(sql);
			int columnCount = resultSet.getMetaData().getColumnCount();
			while (resultSet.next()) {
				ArrayList<Object> subresult = new ArrayList<Object>();
				for (int i = 1; i <= columnCount; i++) {
					subresult.add(resultSet.getObject(i));
				}
				result.add(subresult);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		close();
		return result;
	}

}