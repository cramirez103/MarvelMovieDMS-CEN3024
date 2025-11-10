import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.io.File; // Needed for path validation
import java.sql.Connection; // Needed for connection test

/**
 * IntroScreen - Simple, direct Welcome Screen for DMSGui.
 * Uses pure Swing graphics with a bold Marvel theme.
 */
public class IntroScreen extends JFrame {

    private static final Color MARVEL_RED = new Color(190, 30, 45); // Primary Branding Red
    private static final Color LIGHT_BG = new Color(245, 245, 245); // Near-white background

    // Custom Fonts
    private static final Font WELCOME_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.ITALIC, 16);
    private static final Font BUTTON_FONT = new Font("Dialog", Font.BOLD, 18);

    public IntroScreen() {
        setTitle("M.C.U. Data Access Terminal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(LIGHT_BG);
        setLayout(new BorderLayout());

        // --- 1. TITLE PANEL ---
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(LIGHT_BG);
        titlePanel.setBorder(new EmptyBorder(60, 0, 30, 0));

        // Main Welcome Text
        JLabel welcomeLabel = new JLabel("WELCOME TO THE");
        welcomeLabel.setFont(SUBTITLE_FONT);
        welcomeLabel.setForeground(Color.DARK_GRAY);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // System Title
        JLabel systemLabel = new JLabel("MARVEL DMS");
        systemLabel.setFont(WELCOME_FONT);
        systemLabel.setForeground(MARVEL_RED);
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(systemLabel);

        add(titlePanel, BorderLayout.CENTER);

        // --- 2. BUTTON PANEL ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(LIGHT_BG);
        southPanel.setBorder(new EmptyBorder(0, 0, 50, 0));

        JButton beginButton = new JButton("BEGIN ACCESS");
        beginButton.setFont(BUTTON_FONT);
        beginButton.setBackground(MARVEL_RED);
        beginButton.setForeground(Color.WHITE);
        beginButton.setFocusPainted(false);
        beginButton.setPreferredSize(new Dimension(200, 50));

        // Action Listener
        beginButton.addActionListener(e -> {
            // NOTE: The database path MUST be set BEFORE this button is enabled/clicked.
            // For now, we assume the static main() has already confirmed the setup.

            // 1. Dispose the intro screen
            dispose();

            // 2. Launch the main application GUI
            SwingUtilities.invokeLater(() -> {
                // Since the DB path is set in main, DMSGui can now be created.
                DMSGui gui = new DMSGui();
                gui.setVisible(true);
            });
        });

        southPanel.add(beginButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    // --- NEW STATIC MAIN METHOD TO HANDLE DATABASE SETUP ---
    public static void main(String[] args) {
        String dbFilePath = null;
        boolean pathValidated = false;

        // Loop until a valid path is provided or the user cancels
        while (!pathValidated) {
            // 1. Prompt the user for the database file path
            dbFilePath = JOptionPane.showInputDialog(
                    null,
                    "<html><b>Database Connection Required:</b><br>Please enter the ABSOLUTE path to your SQLite database file (e.g., C:\\sqlite\\myproject.db):</html>",
                    "Database Connection Setup",
                    JOptionPane.QUESTION_MESSAGE
            );

            // Check if the user clicked 'Cancel' or closed the dialog
            if (dbFilePath == null) {
                JOptionPane.showMessageDialog(null, "Application setup canceled by user. Exiting.", "Exiting", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }

            // 2. Validate the path exists
            File dbFile = new File(dbFilePath);
            if (dbFile.exists() && dbFile.isFile()) {
                pathValidated = true;
            } else {
                // Show an error message and loop again
                JOptionPane.showMessageDialog(
                        null,
                        "Error: The specified file path is invalid or the file does not exist.\nEnsure the path is absolute and the file is present.",
                        "Path Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // 3. Set the path in the JDBC helper
        JDBC.setDatabasePath(dbFilePath);

        // 4. Test the connection immediately for robustness
        Connection con = JDBC.openConnection();
        if (con != null) {
            JDBC.closeConnection(con);
            System.out.println("Database connection test successful.");

            // 5. If connection is successful, launch the UI thread
            SwingUtilities.invokeLater(() -> {
                IntroScreen intro = new IntroScreen();
                intro.setVisible(true);
            });
        } else {
            // Connection failed even with a valid file path (e.g., DB file is corrupt or driver issue)
            JOptionPane.showMessageDialog(null, "FATAL: Could not establish a database connection after setup. Check your SQLite JAR or file permissions. Exiting.", "Connection Failure", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}