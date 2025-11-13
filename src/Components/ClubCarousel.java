package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import Models.Club;
import Models.User;
import Repository.ImageDatabaseHandler;

import java.sql.Connection;
import java.util.List;
public class ClubCarousel extends JPanel {
    private final List<Club> clubs;
    private int currentIndex = 0;
    private final JPanel cardContainer;
    private Timer slideTimer;
    private int autoSlideDelay = 5000; // 5 seconds
    private final Color bgColor = new Color(45, 45, 45);

    /**
     * Constructs a {@code ClubCarousel} panel.
     * This component displays a list of {@link Models.Club} cards in a rotating carousel view
     * with manual navigation controls and an automatic sliding feature.
     *
     * @param clubs The {@code List} of {@link Models.Club} objects to be displayed.
     * @param ih The {@link Repository.ImageDatabaseHandler} for handling club logo images within the cards.
     * @param user The {@link Models.User} representing the current user, passed to the {@code ClubCard}s.
     * @param conn The active database {@code Connection}, passed to the {@code ClubCard}s for data operations.
     */
    public ClubCarousel(List<Club> clubs, ImageDatabaseHandler ih, User user, Connection conn) {
        this.clubs = clubs;
        setLayout(new BorderLayout());
        setBackground(bgColor);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Card container with CardLayout
        cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(bgColor);

        // Add all club cards
        for (int i = 0; i < clubs.size(); i++) {
            ClubCard card = new ClubCard(clubs.get(i), user, conn, ih);
            cardContainer.add(card, String.valueOf(i));
        }

        // Navigation panel
        JPanel navPanel = createNavigationPanel();

        add(cardContainer, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);

        // Start auto-slide timer
        startAutoSlide();
    }

    /**
     * Creates and configures the navigation panel located at the bottom of the carousel.
     * This panel contains the Previous, Next, Index label, and Pause/Play buttons.
     *
     * @return A configured {@code JPanel} for navigation controls.
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
     * Applies a uniform dark theme style and hover effect to a given button.
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
     * Updates the text of the index label to show the current position and total count
     * in the format "CurrentIndex / TotalClubs".
     *
     * @param label The {@code JLabel} displaying the carousel index.
     */
    private void updateIndexLabel(JLabel label) {
        label.setText((currentIndex + 1) + " / " + clubs.size());
    }

    /**
     * Advances the carousel to the next card in the sequence.
     * The index wraps around to the beginning if the end is reached.
     */
    private void showNext() {
        currentIndex = (currentIndex + 1) % clubs.size();
        CardLayout cl = (CardLayout) cardContainer.getLayout();
        cl.show(cardContainer, String.valueOf(currentIndex));
    }

    /**
     * Moves the carousel to the previous card in the sequence.
     * The index wraps around to the end if the beginning is reached.
     */
    private void showPrevious() {
        currentIndex = (currentIndex - 1 + clubs.size()) % clubs.size();
        CardLayout cl = (CardLayout) cardContainer.getLayout();
        cl.show(cardContainer, String.valueOf(currentIndex));
    }

    /**
     * Initializes and starts the auto-slide timer, which automatically calls {@code showNext()}
     * at the interval defined by {@code autoSlideDelay}.
     */
    private void startAutoSlide() {
        slideTimer = new Timer(autoSlideDelay, e -> showNext());
        slideTimer.start();
    }

    /**
     * Stops the auto-slide timer if it is running.
     */
    private void stopAutoSlide() {
        if (slideTimer != null) {
            slideTimer.stop();
        }
    }

    /**
     * Sets a new delay for the automatic sliding feature.
     * If the carousel is currently auto-sliding, the timer is stopped and restarted with the new delay.
     *
     * @param milliseconds The new delay in milliseconds.
     */
    public void setAutoSlideDelay(int milliseconds) {
        this.autoSlideDelay = milliseconds;
        if (slideTimer != null && slideTimer.isRunning()) {
            stopAutoSlide();
            startAutoSlide();
        }
    }
}