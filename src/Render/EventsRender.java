package Render;

import Components.EventCalendarPanel;
import Models.Event;
import Models.Resource;
import Models.User;
import MaterialSwingUI.*;
import Repository.ClubMemberRepository;
import Repository.EventRepository;
import Repository.ResourceRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
/**
 * Renders the main dashboard page for managing and viewing event information.
 * This class extends {@code PageStructure} to utilize a predefined layout
 * and populates its sections with metrics, an event creation form, and an event calendar.
 */
public class EventsRender extends PageStructure {

    private JTextField titleField;
    private JTextField typeField;
    private JTextArea descriptionArea;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField budgetAmountField;
    private JComboBox<String> statusComboBox = new JComboBox<>();
    private JComboBox<String> cmbResources = new JComboBox<>();
    private JCheckBox budgetRequestCheck;
    private List<Event> events;
    private final Connection conn;
    private final User user;
    private final EventRepository eventrepo;

    /**
     * Constructs the {@code EventsRender} page.
     * Initializes database connection, user data, repositories, loads available resources,
     * and asynchronously fetches the list of all events to populate the UI.
     *
     * @param conn The active database connection.
     * @param user The currently logged-in {@code User}.
     * @throws RuntimeException if loading resources fails due to execution or interruption errors.
     */
    public EventsRender(Connection conn, User user) {
        super();
        this.conn = conn;
        this.user = user;

        /**
         * Removes the default bottom card from the inherited layout, as the calendar
         * is added to the middle panel instead in this implementation.
         */
        super.removeCard(bottom, bottom);
        eventrepo = new EventRepository(conn);
        try{
            loadResources();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        /**
         * Asynchronously fetches all events from the database.
         * On success, it initializes the UI components (metrics, form, calendar)
         * with the fetched data. Handles the case where no events are available.
         */
        eventrepo.getAllEvents().thenAcceptAsync(listOfEvents->{
            if(listOfEvents.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get events");
                    events = new ArrayList<>();
                    // Setup metrics in top panels
                    setupMetrics(events);

                    // Setup create event form in large panel
                    setupCreateEventForm();

                    // Setup event carousel in bottom panel
                    setupEventCalendar(events);
                });
            }
            else{
                SwingUtilities.invokeLater(()->{
                    events = listOfEvents;
                    // Setup metrics in top panels
                    setupMetrics(events);

                    // Setup create event form in large panel
                    setupCreateEventForm();

                    // Setup event carousel in bottom panel
                    setupEventCalendar(events);
                });
            }
        });


    }

    /**
     * Populates the top-left, top-center, and top-right sections with key metrics
     * (Total Events, Active Events, Pending Approval) derived from the list of events.
     *
     * @param events The list of {@code Event} objects used to calculate metrics.
     */
    private void setupMetrics(List<Event> events) {
        // Top Left - Total Events
        JPanel totalMetric = createMetricPanel("Total Events", String.valueOf(events.size()),
                new Color(52, 152, 219));
        topLeft.add(totalMetric);

        // Top Center - Active Events
        long activeCount = events.stream()
                .filter(e -> "scheduled".equalsIgnoreCase(e.status()))
                .count();
        JPanel activeMetric = createMetricPanel("Active Events", String.valueOf(activeCount),
                new Color(46, 204, 113));
        topCenter.add(activeMetric);

        // Top Right - Pending Events
        long pendingCount = events.stream()
                .filter(e -> "cancelled".equalsIgnoreCase(e.status()))
                .count();
        JPanel pendingMetric = createMetricPanel("Cancelled", String.valueOf(pendingCount),
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
     * Sets up the event creation form in the {@code rightLarge} panel.
     * This form collects details such as title, type, status, venue, description, time, and budget.
     */
    private void setupCreateEventForm() {
        rightLarge.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Create New Event", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        // Event Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Event Title:"), gbc);
        gbc.gridy = 1;
        titleField = createTextField();
        formPanel.add(titleField, gbc);

        // Type
        gbc.gridy = 2;
        formPanel.add(createLabel("Event Type:"), gbc);
        gbc.gridy = 3;
        typeField = createTextField();
        formPanel.add(typeField, gbc);

        // Status
        gbc.gridy = 4;
        formPanel.add(createLabel("Status:"), gbc);
        gbc.gridy = 5;
        statusComboBox = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});
        styleComboBox(statusComboBox);
        formPanel.add(statusComboBox, gbc);

        // Resources
        gbc.gridy = 6;
        formPanel.add(createLabel("Venue:"), gbc);
        gbc.gridy = 7;
        styleComboBox(cmbResources);
        formPanel.add(cmbResources, gbc);

        // Description
        gbc.gridy = 8;
        formPanel.add(createLabel("Description:"), gbc);
        gbc.gridy = 9;
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

        // Start Time
        gbc.gridy = 10;
        formPanel.add(createLabel("Start Time:"), gbc);
        gbc.gridy = 11;
        startTimeField = createTextField();
        formPanel.add(startTimeField, gbc);

        // End Time
        gbc.gridy = 12;
        formPanel.add(createLabel("End Time:"), gbc);
        gbc.gridy = 13;
        endTimeField = createTextField();
        formPanel.add(endTimeField, gbc);

        // Budget Request Checkbox
        gbc.gridy = 14;
        budgetRequestCheck = new JCheckBox("Budget Requested?");
        budgetRequestCheck.setOpaque(false);
        budgetRequestCheck.setForeground(Color.WHITE);
        formPanel.add(budgetRequestCheck, gbc);

        // Budget Amount
        gbc.gridy = 15;
        formPanel.add(createLabel("Budget Amount:"), gbc);
        gbc.gridy = 16;
        budgetAmountField = createTextField();
        formPanel.add(budgetAmountField, gbc);

        // Create Button
        gbc.gridy = 17;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton createButton = new MaterialButton("Create Event", new Color(16, 90, 40));
        createButton.addActionListener(e -> createEvent());
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
     * Handles the creation of a new event: validates the title, retrieves the user's club ID,
     * constructs an {@code Event} object, and submits it to the database.
     */
    private void createEvent() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an event title",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClubMemberRepository memRepo = new ClubMemberRepository(conn);
        /**
         * Asynchronously retrieves the current user's club membership details to get the club ID.
         */
        memRepo.getMemberById(user.userId()).thenAcceptAsync(member->{
            if(member.isPresent()){
                // Create Event record
                /**
                 * Constructs the {@code Event} object using form data, retrieving the resource ID
                 * from the selected item in the combo box by splitting the string.
                 */
                Event newEvent = new Event(
                        System.currentTimeMillis(),
                        titleField.getText().trim(),
                        typeField.getText().trim(),
                        descriptionArea.getText().trim(),
                        new Date(System.currentTimeMillis()),
                        Objects.requireNonNull(statusComboBox.getSelectedItem()).toString().toLowerCase(),
                        startTimeField.getText().trim(),
                        Integer.parseInt(Objects.requireNonNull(cmbResources.getSelectedItem()).toString().trim().split("-")[0]),
                        endTimeField.getText().trim(),
                        budgetRequestCheck.isSelected(),
                        parseDoubleOrZero(budgetAmountField.getText()),
                        "pending",
                        0,
                        new Date(System.currentTimeMillis()),
                        user.userId(),
                        member.get().club_id()
                );
                EventRepository eventRepo = new EventRepository(conn);
                /**
                 * Submits the new event to the database and informs the user of success or failure.
                 */
                eventRepo.addEvent(newEvent).thenAcceptAsync(success->{
                    if(success){
                        JOptionPane.showMessageDialog(this, "Event created successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(this, "Event creation failed!",
                                "Failure", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

            }else{
                JOptionPane.showMessageDialog(this, "Something went wrong!",
                        "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        EventRepository eventRepo = new EventRepository(conn);

        /**
         * Triggers a refresh of the event list after creation attempt to update the UI.
         */
        eventRepo.getAllEvents().thenAcceptAsync(listOfEvents->{
            if(listOfEvents.isEmpty()){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(this, "Failed to get events");
                });
            }
            else{
                events = listOfEvents;
            }
        });
        clearForm();
    }

    /**
     * Attempts to parse a string into a double, returning 0.0 if parsing fails.
     * Used for budget input validation.
     *
     * @param text The string to parse.
     * @return The parsed double value, or 0.0 on error.
     */
    private double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Clears all input fields in the event creation form and resets the dropdowns.
     */
    private void clearForm() {
        titleField.setText("");
        typeField.setText("");
        descriptionArea.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        budgetAmountField.setText("");
        budgetRequestCheck.setSelected(false);
        statusComboBox.setSelectedIndex(0);
    }

    /**
     * Fetches all available resources from the database and populates the {@code cmbResources}
     * combo box with their ID, name, and capacity.
     *
     * @throws ExecutionException if the asynchronous resource fetching fails.
     * @throws InterruptedException if the thread is interrupted while waiting for resources.
     */
    private void loadResources() throws ExecutionException, InterruptedException {
        ResourceRepository resourceRepository = new ResourceRepository(this.conn);
        for (Resource r : resourceRepository.getAllResources().get()){
            if (r.isAvailable()) cmbResources.addItem(r.resourceId() + " - " + r.name() + " Capacity - " + r.capacity());
        }
    }

    /**
     * Sets up the event calendar in the {@code middle} panel.
     * Displays a "No events available" message if the event list is empty.
     *
     * @param events The list of {@code Event} objects to populate the calendar.
     */
    private void setupEventCalendar(List<Event> events) {
        bottom.setLayout(new BorderLayout());

        if (events.isEmpty()) {
            JLabel noEventsLabel = new JLabel("No events available", SwingConstants.CENTER);
            noEventsLabel.setForeground(Color.LIGHT_GRAY);
            noEventsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            bottom.add(noEventsLabel, BorderLayout.CENTER);
        } else {
            EventCalendarPanel calendar = new EventCalendarPanel(events, conn, user);
            middle.add(calendar, BorderLayout.CENTER);
        }
    }
}