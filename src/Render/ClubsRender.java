package Render;

import Models.Club;
import Models.User;
import MaterialSwingUI.*;
import Repository.ClubRepository;
import Repository.ImageDatabaseHandler;
import Repository.Utilities;
import Components.ClubCarousel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Renders the main dashboard page for managing and viewing club information.
 * This class extends {@code PageStructure} to utilize a predefined layout
 * and populates its sections with metrics, a club creation form, a bar chart,
 * and a club carousel.
 */
public class ClubsRender extends PageStructure {

    private JTextField nameField;
    private JTextField categoryField;
    private JTextArea descriptionArea;
    private JTextField budgetProposalField;
    private JTextField memberCapacityField;
    private JComboBox<String> statusComboBox, cmbCategory;
    private JButton uploadLogoButton;
    private File selectedLogoPath ;
    private final Connection conn;
    private List<Club> clubs;
    private final ImageDatabaseHandler ih;
    private  final User user;

    /**
     * Constructs a {@code ClubsRender} page.
     * Initializes database handlers and asynchronously fetches the list of clubs
     * to populate the UI components upon completion.
     *
     * @param conn The active database connection.
     * @param user The currently logged-in {@code User}.
     */
    public ClubsRender(Connection conn, User user) {
        this.conn = conn;
        this.user = user;
        ih = new ImageDatabaseHandler(conn);

        ClubRepository clubRepo = new ClubRepository(conn);
        /**
         * Asynchronously fetches all clubs from the database.
         * On success, it initializes the UI components (metrics, form, charts, carousel)
         * with the fetched data. Handles the case where no clubs are available.
         */
        clubRepo.getAllClubs().thenAcceptAsync(listOfClubs -> {
            if (listOfClubs.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get clubs or no clubs available");
                    clubs = new ArrayList<>();
                    // Setup metrics in top panels
                    setupMetrics(clubs);

                    // Setup create club form in large panel
                    setupCreateClubForm();

                    // Setup bar chart in middle panel
                    setupBarChart(clubs);

                    // Setup club carousel in bottom panel
                    setupClubCarousel(clubs);
                });
            }else{
                SwingUtilities.invokeLater(()->{
                    clubs = listOfClubs;
                    // Setup metrics in top panels
                    setupMetrics(clubs);

                    // Setup create club form in large panel
                    setupCreateClubForm();

                    // Setup bar chart in middle panel
                    setupBarChart(clubs);

                    // Setup club carousel in bottom panel
                    setupClubCarousel(clubs);
                });
            }
        });
    }

    /**
     * Populates the top-left, top-center, and top-right sections with key metrics
     * (Total Clubs, Active Clubs, Pending Approval) derived from the list of clubs.
     *
     * @param clubs The list of {@code Club} objects used to calculate metrics.
     */
    private void setupMetrics(List<Club> clubs) {
        // Top Left - Total Clubs Metric
        JPanel totalMetric = createMetricPanel("Total Clubs", String.valueOf(clubs.size()),
                new Color(52, 152, 219));
        topLeft.add(totalMetric);

        // Top Center - Active Clubs Metric
        long activeCount = clubs.stream()
                .filter(c -> "active".equalsIgnoreCase(c.status()))
                .count();
        JPanel activeMetric = createMetricPanel("Active Clubs", String.valueOf(activeCount),
                new Color(46, 204, 113));
        topCenter.add(activeMetric);

        // Top Right - Pending Approval Metric
        long pendingCount = clubs.stream()
                .filter(c -> "inactive".equalsIgnoreCase(c.status()))
                .count();
        JPanel pendingMetric = createMetricPanel("Deactivated Clubs", String.valueOf(pendingCount),
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
     * Sets up the club creation form in the {@code rightLarge} panel.
     * This form includes fields for club details and a button to handle logo upload
     * and final submission.
     */
    private void setupCreateClubForm() {
        rightLarge.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Create New Club", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        // Club Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Club Name:"), gbc);
        gbc.gridy = 1;
        nameField = createTextField();
        formPanel.add(nameField, gbc);

        // Category
        gbc.gridy = 2;
        formPanel.add(createLabel("Category:"), gbc);
        gbc.gridy = 3;
        categoryField = createTextField();
        formPanel.add(categoryField, gbc);

        // Status
        gbc.gridy = 4;
        formPanel.add(createLabel("Status:"), gbc);
        gbc.gridy = 5;
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Active", "Inactive"});
        styleComboBox(statusComboBox);
        formPanel.add(statusComboBox, gbc);

        // Description
        gbc.gridy = 6;
        formPanel.add(createLabel("Description:"), gbc);
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(50, 50, 50));
        descriptionArea.setForeground(Color.WHITE);
        descriptionArea.setCaretColor(Color.WHITE);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane descScroll = new MaterialScrollPane(descriptionArea);
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        formPanel.add(descScroll, gbc);

        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Budget Proposal
        gbc.gridy = 8;
        formPanel.add(createLabel("Budget Proposal:"), gbc);
        gbc.gridy = 9;
        budgetProposalField = createTextField();
        formPanel.add(budgetProposalField, gbc);

        // Member Capacity
        gbc.gridy = 10;
        formPanel.add(createLabel("Member Capacity:"), gbc);
        gbc.gridy = 11;
        memberCapacityField = createTextField();
        formPanel.add(memberCapacityField, gbc);

        // Logo Upload Button
        gbc.gridy = 12;
        uploadLogoButton = new MaterialButton("Upload Photo", new Color(50,25,150));
        styleButton(uploadLogoButton);
        uploadLogoButton.addActionListener(e -> selectLogo());
        formPanel.add(uploadLogoButton, gbc);

        // Create Button
        gbc.gridy = 13;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton createButton = new MaterialButton("Create Club", new Color(120, 15, 15));
        styleButton(createButton);
        createButton.setBackground(new Color(46, 204, 113));
        createButton.addActionListener(e -> createClub());
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
     * Applies a custom material style to a {@code JButton}, including a hover effect.
     *
     * @param button The {@code JButton} to style.
     */
    private void styleButton(JButton button) {
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color originalColor = button.getBackground();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
    }

    /**
     * Opens a {@code JFileChooser} to allow the user to select an image file for the club logo.
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
     * Handles the creation of a new club: validates form inputs, creates a {@code Club} object,
     * sends it to the database via {@code ClubRepository}, and attempts to save the logo.
     * Displays a dialog box with the result.
     */
    private void createClub() {
        // Validate inputs
        if(nameField.getText().isEmpty() || categoryField.getText().isEmpty() || descriptionArea.getText().isEmpty() || budgetProposalField.getText().isEmpty() || memberCapacityField.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        /**
         * Performs validation on the text fields and checks if budget and capacity are non-negative.
         * Assumes {@code Utilities.isValidText} and the fields contain valid parsable numbers.
         */
        if(!(Utilities.isValidText(nameField.getText()) || Utilities.isValidText(categoryField.getText()) || Double.parseDouble(budgetProposalField.getText()) > -1 || Integer.parseInt(memberCapacityField.getText()) > -1)){
            JOptionPane.showMessageDialog(this, "Please enter valid inputs");
        }

        Club newClub = new Club(
                LocalDateTime.now().getNano(),
                nameField.getText().trim(),
                statusComboBox.getSelectedItem().toString().toLowerCase(),
                categoryField.getText().trim(),
                descriptionArea.getText().trim(),
                Double.parseDouble(budgetProposalField.getText().trim()),
                Integer.parseInt(memberCapacityField.getText().trim()),
                0,
                0,
                java.time.LocalDate.now().toString(),
                user.userId()
        );
        ClubRepository clubRepo = new ClubRepository(conn);
        /**
         * Asynchronously adds the new club to the database and attempts to save the logo.
         */
        clubRepo.addClub(newClub).thenAcceptAsync(success->{
            if(success){
                boolean result = ih.saveClubLogo(newClub.clubId(), selectedLogoPath);
                if(result){
                    SwingUtilities.invokeLater(()->{
                        JOptionPane.showMessageDialog(this, "Club created successfully");
                    });}
                else {
                    SwingUtilities.invokeLater(()->{
                        JOptionPane.showMessageDialog(this, "Club created successfully but failed to upload logo");
                    });
                }
            }
            else{
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Club creation failed");
                });
            }
        });
        //refresh
        /**
         * Triggers a refresh of the club list to update the UI components after creation.
         */
        clubRepo.getAllClubs().thenAcceptAsync(listOfClubs -> {
            if (listOfClubs.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get clubs or no clubs available");
                });
            }else{
                clubs = listOfClubs;
            }
        });
        clearForm();
    }

    /**
     * Clears all input fields in the club creation form and resets the status dropdown.
     */
    private void clearForm() {
        nameField.setText("");
        categoryField.setText("");
        descriptionArea.setText("");
        budgetProposalField.setText("");
        memberCapacityField.setText("");
        statusComboBox.setSelectedIndex(0);
        uploadLogoButton.setText("Upload Logo");
    }

    /**
     * Sets up a bar chart in the {@code middle} panel showing club creation trends over the year.
     *
     * @param clubs The list of {@code Club} objects (currently uses mock data for the chart).
     */
    private void setupBarChart(List<Club> clubs) {
        middle.setLayout(new BorderLayout());

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] clubCounts = {3, 5, 4, 7, 6, 8, 5, 9, 7, 6, 8, 10};

        MaterialBarChart chartPanel = new MaterialBarChart("Clubs Created by Month", months, clubCounts);

        middle.add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Sets up the {@code ClubCarousel} in the {@code bottom} panel to display a navigable list of clubs.
     * Displays a "No clubs available" message if the club list is empty.
     *
     * @param clubs The list of {@code Club} objects to display.
     */
    private void setupClubCarousel(List<Club> clubs) {
        bottom.setLayout(new BorderLayout());

        if (clubs.isEmpty()) {
            JLabel noClubsLabel = new JLabel("No clubs available", SwingConstants.CENTER);
            noClubsLabel.setForeground(Color.LIGHT_GRAY);
            noClubsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            bottom.add(noClubsLabel, BorderLayout.CENTER);
        } else {
            ClubCarousel carousel = new ClubCarousel(clubs, this.ih, this.user, this.conn);
            bottom.add(carousel, BorderLayout.CENTER);
        }
    }

}