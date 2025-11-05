import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DMSGui - Swing GUI for MovieManager.
 */
public class DMSGui extends JFrame {
    private final MovieManager manager;

    // --- MARVEL STYLING CONSTANTS ---
    private static final Color ACCENT_RED = new Color(190, 30, 45);
    private static final Color ACCENT_GOLD = new Color(255, 170, 0);
    private static final Color LIGHT_BG = new Color(240, 240, 240);
    private static final Color DARK_TEXT = new Color(50, 50, 50);
    private static final Font MARVEL_FONT = new Font("Dialog", Font.BOLD, 14);
    private static final Border MARVEL_BORDER = BorderFactory.createLineBorder(ACCENT_RED, 2);
    // ------------------------------------------

    // UI components
    private final DefaultTableModel tableModel;
    private final JTable movieTable;

    // input fields
    private final JTextField titleField = new JTextField(20);
    private final JTextField dateField = new JTextField(10);
    private final JTextField phaseField = new JTextField(4);
    private final JTextField directorField = new JTextField(15);
    private final JTextField runtimeField = new JTextField(5);
    private final JTextField ratingField = new JTextField(4);

    // update/search field
    private final JTextField searchTitleField = new JTextField(15);

    // currently selected movie for editing
    private MarvelMovie movieBeingEdited = null;

    public DMSGui() {
        super("MARVEL CINEMATIC UNIVERSE DMS");
        this.manager = new MovieManager();

        // Table setup
        String[] cols = {"Title", "Release Date", "Phase", "Director", "Runtime (min)", "IMDb"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- TOOLTIP FIX IMPLEMENTATION ---
        movieTable = new JTable(tableModel) {
            /**
             * Overrides getToolTipText to return the full cell content when the mouse hovers over it,
             * preventing long titles from being truncated with "...".
             */
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                // Determine the row and column the mouse is currently over
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                // Ensure the row and column are valid
                if (rowIndex != -1 && colIndex != -1) {
                    // Get the value from the table model at that location
                    Object value = getValueAt(rowIndex, colIndex);
                    if (value != null) {
                        return value.toString();
                    }
                }
                // Return null if no specific tooltip is found
                return null;
            }
        };
        // ---------------------------------

        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING TABLE ---
        movieTable.setBackground(Color.WHITE);
        movieTable.setForeground(DARK_TEXT);
        movieTable.setSelectionBackground(ACCENT_RED.brighter());
        movieTable.setGridColor(Color.LIGHT_GRAY);
        movieTable.getTableHeader().setBackground(ACCENT_RED);
        movieTable.getTableHeader().setForeground(Color.WHITE);
        // ---------------------

        // Layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Set the main content pane background
        getContentPane().setBackground(LIGHT_BG);

        add(buildTopPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.getViewport().setBackground(LIGHT_BG);
        add(scrollPane, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        setSize(1000, 700);
        setLocationRelativeTo(null);
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(LIGHT_BG);

        // Left: Create/Edit form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                MARVEL_BORDER,
                "ADD / EDIT MOVIE DATA",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                MARVEL_FONT,
                ACCENT_RED
        ));
        form.setBackground(LIGHT_BG);

        // Style all input fields
        JTextField[] fields = {titleField, dateField, phaseField, directorField, runtimeField, ratingField, searchTitleField};
        for (JTextField field : fields) {
            field.setBackground(Color.WHITE);
            field.setForeground(DARK_TEXT);
            field.setCaretColor(ACCENT_RED);
            field.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);

        // Helper method to add Marvel-styled labels
        java.util.function.BiConsumer<String, Component> addField = (label, field) -> {
            JLabel lbl = new JLabel(label);
            lbl.setForeground(DARK_TEXT);
            lbl.setFont(MARVEL_FONT);
            c.gridx = 0; c.anchor = GridBagConstraints.EAST; form.add(lbl, c);
            c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(field, c);
            c.gridy++;
        };

        c.gridy = 0;
        addField.accept("TITLE:", titleField);
        addField.accept("RELEASE DATE (YYYY-MM-DD):", dateField);
        addField.accept("PHASE:", phaseField);
        addField.accept("DIRECTOR:", directorField);
        addField.accept("RUNTIME (MIN):", runtimeField);

        // FIX: UPDATE LABEL TEXT to (1.0-10.0)
        addField.accept("IMDB RATING (1.0-10.0):", ratingField);
        // ---------------------------------

        // ADD BUTTON
        JButton addBtn = new JButton("ADD NEW MOVIE");
        addBtn.setBackground(ACCENT_RED);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(MARVEL_FONT);
        addBtn.addActionListener(e -> handleAdd());
        c.gridx = 1; c.anchor = GridBagConstraints.CENTER; form.add(addBtn, c);

        // Right: Controls
        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createTitledBorder(
                MARVEL_BORDER, "CONTROLS",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                MARVEL_FONT, ACCENT_RED
        ));
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(LIGHT_BG);

