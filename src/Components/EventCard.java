package Components;

import Models.Event;
import Models.RSVP;
import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialScrollPane;
import Repository.EventRepository;
import Repository.RSVPRepository;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A detailed {@code JPanel} component used to display all information about a single {@link Models.Event},
 * including its status, detailed fields, and a list of current RSVPs. It also provides functionality
 * for cancelling the event (if authorized) and allowing the current user to submit an RSVP.
 */
public class EventCard extends JPanel {
    private final Color cardBg = new Color(50, 50, 50);
    private final Color textColor = Color.WHITE;
    private final Color labelColor = new Color(180, 180, 180);
    private final Color danger = new Color(180, 50, 50);

    private JLabel statusValueLabel;
    private Event event;
    private final Connection conn;
    private List<RSVP> rsvps = new ArrayList<>();
    private final Map<Event, RSVPData> rsvpStore = new HashMap<>();
    private final User user;

    /**
     * Constructs an {@code EventCard}.
     *
     * @param event The {@link Models.Event} to display.
     * @param conn The database {@code Connection} used for all repository operations (RSVP and Event updates).
     * @param user The currently logged-in {@link Models.User}, necessary for submitting RSVPs and checking cancellation authorization.
     */
    public EventCard(Event event, Connection conn, User user) {
        this.event = event;
        this.conn = conn;
        this.user = user;

        loadRSVPs();
        buildUI();

    }

