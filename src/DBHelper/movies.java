package DBHelper;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class movies extends DBHelper {
	private final String TABLE_NAME = "movies";
	public static final String id = "id";
	public static final String title = "title";
	public static final String releaseDate = "releaseDate";
	public static final String phase = "phase";
	public static final String director = "director";
	public static final String runningTimeMin = "runningTimeMin";
	public static final String imdbRating = "imdbRating";

	private String prepareSQL(String fields, String whatField, String whatValue, String sortField, String sort) {
		String query = "SELECT ";
		query += fields == null ? " * FROM " + TABLE_NAME : fields + " FROM " + TABLE_NAME;
		query += whatField != null && whatValue != null ? " WHERE " + whatField + " = \"" + whatValue + "\"" : "";
		query += sort != null && sortField != null ? " order by " + sortField + " " + sort : "";
		return query;
	}

	public void insert(Integer id, String title, String releaseDate, Integer phase, String director, Integer runningTimeMin, Double imdbRating) {
		title = title != null ? "\"" + title + "\"" : null;
		releaseDate = releaseDate != null ? "\"" + releaseDate + "\"" : null;
		director = director != null ? "\"" + director + "\"" : null;
		
		Object[] values_ar = {id, title, releaseDate, phase, director, runningTimeMin, imdbRating};
		String[] fields_ar = {movies.id, movies.title, movies.releaseDate, movies.phase, movies.director, movies.runningTimeMin, movies.imdbRating};
		String values = "", fields = "";
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

	public void delete(String whatField, String whatValue) {
		super.execute("DELETE from " + TABLE_NAME + " where " + whatField + " = " + whatValue + ";");
	}

	public void update(String whatField, String whatValue, String whereField, String whereValue) {
		super.execute("UPDATE " + TABLE_NAME + " set " + whatField + " = \"" + whatValue + "\" where " + whereField + " = \"" + whereValue + "\";");
	}

	public ArrayList<ArrayList<Object>> select(String fields, String whatField, String whatValue, String sortField, String sort) {
		return super.executeQuery(prepareSQL(fields, whatField, whatValue, sortField, sort));
	}

	public ArrayList<ArrayList<Object>> getExecuteResult(String query) {
		return super.executeQuery(query);
	}

	public void execute(String query) {
		super.execute(query);
	}

	public DefaultTableModel selectToTable(String fields, String whatField, String whatValue, String sortField, String sort) {
		return super.executeQueryToTable(prepareSQL(fields, whatField, whatValue, sortField, sort));
	}

}