        // BATCH LOAD BUTTON
        JButton batchBtn = new JButton("BATCH LOAD (FILE)");
        batchBtn.setBackground(ACCENT_RED);
        batchBtn.setForeground(Color.WHITE);
        batchBtn.setFont(MARVEL_FONT);
        batchBtn.addActionListener(e -> handleBatchLoad());
        batchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        controls.add(batchBtn);
        controls.add(Box.createVerticalStrut(8));

        // REFRESH BUTTON
        JButton refreshBtn = new JButton("REFRESH TABLE");
        refreshBtn.setBackground(ACCENT_RED);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(MARVEL_FONT);
        refreshBtn.addActionListener(e -> refreshTable());
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        controls.add(refreshBtn);
        controls.add(Box.createVerticalStrut(8));

        // DELETE BUTTON
        JButton deleteBtn = new JButton("DELETE SELECTED MOVIE");
        deleteBtn.setBackground(ACCENT_RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(MARVEL_FONT);
        deleteBtn.addActionListener(e -> handleDeleteSelected());
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        controls.add(deleteBtn);
        controls.add(Box.createVerticalStrut(8));

        // Update area
        JPanel updatePanel = new JPanel();
        updatePanel.setBorder(BorderFactory.createTitledBorder(
                MARVEL_BORDER, "UPDATE MOVIE",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                MARVEL_FONT, ACCENT_RED
        ));
        updatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        updatePanel.setBackground(LIGHT_BG);

        JLabel searchLabel = new JLabel("Search Title:");
        searchLabel.setForeground(DARK_TEXT);
        searchLabel.setFont(MARVEL_FONT);
        updatePanel.add(searchLabel);

        searchTitleField.setColumns(15);
        updatePanel.add(searchTitleField);

        // FIND BUTTON
        JButton findBtn = new JButton("FIND");
        findBtn.setBackground(ACCENT_RED);
        findBtn.setForeground(Color.WHITE);
        findBtn.setFont(MARVEL_FONT);
        findBtn.setPreferredSize(new Dimension(80, 25));
        findBtn.addActionListener(e -> populateFieldsFromSearch());

        // APPLY UPDATE BUTTON
        JButton updateBtn = new JButton("APPLY UPDATE");
        updateBtn.setBackground(ACCENT_RED);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(MARVEL_FONT);
        updateBtn.setPreferredSize(new Dimension(130, 25));
        updateBtn.addActionListener(e -> handleUpdate());

        updatePanel.add(findBtn);
        updatePanel.add(updateBtn);

        // Custom Action
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        customPanel.setBorder(BorderFactory.createTitledBorder(
                MARVEL_BORDER, "CUSTOM ACTION",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                MARVEL_FONT, ACCENT_RED
        ));
        customPanel.setBackground(LIGHT_BG);

        JLabel phaseLabel = new JLabel("Phase:");
        phaseLabel.setForeground(DARK_TEXT);
        phaseLabel.setFont(MARVEL_FONT);
        customPanel.add(phaseLabel);

        JTextField phaseInput = new JTextField(4);
        phaseInput.setBackground(Color.WHITE);
        phaseInput.setForeground(DARK_TEXT);
        phaseInput.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        customPanel.add(phaseInput);

        // CALC AVG RATING BUTTON
        JButton avgBtn = new JButton("CALC AVG RATING");
        avgBtn.setBackground(ACCENT_GOLD);
        avgBtn.setForeground(DARK_TEXT);
        avgBtn.setFont(MARVEL_FONT);
        avgBtn.addActionListener(e -> {
            try {
                int ph = Integer.parseInt(phaseInput.getText().trim());
                double avg = manager.calculateAverageRating(ph);
                if (avg > 0.0) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Average IMDb rating for Phase %d: %.2f", ph, avg),
                            "AVERAGE RATING", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No movies found in that phase.", "AVERAGE RATING",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showError("Enter a valid integer for phase.");
            }
        });
        customPanel.add(avgBtn);

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setBackground(LIGHT_BG);
        rightCol.add(controls);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(updatePanel);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(customPanel);

        panel.add(form, BorderLayout.WEST);
        panel.add(rightCol, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        p.setBackground(ACCENT_RED);
        JLabel help = new JLabel("M.C.U. DATA MANAGEMENT SYSTEM | Use 'FIND' to edit existing movies, or 'ADD' to create new entries.");
        help.setForeground(Color.WHITE);
        help.setFont(new Font("Dialog", Font.PLAIN, 12));
        p.add(help, BorderLayout.CENTER);
        return p;
    }

    // ---------- Handlers (Functionality Unchanged) ----------

    private void handleAdd() {
        // Primary check: if we are in edit mode, block the 'Add' function.
        if (movieBeingEdited != null) {
            showError("You are currently in EDIT mode (Movie: " + movieBeingEdited.getTitle() + "). Use 'Apply Update' to save changes, or clear fields to exit edit mode.");
            return;
        }

        String validationErrors = validateInputFields();

        if (!validationErrors.isEmpty()) {
            showValidationErrors(validationErrors);
            return;
        }

        // --- Data Retrieval ---
        String title = titleField.getText().trim();
        String date = dateField.getText().trim();
        String phaseStr = phaseField.getText().trim();
        String director = directorField.getText().trim();
        String runtimeStr = runtimeField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        try {
            int phase = Integer.parseInt(phaseStr);
            int runtime = Integer.parseInt(runtimeStr);
            double rating = Double.parseDouble(ratingStr);

            boolean ok = manager.addMovie(title, date, phase, director, runtime, rating);

            if (ok) {
                showInfo("Movie added successfully: " + title);
                clearInputFields();
                refreshTable();
            } else {
                // This means MovieManager returned false, likely due to a duplicate title
                showError("Failed to add movie. A movie with the title '" + title + "' may already exist.");
            }
        } catch (NumberFormatException ex) {
            // Should be caught by validateInputFields, but good for safety
            showError("Data format error during add. Check all number fields.");
        } catch (Exception ex) {
            showError("A critical error occurred during final data processing: " + ex.getMessage());
        }
    }

    private void handleBatchLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select movie data file (text)");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String result = manager.loadBatchData(f.getAbsolutePath());
            JOptionPane.showMessageDialog(this, result, "Batch Load", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        }
    }

