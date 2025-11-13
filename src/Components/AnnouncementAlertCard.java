package Components;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

import Models.Announcements;

/**
 * A custom Swing JPanel component designed to display a single announcement
 * in an alert-card style format.
 * This component visualizes the announcement's title, content, creation date, and expiry date.
 */
public class AnnouncementAlertCard extends JPanel {

    private final Announcements announcement;

    /**
     * Constructs an {@code AnnouncementAlertCard} panel initialized with a specific announcement.
     * It sets up the basic visual properties of the card and calls {@code initComponents()}
     * to populate its content.
     *
     * @param announcement The {@link Models.Announcements} object containing the data to display.
     */
    public AnnouncementAlertCard(Announcements announcement) {
        this.announcement = announcement;
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        initComponents();
    }

    /**
     * Initializes and lays out the sub-components (labels and text area) that display
     * the announcement's details within the card.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel(announcement.Title());
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JTextArea contentArea = new JTextArea(announcement.content());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setForeground(new Color(220, 220, 220));
        contentArea.setBackground(new Color(45, 45, 45));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel expiryLabel = new JLabel("Expires: " + formatDate(announcement.expiryDate()));
        expiryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        expiryLabel.setForeground(new Color(180, 100, 100));
        headerPanel.add(expiryLabel, BorderLayout.EAST);

        JLabel dateLabel = new JLabel("Posted: " + formatDate(announcement.createdDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(150, 150, 150));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.add(dateLabel);

        add(headerPanel, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Formats a {@code java.util.Date} object into a standardized string format "dd MMM yyyy".
     *
     * @param date The date object to format. Can be {@code null}.
     * @return A formatted date string, or "N/A" if the input date is {@code null}.
     */
    private String formatDate(java.util.Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("dd MMM yyyy").format(date);
    }

    /**
     * Overrides the standard component painting to set the cursor to a hand cursor
     * when over the card, indicating it may be interactive.
     *
     * @param g The {@code Graphics} context used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}