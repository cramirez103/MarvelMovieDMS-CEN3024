import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

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
            // 1. Dispose the intro screen
            dispose();

            // 2. Launch the main application
            SwingUtilities.invokeLater(() -> {
                DMSGui gui = new DMSGui();
                gui.setVisible(true);
            });
        });

        southPanel.add(beginButton);
        add(southPanel, BorderLayout.SOUTH);
    }
}