    private void handleDeleteSelected() {
        int sel = movieTable.getSelectedRow();
        if (sel < 0) { showError("Select a row to delete."); return; }
        String title = (String) tableModel.getValueAt(sel, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "DELETE movie \"" + title + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (manager.removeMovie(title)) {
                showInfo("Movie removed.");
                refreshTable();
            } else {
                showError("Could not remove movie.");
            }
        }
    }

    private void populateFieldsFromSearch() {
        String title = searchTitleField.getText().trim();
        if (title.isEmpty()) { showError("Enter a title to find."); return; }

        MarvelMovie m = manager.findMovieByTitle(title);
        if (m == null) {
            showError("Movie not found.");
            return;
        }

        titleField.setText(m.getTitle());
        dateField.setText(m.getReleaseDate());
        phaseField.setText(String.valueOf(m.getPhase()));
        directorField.setText(m.getDirector());
        runtimeField.setText(String.valueOf(m.getRunningTimeMin()));
        ratingField.setText(String.valueOf(m.getImdbRating()));
        movieBeingEdited = m;

        showInfo("Movie found. Edit fields and click 'APPLY UPDATE' to save changes.");
    }

    private void handleUpdate() {
        if (movieBeingEdited == null) {
            showError("No movie selected for editing. Use Find first.");
            return;
        }

        String validationErrors = validateInputFields();

        if (!validationErrors.isEmpty()) {
            showValidationErrors(validationErrors);
            return;
        }

        try {
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            int phase = Integer.parseInt(phaseField.getText().trim());
            String director = directorField.getText().trim();
            int runtime = Integer.parseInt(runtimeField.getText().trim());
            double rating = Double.parseDouble(ratingField.getText().trim());

            boolean ok = manager.updateMovieField(movieBeingEdited, "title", title)
                    && manager.updateMovieField(movieBeingEdited, "releaseDate", date)
                    && manager.updateMovieField(movieBeingEdited, "phase", phase)
                    && manager.updateMovieField(movieBeingEdited, "director", director)
                    && manager.updateMovieField(movieBeingEdited, "runningTimeMin", runtime)
                    && manager.updateMovieField(movieBeingEdited, "imdbRating", rating);

            if (ok) {
                showInfo("Movie updated successfully.");
                movieBeingEdited = null;
                clearInputFields();
                refreshTable();
            } else {
                showError("Update failed. The movie manager returned false (e.g., failed to find or save).");
            }
        } catch (Exception ex) {
            showError("A critical error occurred during update processing.");
        }
    }

