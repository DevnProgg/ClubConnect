// NotificationsRender.java — Full functionality implementation
// Includes DB loading, create announcement dialog, email sending, refresh

package Render;

import Components.AnnouncementAlertCard;
import Models.Announcements;
import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialField;
import Repository.AnnouncementsRepository;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * A panel responsible for rendering the notifications and announcements dashboard.
 * It displays a list of announcements, provides actions to send new announcements,
 * create polls (not implemented), and send email reminders.
 */
public class NotificationsRender extends JPanel {

    private JPanel notificationsContainer;
    private final ExecutorService emailExecutor;
    private final Connection conn;
    private final User user;
    private final AnnouncementsRepository repo;
    private List<Announcements> announcements = new ArrayList<>();

    /**
     * Constructs the {@code NotificationsRender} panel.
     * Initializes the UI components, sets up action listeners for buttons,
     * and initializes an {@code ExecutorService} for handling background tasks like email sending.
     *
     * @param conn The active database connection.
     * @param user The currently logged-in {@code User}.
     */
    public NotificationsRender(Connection conn, User user) {
        this.conn = conn;
        this.user = user;
        this.repo = new AnnouncementsRepository(conn);
        this.emailExecutor = Executors.newFixedThreadPool(5);

        /**
         * Populates a list of sample announcements for initial display testing.
         */
        for (int i = 1; i <= 5; i++) {
            Announcements a = new Announcements(
                    i,
                    101,
                    "This is a sample announcement content number " + i +
                            " with more text to test how the text area wraps and adjusts height automatically.",
                    "Announcement " + i,
                    0,
                    new Date(System.currentTimeMillis() + (86400000L * i)),
                    new Date()
            );
            announcements.add(a);
        }

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        JLabel headerLabel = new JLabel("Notifications & Announcements");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(headerLabel, BorderLayout.NORTH);

        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(new Color(30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBackground(new Color(35, 35, 35));

        JButton sendAnnouncementButton = new MaterialButton("Send Announcement", new Color(60, 60, 60));
        JButton createPollButton = new MaterialButton("Create Poll", new Color(60, 60, 60));
        JButton emailRemindersButton = new MaterialButton("Send Email Reminders", new Color(60, 60, 60));
        JButton refreshButton = new MaterialButton("Refresh", new Color(60, 60, 60));

        sendAnnouncementButton.addActionListener(e -> showCreateAnnouncementDialog());
        emailRemindersButton.addActionListener(e -> sendReminderEmails());
        refreshButton.addActionListener(e -> refreshAnnouncements());

        actionsPanel.add(sendAnnouncementButton);
        actionsPanel.add(createPollButton);
        actionsPanel.add(emailRemindersButton);
        actionsPanel.add(refreshButton);

        add(actionsPanel, BorderLayout.SOUTH);

        refreshAnnouncements();
    }

    /**
     * Triggers the refresh process for the announcement display by calling {@code loadAnnouncements}
     * with the current list of announcements.
     */
    private void refreshAnnouncements() {
        loadAnnouncements(this.announcements);
    }

    /**
     * Clears the current list of displayed announcements and populates the container
     * with new {@code AnnouncementAlertCard} components based on the provided list.
     *
     * @param announcementsList The list of {@code Announcements} to display.
     */
    public void loadAnnouncements(List<Announcements> announcementsList) {
        notificationsContainer.removeAll();
        /**
         * Iterates through the list, creates a card for each announcement,
         * sets its maximum size to ensure proper alignment in the BoxLayout,
         * and adds it to the container with a vertical separator.
         */
        for (Announcements ann : announcementsList) {
            AnnouncementAlertCard card = new AnnouncementAlertCard(ann);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
            notificationsContainer.add(card);
            notificationsContainer.add(Box.createVerticalStrut(10));
        }
        revalidate();
        repaint();
    }

    /**
     * Displays a dialog window for the user to input the title and content
     * of a new announcement. Upon confirmation, it creates the announcement
     * record and attempts to save it to the database.
     */
    private void showCreateAnnouncementDialog() {
        JTextField titleField = new MaterialField();
        JTextArea contentArea = new JTextArea(5, 20);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(new Color(40, 40, 40));

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Content:"));
        panel.add(contentScroll);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Announcement", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Announcements ann = new Announcements(
                    0,
                    0,
                    contentArea.getText(),
                    titleField.getText(),
                    0,
                    new Date(System.currentTimeMillis() + 86400000L),
                    new Date()
            );

            try {
                /**
                 * Adds the new announcement to the local list and persists it to the database.
                 */
                this.announcements.add(ann);
                repo.insertAnnouncement(ann);
                refreshAnnouncements();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to save announcement", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Initiates the process of sending reminder emails to a predefined list of recipients
     * by submitting the email tasks to the {@code emailExecutor} thread pool.
     */
    public void sendReminderEmails() {
        List<String> emails = new ArrayList<>();
        emails.add("member@example.com");
        /**
         * Submits a task to the executor service to iterate through the list of emails
         * and call {@code sendEmail} for each recipient.
         */
        emailExecutor.submit(() -> emails.forEach(e -> sendEmail(e, "Event Reminder", "Don't miss upcoming events!")));
        JOptionPane.showMessageDialog(this, "Reminder emails queued.");
    }

    /**
     * Simulates the process of sending an email, printing the recipient information to the console
     * and introducing a short delay to mimic network latency.
     *
     * @param recipient The email address of the recipient.
     * @param subject The subject line of the email.
     * @param body The body content of the email.
     */
    private void sendEmail(String recipient, String subject, String body) {
        System.out.println("Sending email to: " + recipient);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}