    /**
     * Creates a card component to display the details of a single RSVP submission for the event.
     * It includes the user ID, RSVP status, date, and a checkbox to mark attendance.
     *
     * @param rsvp The {@link Models.RSVP} object to display.
     * @return A styled {@code JPanel} representing the RSVP card.
     */
    private JPanel createRSVPCard(RSVP rsvp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel userLabel = new JLabel("User ID: " + rsvp.user_id());
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel statusLabel = new JLabel("Status: " + rsvp.status());
        statusLabel.setForeground(new Color(180, 180, 180));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel dateLabel = new JLabel("RSVP Date: " + rsvp.rsvpDate());
        dateLabel.setForeground(new Color(150, 150, 150));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JCheckBox attendanceBox = getAttendanceBox(rsvp, panel);

        JPanel top = new JPanel(new GridLayout(3, 1));
        top.setBackground(new Color(60, 60, 60));
        top.add(userLabel);
        top.add(statusLabel);
        top.add(dateLabel);

        panel.add(top, BorderLayout.CENTER);
        panel.add(attendanceBox, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates and configures the checkbox used to mark or unmark attendance for a specific RSVP.
     * This method also attaches an {@code ActionListener} to update the RSVP status asynchronously
     * in the database when the box state changes.
     *
     * @param rsvp The {@link Models.RSVP} instance being modified.
     * @param panel The parent {@code JPanel} used as the owner for the {@code JOptionPane} feedback.
     * @return A {@code JCheckBox} configured for attendance marking.
     */
    private JCheckBox getAttendanceBox(RSVP rsvp, JPanel panel) {
        JCheckBox attendanceBox = new JCheckBox("Attendance Marked");
        attendanceBox.setSelected(rsvp.attendanceMarked());
        attendanceBox.setForeground(Color.WHITE);
        attendanceBox.setBackground(new Color(60, 60, 60));
        attendanceBox.addActionListener(e -> {
            boolean marked = attendanceBox.isSelected();
            RSVPRepository rsvpRepository = new RSVPRepository(conn);
            RSVP Rsvp = new RSVP(
                    rsvp.RSVP_ID(),
                    rsvp.user_id(),
                    rsvp.event_id(),
                    rsvp.status(),
                    rsvp.rsvpDate(),
                    marked
            );
            rsvpRepository.updateRSVP(Rsvp).thenAcceptAsync(success->{
                if(success){
                    JOptionPane.showMessageDialog(panel,
                            "Attendance for user " + rsvp.user_id() + " marked as " + (marked ? "Present" : "Absent"),
                            "Attendance Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
        return attendanceBox;
    }

    /**
     * Creates a vertically aligned panel for displaying a key-value pair of event data.
     * It uses a smaller font for the label and a multi-line text area for the value.
     *
     * @param prefix A short prefix (e.g., "PK") to highlight the field type.
     * @param label The descriptive label of the field (the key).
     * @param value The actual value of the field (the value).
     * @return A styled {@code JPanel} containing the labeled field.
     */
    private JPanel createFieldPanel(String prefix, String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelPanel = getLabelPanel(prefix, label);

        JTextArea valueLabel = new JTextArea(value);
        valueLabel.setWrapStyleWord(true);
        valueLabel.setLineWrap(true);
        valueLabel.setEditable(false);
        valueLabel.setForeground(textColor);
        valueLabel.setBackground(cardBg);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setBorder(null);

        panel.add(labelPanel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);

        return panel;
    }

    /**
     * Creates a horizontal panel for displaying the field label and an optional prefix.
     *
     * @param prefix A short prefix (e.g., "PK").
     * @param label The descriptive label of the field.
     * @return A styled {@code JPanel} containing the prefix and label.
     */
    private JPanel getLabelPanel(String prefix, String label) {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setBackground(cardBg);

        if (!prefix.isEmpty()) {
            JLabel prefixLabel = new JLabel(prefix + "  ");
            prefixLabel.setForeground(labelColor);
            prefixLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            labelPanel.add(prefixLabel);
        }

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(labelColor);
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelPanel.add(fieldLabel);
        return labelPanel;
    }

    /**
     * Creates a custom {@code Border} that provides rounded corners for the main card background.
     *
     * @return An {@code AbstractBorder} implementation for rounded corners.
     */
    private Border createRoundedBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fillRoundRect(x, y, width - 1, height - 1, 15, 15);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(5, 5, 5, 5);
            }
        };
    }
    /**
     * Asynchronously loads the list of RSVPs for the current event from the database.
     * Upon completion, it updates the internal {@code rsvps} list and calls {@code buildUI()}
     * to refresh the displayed card content on the Swing Event Dispatch Thread (EDT).
     */
    private void loadRSVPs(){
        RSVPRepository rsvpRepository = new RSVPRepository(this.conn);

        rsvpRepository.getRSVPByEventId(event.eventId())
                .thenAcceptAsync(listOfrsvp -> {

                    SwingUtilities.invokeLater(() -> {
                        if(listOfrsvp.isEmpty()){
                            JOptionPane.showMessageDialog(this, "No RSVP data.");
                        } else {
                            rsvps = listOfrsvp;
                        }

                        removeAll();   // remove old UI
                        revalidate();  // rebuild UI
                        repaint();     // repaint UI

                        // rebuild UI with loaded RSVPs
                        buildUI();
                    });
                });
    }

    /**
     * Lays out and constructs the entire user interface of the {@code EventCard}.
     * This includes the header, detailed event fields, the status label, and the scrollable RSVP list.
     */
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        removeAll();

        setLayout(new BorderLayout(0, 0));
        setBackground(cardBg);
        setBorder(createRoundedBorder());

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        Color accent = new Color(0, 120, 100);
        header.setBackground(accent);
        header.setPreferredSize(new Dimension(350, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Title and Cancel Button Panel
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);

        JLabel titleLabel = new JLabel(event.title());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerContent.add(titleLabel, BorderLayout.CENTER);

        JButton cancelButton = createCancelButton();
        JButton rsvpButton = createRSVPButton();
        headerContent.add(cancelButton, BorderLayout.EAST);
        headerContent.add(rsvpButton, BorderLayout.WEST);

        header.add(headerContent, BorderLayout.CENTER);

        // --- CONTENT PANEL ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(cardBg);
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Event Fields
        content.add(createFieldPanel("PK", "Event ID", String.valueOf(event.eventId())));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Event Type", event.type()));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Description", event.description()));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Event Date", event.date() + " " + event.startTime() + " → " + event.endTime()));
        content.add(Box.createVerticalStrut(8));

        // Status
        statusValueLabel = new JLabel(event.status());
        statusValueLabel.setForeground(event.status().equalsIgnoreCase("Cancelled") ? danger : textColor);
        statusValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setBackground(cardBg);
        statusPanel.add(new JLabel("Status: "));
        statusPanel.add(statusValueLabel);
        content.add(statusPanel);
        content.add(Box.createVerticalStrut(8));

        // Budget
        if (event.isBudgetRequested()) {
            content.add(createFieldPanel("", "Budget Amount", String.valueOf(event.budgetAmount())));
            content.add(Box.createVerticalStrut(8));
            content.add(createFieldPanel("", "Budget Status", event.budgetStatus()));
            content.add(Box.createVerticalStrut(8));
        }

        // Other Fields
        content.add(createFieldPanel("", "Resource", String.valueOf(event.resourceId())));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Approved By", String.valueOf(event.approvedBy())));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Created By", String.valueOf(event.createdBy())));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Created Date", String.valueOf(event.createdDate())));
        content.add(Box.createVerticalStrut(8));
        content.add(createFieldPanel("", "Club", String.valueOf(event.clubId())));
        content.add(Box.createVerticalStrut(15));

        // --- RSVP SECTION ---
        JLabel rsvpLabel = new JLabel("Event RSVPs");
        rsvpLabel.setForeground(Color.WHITE);
        rsvpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.add(rsvpLabel);
        content.add(Box.createVerticalStrut(10));

        JPanel rsvpPanel = new JPanel();
        rsvpPanel.setLayout(new BoxLayout(rsvpPanel, BoxLayout.Y_AXIS));
        rsvpPanel.setBackground(new Color(45, 45, 45));

        if (rsvps.isEmpty()) {
            JLabel noRSVP = new JLabel("No RSVPs for this event yet.");
            noRSVP.setForeground(Color.GRAY);
            rsvpPanel.add(noRSVP);
        } else {
            for (RSVP rsvp : rsvps) {
                JPanel rsvpCard = createRSVPCard(rsvp);
                rsvpCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, rsvpCard.getPreferredSize().height));
                rsvpPanel.add(rsvpCard);
                rsvpPanel.add(Box.createVerticalStrut(5));
            }
        }

