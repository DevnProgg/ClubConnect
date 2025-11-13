package Render;

import Models.User;
import MaterialSwingUI.*;
import Repository.ImageDatabaseHandler;
import Repository.UserRepository;
import Repository.Utilities;
import Components.UserCarousel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
/**
 * Renders the main dashboard page for managing and viewing user information.
 * This class extends {@code PageStructure} to utilize a predefined layout
 * and populates its sections with metrics, a user creation form, a bar chart,
 * and a user carousel.
 */
public class UsersRender extends PageStructure {

    private JTextField nameField;
    private JTextField emailField;
    private JTextArea PasswordArea;
    private JComboBox<String> statusComboBox;
    private JTextField usernameField;
    private JButton uploadLogoButton;
    private File selectedLogoPath;
    private List<User> users;
    private final Connection conn;
    private ImageDatabaseHandler ih;
    private final User user;

    /**
     * Constructs a {@code UsersRender} page.
     * Initializes database handlers and asynchronously fetches the list of users
     * to populate the UI components upon completion.
     *
     * @param conn The active database connection.
     * @param user The currently logged-in {@code User}
     */
    public UsersRender(Connection conn, User user) {
        this.conn = conn;
        this.user = user;
        UserRepository userRepo = new UserRepository(conn);

        /**
         * Asynchronously fetches all users from the database.
         * On success, it initializes the UI components (metrics, form, charts, carousel)
         * with the fetched data. Handles the case where no users are available.
         */
        userRepo.getAllUsers().thenAcceptAsync(listOfUsers->{
            if(listOfUsers.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get users or Users are not available");
                    users = new ArrayList<>();
                    // Setup metrics in top panels
                    setupMetrics(users);

                    // Setup create club form in large panel
                    setupCreateUserForm();

                    // Setup bar chart in middle panel
                    setupBarChart(users);

                    // Setup club carousel in bottom panel
                    setupUserCarousel(users);
                });
            }else{
                users =listOfUsers;
            }
            // Setup metrics in top panels
            setupMetrics(users);

            // Setup create club form in large panel
            setupCreateUserForm();

            // Setup bar chart in middle panel
            setupBarChart(users);

            // Setup club carousel in bottom panel
            setupUserCarousel(users);
        });
    }

    /**
     * Populates the top-left, top-center, and top-right sections with key user metrics
     * (Total Users, Active Users, Inactive Users) derived from the list of users.
     *
     * @param users The list of {@code User} objects used to calculate metrics.
     */
    private void setupMetrics(List<User> users) {
        // Top Left - Total Clubs Metric
        JPanel totalMetric = createMetricPanel("Total users", String.valueOf(users.size()),
                new Color(52, 152, 219));
        topLeft.add(totalMetric);

        // Top Center - Active Clubs Metric
        long activeCount = users.stream()
                .filter(u -> "active".equalsIgnoreCase(u.status()))
                .count();
        JPanel activeMetric = createMetricPanel("Active Users", String.valueOf(activeCount),
                new Color(46, 204, 113));
        topCenter.add(activeMetric);

        // Top Right - Pending Approval Metric
        long pendingCount = users.stream()
                .filter(u -> "inactive".equalsIgnoreCase(u.status()))
                .count();
        JPanel pendingMetric = createMetricPanel("Inactive Users", String.valueOf(pendingCount),
                new Color(241, 196, 15));
        topRight.add(pendingMetric);
    }

    /**
     * Creates a standardized panel for displaying a single metric value and its label.
     *
     * @param label The descriptive text for the metric.
     * @param value The numerical value of the metric, displayed prominently.
     * @param accentColor The color used for the value display.
     * @return A styled {@code JPanel} ready for display in a metric section.
     */
    private JPanel createMetricPanel(String label, String value, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        valueLabel.setForeground(accentColor);

        JLabel titleLabel = new JLabel(label, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sets up the user creation form in the {@code rightLarge} panel.
     * This form includes fields for user details, password, and a button to upload a profile logo.
     */
    private void setupCreateUserForm() {
        rightLarge.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Create New User", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        // User's Names
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Fullnames :"), gbc);
        gbc.gridy = 1;
        nameField = createTextField();
        formPanel.add(nameField, gbc);

        // Category
        gbc.gridy = 2;
        formPanel.add(createLabel("Email:"), gbc);
        gbc.gridy = 3;
        emailField = createTextField();
        formPanel.add(emailField, gbc);

        // Status
        gbc.gridy = 4;
        formPanel.add(createLabel("Status:"), gbc);
        gbc.gridy = 5;
        statusComboBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        styleComboBox(statusComboBox);
        formPanel.add(statusComboBox, gbc);

        // Description
        gbc.gridy = 6;
        formPanel.add(createLabel("Password:"), gbc);
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        PasswordArea = new JTextArea(4, 20);
        PasswordArea.setLineWrap(true);
        PasswordArea.setWrapStyleWord(true);
        PasswordArea.setBackground(new Color(50, 50, 50));
        PasswordArea.setForeground(Color.WHITE);
        PasswordArea.setCaretColor(Color.WHITE);
        PasswordArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        PasswordArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane descScroll = new MaterialScrollPane(PasswordArea);
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        formPanel.add(descScroll, gbc);

        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 8;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridy = 9;
        usernameField = createTextField();
        formPanel.add(usernameField, gbc);

        // Logo Upload Button
        gbc.gridy = 10;
        uploadLogoButton = new MaterialButton("Upload Logo", new Color(70,15,20));
        uploadLogoButton.addActionListener(e -> selectLogo());
        formPanel.add(uploadLogoButton, gbc);

        // Create Button
        gbc.gridy = 11;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton createButton = new MaterialButton("Create User", new Color(46, 204, 113));
        createButton.addActionListener(e -> createUser());
        formPanel.add(createButton, gbc);

        JScrollPane scrollPane = new MaterialScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        rightLarge.add(titleLabel, BorderLayout.NORTH);
        rightLarge.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a styled {@code JLabel} for use as a form field label.
     *
     * @param text The text for the label.
     * @return A styled {@code JLabel}.
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    /**
     * Creates a styled {@code JTextField} (using {@code MaterialField}) for form input.
     *
     * @return A styled {@code JTextField}.
     */
    private JTextField createTextField() {
        JTextField field = new MaterialField();
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    /**
     * Applies a dark theme style to a {@code JComboBox}.
     *
     * @param comboBox The {@code JComboBox} to style.
     */
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(new Color(50, 50, 50));
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
    }


    /**
     * Opens a {@code JFileChooser} to allow the user to select an image file for the user profile logo.
     * Updates the {@code uploadLogoButton} text upon successful selection.
     */
    private void selectLogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedLogoPath = fileChooser.getSelectedFile();
            uploadLogoButton.setText("Logo Selected");
        }
    }

    /**
     * Handles the creation of a new user: validates form inputs, creates a {@code User} object,
     * sends it to the database via {@code UserRepository}, and attempts to save the profile picture.
     * Displays a dialog box with the result and refreshes the user list.
     */
    private void createUser() {
        // Validate inputs
        if (nameField.getText().trim().isEmpty() || emailField.getText().isEmpty() || PasswordArea.getText().isEmpty() || usernameField.getText().isEmpty() ) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        /**
         * Validates the format of the full names and email address using {@code Utilities} methods.
         */
        if (!(Utilities.isValidText(nameField.getText()) || Utilities.isValidEmail(emailField.getText()))){
            JOptionPane.showMessageDialog(this, "Please enter valid inputs for email and fullnames");
        }

        // Create user object
        User newUser = new User(
                LocalDateTime.now().getNano(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                usernameField.getText().trim(),
                PasswordArea.getText().trim(),
                4,
                Objects.requireNonNull(statusComboBox.getSelectedItem()).toString().toLowerCase(),
                java.time.LocalDate.now().toString()
        );
        UserRepository repo = new UserRepository(conn);

        /**
         * Asynchronously adds the new user to the database and attempts to save the profile picture.
         */
        repo.addUser(newUser).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    boolean result = ih.saveUserProfilePicture(newUser.userId(), selectedLogoPath);

                    if (result) {
                        JOptionPane.showMessageDialog(this, "User created successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "User created, but profile picture not uploaded!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User creation failed!");
                }
            });
        });
        UserRepository userRepo = new UserRepository(conn);

        /**
         * Triggers a refresh of the user list to update the UI components after creation.
         */
        userRepo.getAllUsers().thenAcceptAsync(listOfUsers->{
            if(listOfUsers.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get users or Users are not available");
                });
            }else{
                users =listOfUsers;
            }
        });
        clearForm();
    }

    /**
     * Clears all input fields in the user creation form and resets the status dropdown.
     */
    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        PasswordArea.setText("");
        usernameField.setText("");
        statusComboBox.setSelectedIndex(0);
        uploadLogoButton.setText("Upload Logo");
    }

    /**
     * Sets up a bar chart in the {@code middle} panel showing user creation trends over the year.
     *
     * @param users The list of {@code User} objects (currently uses mock data for the chart).
     */
    private void setupBarChart(List<User> users) {
        middle.setLayout(new BorderLayout());

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] userCounts = {3, 5, 4, 7, 6, 8, 5, 9, 7, 6, 8, 10};

        MaterialBarChart chartPanel = new MaterialBarChart("Users Created by Month", months, userCounts);

        middle.add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Sets up the {@code UserCarousel} in the {@code bottom} panel to display a navigable list of users.
     * Displays a "No Users available" message if the user list is empty.
     *
     * @param users The list of {@code User} objects to display.
     */
    private void setupUserCarousel(List<User> users) {
        bottom.setLayout(new BorderLayout());

        if (users.isEmpty()) {
            JLabel noClubsLabel = new JLabel("No Users available", SwingConstants.CENTER);
            noClubsLabel.setForeground(Color.LIGHT_GRAY);
            noClubsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            bottom.add(noClubsLabel, BorderLayout.CENTER);
        } else {
            UserCarousel carousel = new UserCarousel(users, conn, ih, this.user);
            bottom.add(carousel, BorderLayout.CENTER);
        }
    }

}