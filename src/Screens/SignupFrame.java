package Screens;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.time.LocalDateTime;

import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.RoundedPasswordField;
import MaterialSwingUI.MaterialField;
import Repository.ImageDatabaseHandler;
import Repository.UserRepository;
import Repository.Utilities;
import Components.*;
/**
 * Represents the Sign Up window of the Club Connect application.
 * This frame allows new users to register an account, including providing
 * personal details, a password, and optionally uploading a profile picture.
 */
class SignupFrame extends JFrame {
    private final ImageDatabaseHandler imageHander;
    private File profile_pic;
    private final JButton uploadLogoButton;

    /**
     * Constructs the signup frame, initializing the UI components and data handlers.
     *
     * @param conn The active database connection used for user registration and image handling.
     */
    public SignupFrame(Connection conn) {
        setTitle("Club Connect - Sign Up");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        this.imageHander = new ImageDatabaseHandler(conn);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel (Dark gray) - Welcome Back section
        JPanel leftPanel = getLeftPanel(68, 440);

        // Welcome Back text
        JLabel welcomeLabel = new JLabel("Welcome Back Friend !");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 34));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(40, 310, 400, 50);
        leftPanel.add(welcomeLabel);

        // Login button
        JButton loginButton = new MaterialButton("Login", new Color(68, 68, 68));
        loginButton.setBounds(110, 390, 220, 50);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(loginButton);

        // Add action listener to switch to loginscreen
        loginButton.addActionListener(e -> {
            new LoginFrame(conn).setVisible(true);
            dispose();
        });

        // Right panel (Light gray) - Create Account section
        JPanel rightPanel = getLeftPanel(240, 660);

        // Logo panel with circle
        LogoPanel logoPanel = new LogoPanel();
        logoPanel.setBounds(240, 60, 180, 180);
        rightPanel.add(logoPanel);

        // Create New Account label
        JLabel signupLabel = new JLabel("Create New Account");
        signupLabel.setFont(new Font("Arial", Font.BOLD, 38));
        signupLabel.setForeground(new Color(40, 40, 40));
        signupLabel.setBounds(145, 250, 450, 50);
        rightPanel.add(signupLabel);

        // Fullnames field
        JTextField fullnamesField = new MaterialField("Fullnames", 20);
        fullnamesField.setBounds(155, 320, 350, 45);
        rightPanel.add(fullnamesField);

        // Email field
        JTextField emailField = new MaterialField("Email", 20);
        emailField.setBounds(155, 380, 350, 45);
        rightPanel.add(emailField);

        // Username field
        JTextField usernameField = new MaterialField("Username", 20);
        usernameField.setBounds(155, 440, 350, 45);
        rightPanel.add(usernameField);

        // Password field
        JPasswordField passwordField = new RoundedPasswordField("Password", 20);
        passwordField.setBounds(155, 500, 350, 45);
        rightPanel.add(passwordField);

        // Profile Picture field
        uploadLogoButton = new MaterialButton("Upload Photo", new Color(50,90,150));
        uploadLogoButton.addActionListener(e -> selectLogo());
        uploadLogoButton.setBounds(155,560,350,45);
        rightPanel.add(uploadLogoButton);

        // Create Account button
        JButton createAccountButton = new MaterialButton("Create Account", Color.BLACK);
        createAccountButton.setBounds(250, 630, 170, 40);
        createAccountButton.setForeground(Color.WHITE);
        createAccountButton.setFont(new Font("Arial", Font.BOLD, 16));
        rightPanel.add(createAccountButton);

        /**
         * Sets up the action listener for the "Create Account" button.
         * It validates input fields, creates a new {@code User} object, and attempts to add it
         * to the database asynchronously. It also handles the profile picture upload.
         */
        createAccountButton.addActionListener(e -> {
            if (fullnamesField.getText().isEmpty() || emailField.getText().isEmpty()
                    || usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields");
                return;
            }

            if (!Utilities.isValidText(fullnamesField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Enter valid names");
                return;
            }

            if (!Utilities.isValidEmail(emailField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Enter valid email address");
                return;
            }

            User newUser = new User(
                    (long) LocalDateTime.now().getNano(),
                    fullnamesField.getText().trim(),
                    emailField.getText().trim(),
                    usernameField.getText().trim(),
                    Utilities.hashPassword(passwordField.getText().trim()),
                    4,
                    "Active",
                    String.valueOf(LocalDateTime.now())
            );

            UserRepository repo = new UserRepository(conn);

            /**
             * Asynchronously adds the new user to the database.
             */
            repo.addUser(newUser).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        /**
                         * If user creation is successful, attempt to save the profile picture.
                         */
                        boolean result = imageHander.saveUserProfilePicture(newUser.userId(), profile_pic);

                        if (result) {
                            JOptionPane.showMessageDialog(this, "User created successfully!");
                            new DashboardFrame(conn, newUser);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "User created, but profile picture not uploaded!");

                            /**
                             * If profile picture upload fails, retrieve the user to get a fully populated model
                             * (e.g., if the DB auto-generated registration date/status) and proceed to dashboard.
                             */
                            repo.getUserById(newUser.userId()).thenAccept(optional -> {
                                SwingUtilities.invokeLater(() -> {
                                    if (optional.isPresent()) {
                                        new DashboardFrame(conn, optional.get()).setVisible(true);
                                        dispose();
                                    } else {
                                        JOptionPane.showMessageDialog(this, "Something went wrong");
                                    }
                                });
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "User creation failed!");
                    }
                });
            });
        });


        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    /**
     * Opens a {@code JFileChooser} dialog to allow the user to select an image file
     * for their profile picture. Updates the button text upon successful selection.
     */
    private void selectLogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            profile_pic = fileChooser.getSelectedFile();
            uploadLogoButton.setText("Logo Selected");
        }
    }

    /**
     * Creates a custom {@code JPanel} with a solid background color for the side panels.
     *
     * @param r The red/color intensity component (used for gray color: r, r, r).
     * @param width The preferred width of the panel.
     * @return The configured {@code JPanel}.
     */
    private static JPanel getLeftPanel(int r, int width) {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(r, r, r));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setLayout(null);
        leftPanel.setPreferredSize(new Dimension(width, 720));
        return leftPanel;
    }
}