        JScrollPane rsvpScroll = new MaterialScrollPane(rsvpPanel);
        rsvpScroll.setPreferredSize(new Dimension(0, 150));
        rsvpScroll.setBorder(BorderFactory.createEmptyBorder());
        rsvpScroll.getViewport().setBackground(new Color(45, 45, 45));

        content.add(rsvpScroll);

        JScrollPane mainScroll = new MaterialScrollPane(content);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(cardBg);

        add(header, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    /**
     * Creates and configures the Cancel button, which allows users to cancel the event.
     * It includes a confirmation dialog and updates the event status in the database and the UI asynchronously.
     *
     * @return A styled {@code JButton} for cancelling the event.
     */
    private JButton createCancelButton() {
        JButton cancelButton =  new MaterialButton("Cancel", new Color(0, 120, 80));
        cancelButton.setBackground(danger);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to cancel this event?\nThis action cannot be undone.",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                event = new Event(
                        event.eventId(),
                        event.title(),
                        event.type(),
                        event.description(),
                        event.date(),
                        "Cancelled",
                        event.startTime(),
                        event.clubId(),
                        event.endTime(),
                        event.isBudgetRequested(),
                        event.budgetAmount(),
                        event.budgetStatus(),
                        event.resourceId(),
                        event.createdDate(),
                        event.approvedBy(),
                        event.createdBy()
                );
                EventRepository eventRepository = new EventRepository(conn);
                eventRepository.updateEvent(event).thenAcceptAsync(success->{
                    if(success){
                        statusValueLabel.setText("Cancelled");
                        statusValueLabel.setForeground(danger);
                        JOptionPane.showMessageDialog(this, "Event cancelled successfully!", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "Event cancellation failed!", "Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
        return cancelButton;
    }

    /**
     * Creates the RSVP button, which opens a modal dialog allowing the current user to submit their RSVP status.
     *
     * @return A styled {@code JButton} for initiating the RSVP process.
     */
    private JButton createRSVPButton() {
        JButton cancelButton =  new MaterialButton("RSVP", new Color(0, 120, 170));
        cancelButton.setBackground(danger);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    createRSVPPanel(event),
                    "Event Details",
                    JOptionPane.PLAIN_MESSAGE
            );
        });
        return cancelButton;
    }

    /**
     * Creates a small control panel containing "Yes," "Maybe," and "No" buttons for submitting an RSVP.
     * It also displays the current RSVP counts for the event.
     *
     * @param e The {@link Models.Event} being RSVP'd to.
     * @return A {@code JPanel} containing the RSVP controls.
     */
    private JPanel createRSVPPanel(Event e) {
        RSVPRepository rsvpRepository = new RSVPRepository(this.conn);
        RSVPData data = rsvpStore.computeIfAbsent(e, k -> new RSVPData());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(45, 45, 45));

        JButton yes = new MaterialButton("Yes", Color.GREEN);
        JButton maybe = new MaterialButton("Maybe",Color.CYAN);
        JButton no = new MaterialButton("No", Color.RED);

        JLabel counter = new JLabel(data.toString());
        counter.setForeground(Color.WHITE);

        yes.addActionListener(a -> {
            data.yes++;
            counter.setText(data.toString());
            RSVP newRsvp = new RSVP(
                    0,
                    user.userId(),
                    e.eventId(),
                    "going",
                    String.valueOf(LocalDateTime.now()),
                    false
            );
            rsvpRepository.addRSVP(newRsvp).thenAcceptAsync(success->{
                if(success){
                    JOptionPane.showMessageDialog(this, "RSVP set!");
                }else{
                    JOptionPane.showMessageDialog(this, "RSVP not set!");
                }
                rsvpRepository.shutdown();
            });
        });
        maybe.addActionListener(a -> {
            data.maybe++;
            counter.setText(data.toString());
            RSVP newRsvp = new RSVP(
                    0,
                    user.userId(),
                    e.eventId(),
                    "maybe",
                    String.valueOf(LocalDateTime.now()),
                    false
            );
            rsvpRepository.addRSVP(newRsvp).thenAcceptAsync(success->{
                if(success){
                    JOptionPane.showMessageDialog(this, "RSVP set!");
                }else{
                    JOptionPane.showMessageDialog(this, "RSVP not set!");
                }
                rsvpRepository.shutdown();
            });
        });
        no.addActionListener(a -> {
            data.no++;
            counter.setText(data.toString());
            RSVP newRsvp = new RSVP(
                    0,
                    user.userId(),
                    e.eventId(),
                    "not going",
                    String.valueOf(LocalDateTime.now()),
                    false
            );
            rsvpRepository.addRSVP(newRsvp).thenAcceptAsync(success->{
                if(success){
                    JOptionPane.showMessageDialog(this, "RSVP set!");
                }else{
                    JOptionPane.showMessageDialog(this, "RSVP not set!");
                }rsvpRepository.shutdown();

            });
        });

        panel.add(yes);
        panel.add(maybe);
        panel.add(no);
        panel.add(counter);

        return panel;
    }

    /**
     * A private static class used to store and format the RSVP count data (Yes, Maybe, No)
     * for an event without needing to recalculate from the full RSVP list every time.
     */
    private static class RSVPData {
        public int yes = 0;
        public int maybe = 0;
        public int no = 0;

        /**
         * Returns a formatted string representation of the RSVP counts.
         *
         * @return A string showing the breakdown of RSVPs.
         */
        public String toString() {
            return String.format("RSVP ⇒ Yes: %d | Maybe: %d | No: %d", yes, maybe, no);
        }
    }

}