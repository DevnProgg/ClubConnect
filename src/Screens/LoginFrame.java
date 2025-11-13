package Screens;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.RoundedPasswordField;
import MaterialSwingUI.MaterialField;
import Repository.Authenticate;
import Repository.Utilities;
import Components.*;
/**
 * Represents the main Login window of the Club Connect application.
 * This frame provides the user interface for authenticating users via username and password.
 * Successful login transitions the user to the {@code DashboardFrame}.
 */
public class LoginFrame extends JFrame {
    private  User user;

    /**
     * Constructs the login frame, initializing the UI components and setting up layout and listeners.
     *
     * @param conn The active database connection used for authentication logic.
     */
    public LoginFrame(Connection conn) {
        setTitle("Club Connect - Login");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = getLeftPanel(68, 440);

        JLabel welcomeLabel = new JLabel("Hello, Friend !");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 45));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(70, 310, 400, 50);
        leftPanel.add(welcomeLabel);

        JButton createAccountButton = new MaterialButton("Create Account", (new Color(68, 68, 68)));
        createAccountButton.setBounds(110, 390, 220, 50);
        createAccountButton.setForeground(Color.WHITE);
        createAccountButton.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(createAccountButton);

        /**
         * Sets up the action listener for the "Create Account" button to switch to the registration screen.
         */
        createAccountButton.addActionListener(e -> {
            new SignupFrame(conn).setVisible(true);
            dispose();
        });

        JPanel rightPanel = getLeftPanel(240, 660);

        LogoPanel logoPanel = new LogoPanel();
        logoPanel.setBounds(240, 60, 180, 180);
        rightPanel.add(logoPanel);

        JLabel signupLabel = new JLabel("Login");
        signupLabel.setFont(new Font("Arial", Font.BOLD, 38));
        signupLabel.setForeground(new Color(40, 40, 40));
        signupLabel.setBounds(280, 250, 450, 50);
        rightPanel.add(signupLabel);

        JTextField usernameField = new MaterialField("Username", 20);
        usernameField.setBounds(155, 320, 350, 45);
        rightPanel.add(usernameField);

        // Password field
        JPasswordField passwordField = new RoundedPasswordField("Password", 20);
        passwordField.setBounds(155, 380, 350, 45);
        rightPanel.add(passwordField);

        // login
        JButton loginButtonAccount = new MaterialButton("Login", Color.BLACK);
        loginButtonAccount.setBounds(250, 440, 170, 60);
        loginButtonAccount.setForeground(Color.WHITE);
        loginButtonAccount.setFont(new Font("Arial", Font.BOLD, 16));
        rightPanel.add(loginButtonAccount);

        /**
         * Sets up the action listener for the "Login" button.
         * It validates input, initiates asynchronous authentication, and handles the result.
         */
        loginButtonAccount.addActionListener(e -> {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill in all fields");
                return;
            }

            Authenticate authenticator = new Authenticate(conn, usernameField.getText().trim());
            CompletableFuture<Optional<?>> future = authenticator.AuthenticateUser();

            /**
             * Processes the result of the asynchronous authentication.
             * 1. If a user object is returned, verify the password hash.
             * 2. If verification is successful, transition to the dashboard.
             * 3. Otherwise, display an error message.
             */
            future.thenAccept(optional -> {
                if (optional.isPresent()) {
                    user = (User) optional.get();
                    if (Utilities.verifyPassword(passwordField.getText().trim(), user.passwordHash())) {
                        SwingUtilities.invokeLater(() -> {
                            new DashboardFrame(conn, user).setVisible(true);
                            dispose();
                        });
                    } else {
                        JOptionPane.showMessageDialog(this, "Username or Password is wrong");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Username or Password is wrong");
                }
            });

        });



        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
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