package Components;

import javax.swing.*;
import java.awt.*;

import Models.User;
import Repository.ImageDatabaseHandler;

import java.sql.Connection;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A specialized {@code JPanel} that acts as a user card carousel, displaying a list of
 * {@link Models.User} details using {@code UserCard} components in a sliding animation.
 * It provides navigation controls (Previous/Next) and an optional auto-slide feature.
 */
public class UserCarousel extends JPanel {
    private final List<User> users;
    private int currentIndex = 0;
    private final JPanel cardContainer;
    private Timer slideTimer;
    private int autoSlideDelay = 5000; // 5 seconds
    private final Color bgColor = new Color(45, 45, 45);
    private final ImageDatabaseHandler ih;
    private final Connection conn;
    private final User user;

    /**
     * Constructs the {@code UserCarousel}.
     *
     * @param users The list of {@link Models.User} objects to be displayed in the carousel.
     * @param conn The active database {@code Connection} to be passed to each {@code UserCard}.
     * @param ih The {@code ImageDatabaseHandler} instance.
     */
    public UserCarousel(List<User> users, Connection conn, ImageDatabaseHandler ih, User user) {
        this.users = users;
        this.conn = conn;
        this.ih = ih;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(bgColor);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Card container with CardLayout
        /**
         * Initializes the {@code cardContainer} with {@code CardLayout} to facilitate
         * smooth transitions between user cards.
         */
        cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(bgColor);

        // Add all event cards
        for (int i = 0; i < users.size(); i++) {
            UserCard card = new UserCard(users.get(i), conn, user);
            cardContainer.add(card, String.valueOf(i));
            cardContainer.revalidate();
            cardContainer.repaint();
        }

        // Navigation panel
        JPanel navPanel = createNavigationPanel();

        add(cardContainer, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);

        // Start auto-slide timer
        startAutoSlide();
    }

    /**
     * Creates and configures the panel containing the navigation buttons (Previous, Next, Pause/Play)
     * and the current index label.
     *
     * @return A styled {@code JPanel} containing the navigation controls.
     */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        navPanel.setBackground(bgColor);

        // Previous button
        JButton prevBtn = new JButton("◄ Previous");
        styleButton(prevBtn);
        prevBtn.setFocusPainted(false);
        prevBtn.setOpaque(true);
        prevBtn.setContentAreaFilled(true);
        prevBtn.setBorderPainted(false);
        prevBtn.addActionListener(e -> {
            stopAutoSlide();
            showPrevious();
            startAutoSlide();
        });

        // Current index label
        JLabel indexLabel = new JLabel();
        updateIndexLabel(indexLabel);
        indexLabel.setForeground(Color.WHITE);
        indexLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Next button
        JButton nextBtn = new JButton("Next ►");
        styleButton(nextBtn);
        nextBtn.setFocusPainted(false);
        nextBtn.setOpaque(true);
        nextBtn.setContentAreaFilled(true);
        nextBtn.setBorderPainted(false);
        nextBtn.addActionListener(e -> {
            stopAutoSlide();
            showNext();
            startAutoSlide();
        });

        // Pause/Play button
        JButton pauseBtn = new JButton("|| Pause");
        styleButton(pauseBtn);
        pauseBtn.setFocusPainted(false);
        pauseBtn.setOpaque(true);
        pauseBtn.setContentAreaFilled(true);
        pauseBtn.setBorderPainted(false);
        pauseBtn.addActionListener(e -> {
            if (slideTimer.isRunning()) {
                stopAutoSlide();
                pauseBtn.setText("▶ Play");
            } else {
                startAutoSlide();
                pauseBtn.setText("⏸ Pause");
            }
        });

        navPanel.add(prevBtn);
        navPanel.add(indexLabel);
        navPanel.add(nextBtn);
        navPanel.add(pauseBtn);

        // Update label on navigation
        ActionListener updateLabel = e -> updateIndexLabel(indexLabel);
        prevBtn.addActionListener(updateLabel);
        nextBtn.addActionListener(updateLabel);

        return navPanel;
    }

    /**
     * Applies standard styling and mouse listener effects to a navigation button.
     *
     * @param button The {@code JButton} to be styled.
     */
    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(90, 90, 90));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 70, 70));
            }
        });
    }

    /**
     * Updates the text of the index label to reflect the current position in the carousel
     * (e.g., "1 / 5").
     *
     * @param label The {@code JLabel} displaying the index.
     */
    private void updateIndexLabel(JLabel label) {
        label.setText((currentIndex + 1) + " / " + users.size());
    }

    /**
     * Calculates the next index (looping back to 0 if the end is reached) and uses
     * {@code CardLayout} to display the corresponding user card.
     */
    private void showNext() {
        currentIndex = (currentIndex + 1) % users.size();
        CardLayout cl = (CardLayout) cardContainer.getLayout();
        cl.show(cardContainer, String.valueOf(currentIndex));
    }

    /**
     * Calculates the previous index (looping back to the end if the beginning is reached) and uses
     * {@code CardLayout} to display the corresponding user card.
     */
    private void showPrevious() {
        currentIndex = (currentIndex - 1 + users.size()) % users.size();
        CardLayout cl = (CardLayout) cardContainer.getLayout();
        cl.show(cardContainer, String.valueOf(currentIndex));
    }

    /**
     * Starts the {@code Timer} to automatically slide the carousel to the next card
     * at the defined {@code autoSlideDelay} interval.
     */
    private void startAutoSlide() {
        slideTimer = new Timer(autoSlideDelay, e -> showNext());
        slideTimer.start();
    }

    /**
     * Stops the auto-slide timer if it is currently running.
     */
    private void stopAutoSlide() {
        if (slideTimer != null) {
            slideTimer.stop();
        }
    }

    /**
     * Sets a new delay for the auto-slide feature. If the carousel is currently sliding,
     * the timer is stopped and restarted with the new delay.
     *
     * @param milliseconds The new delay time in milliseconds.
     */
    public void setAutoSlideDelay(int milliseconds) {
        this.autoSlideDelay = milliseconds;
        if (slideTimer != null && slideTimer.isRunning()) {
            stopAutoSlide();
            startAutoSlide();
        }
    }
}