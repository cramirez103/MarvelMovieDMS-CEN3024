import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class DMSGui extends JFrame {
    private final MovieManager manager;
    private final JTable movieTable;
    private final DefaultTableModel tableModel;

    // Input fields
    private final JTextField titleField = new JTextField(20);
    private final JTextField dateField = new JTextField(10);
    private final JTextField phaseField = new JTextField(4);
    private final JTextField directorField = new JTextField(15);
    private final JTextField runtimeField = new JTextField(5);
    private final JTextField ratingField = new JTextField(4);
    private final JTextField searchTitleField = new JTextField(15);

    public DMSGui() {
        manager = new MovieManager();
        setTitle("Marvel Movie Database System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLayout(new BorderLayout(8, 8));

        // ===== Table =====
        String[] cols = {"Title", "Release Date", "Phase", "Director", "Running Time", "IMDb Rating"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        movieTable.setRowHeight(20);
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(220); // Title column wider
        JScrollPane scrollPane = new JScrollPane(movieTable);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Top Panel (Add + Controls + Update + Custom) =====
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(buildAddPanel(), BorderLayout.WEST);
        topPanel.add(buildRightControlsPanel(), BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ===== Bottom Help Panel =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        JLabel help = new JLabel("Select a row to populate fields for quick edits. Use Batch Load to upload a .txt file.");
        bottomPanel.add(help, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildAddPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add New Movie"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.anchor = GridBagConstraints.EAST;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Title:"), c);
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
        addBtn.addActionListener(e -> addMovie());
        c.gridx = 1; c.gridy++; c.anchor = GridBagConstraints.CENTER; form.add(addBtn, c);

        return form;
    }

    private JPanel buildRightControlsPanel() {
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setBackground(new Color(240, 248, 255)); // soft background color

        // Controls panel
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> refreshTable());
        JButton batchBtn = new JButton("Batch Load (File)");
        batchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        batchBtn.addActionListener(e -> batchLoad());
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.addActionListener(e -> deleteSelected());

        controls.add(refreshBtn); controls.add(Box.createVerticalStrut(6));
        controls.add(batchBtn); controls.add(Box.createVerticalStrut(6));
        controls.add(deleteBtn); controls.add(Box.createVerticalStrut(6));

        // Update panel
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Selected"));
        updatePanel.add(new JLabel("Search Title:"));
        updatePanel.add(searchTitleField);
        JButton findBtn = new JButton("Find");
        findBtn.addActionListener(e -> populateFieldsFromSearch());
        JButton updateBtn = new JButton("Apply Update");
        updateBtn.addActionListener(e -> updateMovieField());
        updatePanel.add(findBtn);
        updatePanel.add(updateBtn);

        // Custom panel
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
                if (avg > 0) {
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

        rightCol.add(controls);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(updatePanel);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(customPanel);

        return rightCol;
    }

    // ---------- Handlers ----------
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (MarvelMovie m : manager.getMovies()) {
            tableModel.addRow(new Object[]{m.getTitle(), m.getReleaseDate(), m.getPhase(), m.getDirector(),
                    m.getRunningTimeMin(), m.getImdbRating()});
        }
    }

    private void addMovie() {
        try {
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            int phase = Integer.parseInt(phaseField.getText().trim());
            String director = directorField.getText().trim();
            int runtime = Integer.parseInt(runtimeField.getText().trim());
            double rating = Double.parseDouble(ratingField.getText().trim());

            if (manager.addMovie(title, date, phase, director, runtime, rating)) {
                JOptionPane.showMessageDialog(this, "Movie added successfully!");
                clearInputFields();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add movie. Check values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int sel = movieTable.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String title = (String) tableModel.getValueAt(sel, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete movie \"" + title + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (manager.removeMovie(title)) { JOptionPane.showMessageDialog(this, "Movie removed."); refreshTable(); }
            else JOptionPane.showMessageDialog(this, "Could not remove movie.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFieldsFromSearch() {
        String title = searchTitleField.getText().trim();
        MarvelMovie m = manager.findMovieByTitle(title);
        if (m == null) { JOptionPane.showMessageDialog(this, "Movie not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        titleField.setText(m.getTitle());
        dateField.setText(m.getReleaseDate());
        phaseField.setText(String.valueOf(m.getPhase()));
        directorField.setText(m.getDirector());
        runtimeField.setText(String.valueOf(m.getRunningTimeMin()));
        ratingField.setText(String.valueOf(m.getImdbRating()));
        JOptionPane.showMessageDialog(this, "Fields populated. Edit and click 'Add Movie' or 'Apply Update'.");
    }

    private void updateMovieField() {
        String titleSearch = searchTitleField.getText().trim();
        MarvelMovie movie = manager.findMovieByTitle(titleSearch);
        if (movie == null) { JOptionPane.showMessageDialog(this, "Movie not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        String[] fields = {"title","releaseDate","phase","director","runningTimeMin","imdbRating"};
        String field = (String) JOptionPane.showInputDialog(this, "Choose field to update:", "Update Field", JOptionPane.QUESTION_MESSAGE, null, fields, fields[0]);
        if (field == null) return;

        Object newValue = JOptionPane.showInputDialog(this, "Enter new value for " + field + ":");
        if (newValue == null) return;

        boolean ok;
        try {
            switch (field) {
                case "phase", "runningTimeMin" -> ok = manager.updateMovieField(movie, field, Integer.parseInt(newValue.toString().trim()));
                case "imdbRating" -> ok = manager.updateMovieField(movie, field, Double.parseDouble(newValue.toString().trim()));
                default -> ok = manager.updateMovieField(movie, field, newValue.toString().trim());
            }
        } catch (Exception e) { ok = false; }

        if (ok) { JOptionPane.showMessageDialog(this, "Update successful."); refreshTable(); }
        else JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void batchLoad() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String result = manager.loadBatchData(f.getAbsolutePath());
            JOptionPane.showMessageDialog(this, result, "Batch Load", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DMSGui::new);
    }
}