    // ---------- VALIDATION UTILITY METHODS (RATING CHECK ALIGNED with 1.0) ----------

    private boolean isValidDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateStr.trim(), formatter);

            // Year Range Constraint: 1900-2025
            int year = date.getYear();
            return (year >= 1900 && year <= 2025);

        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String validateInputFields() {
        StringBuilder errorMessages = new StringBuilder();

        String title = titleField.getText().trim();
        String date = dateField.getText().trim();
        String phaseStr = phaseField.getText().trim();
        String director = directorField.getText().trim();
        String runtimeStr = runtimeField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        // 1. Title Check (Required)
        if (title.isEmpty()) {
            errorMessages.append(" • Title cannot be empty.\n");
        }

        // 2. Director Check (Required and must look like a name)
        if (director.isEmpty()) {
            errorMessages.append(" • Director name is required.\n");
        }
        // Constraint Check: Must start and end with a letter, and contain only name-friendly characters.
        else if (director.length() < 2 || !director.matches("^[a-zA-Z][a-zA-Z\\s'-]*[a-zA-Z]$")) {
            errorMessages.append(" • Director name is invalid. Please use only letters, spaces, hyphens, or apostrophes, and ensure it looks like a proper name. **Check format.**\n");
        }

        // 3. Release Date Check (Specific Format and Range)
        if (!isValidDateFormat(date)) {
            errorMessages.append(" • Release Date is invalid. **Check format (YYYY-MM-DD) AND range (1900-2025).**\n");
        }

        // 4. Phase Check (Integer and Positive)
        if (phaseStr.isEmpty()) {
            errorMessages.append(" • Phase cannot be empty.\n");
        } else {
            try {
                int phase = Integer.parseInt(phaseStr);
                if (phase <= 0) {
                    errorMessages.append(" • Phase must be a positive whole number. **Check format (integer).**\n");
                }
            } catch (NumberFormatException ex) {
                errorMessages.append(" • Phase must be a valid whole number. **Check format (integer).**\n");
            }
        }

        // 5. Runtime Check (Integer and Range: 30-300)
        if (runtimeStr.isEmpty()) {
            errorMessages.append(" • Runtime cannot be empty.\n");
        } else {
            try {
                int runtime = Integer.parseInt(runtimeStr);
                // Constraint Check: must be between 30 and 300 minutes, inclusive.
                if (runtime < 30 || runtime > 300) {
                    errorMessages.append(" • Runtime must be between 30 and 300 minutes. **Check range (integer).**\n");
                }
            } catch (NumberFormatException ex) {
                errorMessages.append(" • Runtime must be a whole number (minutes). **Check format (integer).**\n");
            }
        }

        // 6. Rating Check (Numeric and Range: 1.0-10.0)
        if (ratingStr.isEmpty()) {
            errorMessages.append(" • IMDb Rating cannot be empty.\n");
        } else {
            try {
                double rating = Double.parseDouble(ratingStr);
                // Constraint Check: MUST be between 1.0 and 10.0, inclusive.
                if (rating < 1.0 || rating > 10.0) {
                    // ERROR MESSAGE ALIGNED WITH 1.0
                    errorMessages.append(" • IMDb Rating must be between **1.0 and 10.0**. **Check range/format.**\n");
                }
            } catch (NumberFormatException ex) {
                errorMessages.append(" • IMDb Rating must be a numeric value (e.g., 8.5). **Check format (decimal number).**\n");
            }
        }

        return errorMessages.toString();
    }

    // ------------------------------------------------------------------

    private void showValidationErrors(String msg) {
        JOptionPane.showMessageDialog(
                this,
                "Please correct the following input errors:\n" + msg,
                "INPUT VALIDATION FAILED",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (manager != null && manager.getMovies() != null) {
            for (MarvelMovie m : manager.getMovies()) {
                tableModel.addRow(new Object[]{
                        m.getTitle(), m.getReleaseDate(), m.getPhase(),
                        m.getDirector(), m.getRunningTimeMin(), m.getImdbRating()
                });
            }
        }
    }

    private void clearInputFields() {
        titleField.setText("");
        dateField.setText("");
        phaseField.setText("");
        directorField.setText("");
        runtimeField.setText("");
        ratingField.setText("");
        searchTitleField.setText("");
        movieBeingEdited = null;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "ERROR: MCU DATA FAILURE", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "MCU DATA SUCCESS", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // LAUNCH THE INTRO SCREEN FIRST
            IntroScreen intro = new IntroScreen();
            intro.setVisible(true);
        });
    }
}
