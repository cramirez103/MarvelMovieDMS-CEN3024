// DMSGui.java
// Simple Swing GUI for the Marvel Movie DMS
// Put this in the same package as your other classes (or default package)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * DMSGui - Swing GUI wrapper for MovieManager.
 * Implements: Display, Create (manual), Batch load (file chooser), Update, Delete, Custom action.
 */
public class DMSGui extends JFrame {
    private final MovieManager manager;

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

    public DMSGui() {
        super("Marvel Movie DMS (GUI)"); // window title
        this.manager = new MovieManager();

        // Table setup
        String[] cols = {"Title", "Release Date", "Phase", "Director", "Runtime (min)", "IMDb"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        movieTable = new JTable(tableModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(movieTable), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setSize(900, 600);
        setLocationRelativeTo(null); // center
        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // Left: Create form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add New Movie"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("Title:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(titleField, c);
        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("Release Date:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(dateField, c);
        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("Phase:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(phaseField, c);
        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("Director:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(directorField, c);
        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("Runtime (min):"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(runtimeField, c);
        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; form.add(new JLabel("IMDb Rating:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; form.add(ratingField, c);

        JButton addBtn = new JButton("Add Movie");
        addBtn.addActionListener(e -> handleAdd());
        c.gridx = 1; c.gridy++; c.anchor = GridBagConstraints.CENTER; form.add(addBtn, c);

        // Right: Controls
        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createTitledBorder("Controls"));
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        // Batch load
        JButton batchBtn = new JButton("Batch Load (File)");
        batchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        batchBtn.addActionListener(e -> handleBatchLoad());
        controls.add(batchBtn);
        controls.add(Box.createVerticalStrut(8));

        // Display / refresh
        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> refreshTable());
        controls.add(refreshBtn);
        controls.add(Box.createVerticalStrut(8));

        // Delete
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.addActionListener(e -> handleDeleteSelected());
        controls.add(deleteBtn);
        controls.add(Box.createVerticalStrut(8));

        // Update area
        JPanel updatePanel = new JPanel();
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Selected"));
        updatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        updatePanel.add(new JLabel("Search Title:"));
        updatePanel.add(searchTitleField);
        JButton findBtn = new JButton("Find");
        findBtn.addActionListener(e -> populateFieldsFromSearch());
        updatePanel.add(findBtn);
        JButton updateBtn = new JButton("Apply Update");
        updateBtn.addActionListener(e -> handleUpdateField());
        updatePanel.add(updateBtn);

        // Custom action (average rating)
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customPanel.setBorder(BorderFactory.createTitledBorder("Custom Action"));
        customPanel.add(new JLabel("Phase:"));
        JTextField phaseInput = new JTextField(4);
        customPanel.add(phaseInput);
        JButton avgBtn = new JButton("Calc Avg Rating");
        avgBtn.addActionListener(e -> {
            try {
                int ph = Integer.parseInt(phaseInput.getText().trim());
                double avg = manager.calculateAverageRating(ph);
                if (avg > 0.0) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Average IMDb rating for Phase %d: %.2f", ph, avg),
                            "Average Rating", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No movies found in that phase.", "Average Rating",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid integer for phase.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        customPanel.add(avgBtn);

        // assemble right column
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
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
        JLabel help = new JLabel("Select a row to populate fields for quick edits. Use Batch Load to upload a .txt file.");
        p.add(help, BorderLayout.CENTER);
        return p;
    }

    // ---------- Handlers ----------

    private void handleAdd() {
        String title = titleField.getText().trim();
        String date = dateField.getText().trim();
        String phaseStr = phaseField.getText().trim();
        String director = directorField.getText().trim();
        String runtimeStr = runtimeField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        // Basic parsing + validation messages
        int phase; int runtime; double rating;
        try {
            phase = Integer.parseInt(phaseStr);
        } catch (Exception ex) { showError("Phase must be a whole number."); return; }

        try {
            runtime = Integer.parseInt(runtimeStr);
        } catch (Exception ex) { showError("Runtime must be a whole number (minutes)."); return; }

        try {
            rating = Double.parseDouble(ratingStr);
        } catch (Exception ex) { showError("IMDb rating must be a number (1.0 - 10.0)."); return; }

        // call manager's addMovie (manager will reject invalid inputs)
        boolean ok = manager.addMovie(title, date, phase, director, runtime, rating);
        if (ok) {
            showInfo("Movie added successfully.");
            clearInputFields();
            refreshTable();
        } else {
            showError("Failed to add movie. Check for duplicates or invalid values (date format YYYY-MM-DD, date range, runtime, rating).");
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
        int confirm = JOptionPane.showConfirmDialog(this, "Delete movie \"" + title + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
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
        if (m == null) { showError("Movie not found."); return; }
        // populate input fields for editing convenience
        titleField.setText(m.getTitle());
        dateField.setText(m.getReleaseDate());
        phaseField.setText(String.valueOf(m.getPhase()));
        directorField.setText(m.getDirector());
        runtimeField.setText(String.valueOf(m.getRunningTimeMin()));
        ratingField.setText(String.valueOf(m.getImdbRating()));
        showInfo("Fields populated. Edit and click Add to attempt update (or use Apply Update).");
    }

    private void handleUpdateField() {
        // This will update selected row's movie by title using whatever fields are present
        String titleSearch = searchTitleField.getText().trim();
        if (titleSearch.isEmpty()) { showError("Enter the title of the movie to update in Search box."); return; }
        MarvelMovie movie = manager.findMovieByTitle(titleSearch);
        if (movie == null) { showError("Movie not found."); return; }

        // We will ask user which field to update using a simple dropdown
        String[] fields = {"title","releaseDate","phase","director","runningTimeMin","imdbRating"};
        String field = (String) JOptionPane.showInputDialog(this, "Choose field to update:", "Update Field",
                JOptionPane.QUESTION_MESSAGE, null, fields, fields[0]);
        if (field == null) return; // cancelled

        Object newValue = null;
        try {
            switch (field) {
                case "title":
                case "director":
                    newValue = JOptionPane.showInputDialog(this, "Enter new value for " + field + ":");
                    if (newValue == null) return;
                    break;
                case "releaseDate":
                    String date = JOptionPane.showInputDialog(this, "Enter new Release Date (YYYY-MM-DD):");
                    if (date == null) return;
                    newValue = date.trim();
                    break;
                case "phase":
                    String pStr = JOptionPane.showInputDialog(this, "Enter new Phase (integer):");
                    if (pStr == null) return;
                    newValue = Integer.parseInt(pStr.trim());
                    break;
                case "runningTimeMin":
                    String rtStr = JOptionPane.showInputDialog(this, "Enter new Running Time (minutes):");
                    if (rtStr == null) return;
                    newValue = Integer.parseInt(rtStr.trim());
                    break;
                case "imdbRating":
                    String rStr = JOptionPane.showInputDialog(this, "Enter new IMDb Rating (1.0-10.0):");
                    if (rStr == null) return;
                    newValue = Double.parseDouble(rStr.trim());
                    break;
            }
        } catch (NumberFormatException ex) {
            showError("Invalid number format for the chosen field.");
            return;
        }

        boolean ok = manager.updateMovieField(movie, field, newValue);
        if (ok) {
            showInfo("Update successful.");
            refreshTable();
        } else {
            showError("Update failed. Check value validity (date format/range, runtime range, rating).");
        }
    }

    // ---------- Utility UI methods ----------
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (MarvelMovie m : manager.getMovies()) {
            Object[] row = {m.getTitle(), m.getReleaseDate(), m.getPhase(), m.getDirector(), m.getRunningTimeMin(), m.getImdbRating()};
            tableModel.addRow(row);
        }
    }

    private void clearInputFields() {
        titleField.setText("");
        dateField.setText("");
        phaseField.setText("");
        directorField.setText("");
        runtimeField.setText("");
        ratingField.setText("");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DMSGui gui = new DMSGui();
            gui.setVisible(true);
        });
    }
}
