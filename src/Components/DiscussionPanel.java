package Components;

import Models.DiscussionForum;
import Models.DiscussionComment;
import MaterialSwingUI.MaterialScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DiscussionPanel extends JPanel {

    /**
     * Constructs a {@code DiscussionPanel} to display a discussion thread and its comments
     * in a detailed, dark-themed view.
     *
     * @param discussion The {@link Models.DiscussionForum} object containing the main thread content.
     * @param comments The {@code List} of {@link Models.DiscussionComment} objects associated with the discussion.
     */
    public DiscussionPanel(DiscussionForum discussion, List<DiscussionComment> comments) {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Discussion Card
        RoundedPanel discussionCard = new RoundedPanel(20, new Color(33, 33, 33));
        discussionCard.setLayout(new BorderLayout());
        discussionCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(discussion.title());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JTextArea messageArea = new JTextArea(discussion.message());
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setForeground(Color.LIGHT_GRAY);
        messageArea.setBackground(new Color(33, 33, 33));
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel timeLabel = new JLabel("Posted on: " + discussion.timestamp());
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        discussionCard.add(titleLabel, BorderLayout.NORTH);
        discussionCard.add(messageArea, BorderLayout.CENTER);
        discussionCard.add(timeLabel, BorderLayout.SOUTH);

        // Comments Section
        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(new Color(18, 18, 18));
        commentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel commentHeader = new JLabel("Comments");
        commentHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        commentHeader.setForeground(Color.WHITE);
        commentHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentsPanel.add(commentHeader);
        commentsPanel.add(Box.createVerticalStrut(10));

        /**
         * Logic to iterate through the list of comments, creating and adding a
         * visually distinct card for each comment to the comments panel.
         */
        if (comments.isEmpty()) {
            JLabel noComments = new JLabel("No comments yet.");
            noComments.setForeground(Color.GRAY);
            commentsPanel.add(noComments);
        } else {
            for (DiscussionComment comment : comments) {
                RoundedPanel commentCard = new RoundedPanel(15, new Color(40, 40, 40));
                commentCard.setLayout(new BorderLayout());
                commentCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                commentCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel userLabel = new JLabel("User ID: " + comment.userId());
                userLabel.setForeground(new Color(180, 180, 180));
                userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JTextArea commentText = new JTextArea(comment.message());
                commentText.setWrapStyleWord(true);
                commentText.setLineWrap(true);
                commentText.setEditable(false);
                commentText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                commentText.setForeground(Color.WHITE);
                commentText.setBackground(new Color(40, 40, 40));
                commentText.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                JLabel time = new JLabel(comment.timestamp().toString());
                time.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                time.setForeground(Color.GRAY);

                commentCard.add(userLabel, BorderLayout.NORTH);
                commentCard.add(commentText, BorderLayout.CENTER);
                commentCard.add(time, BorderLayout.SOUTH);

                commentsPanel.add(commentCard);
                commentsPanel.add(Box.createVerticalStrut(10));
            }
        }

        add(discussionCard, BorderLayout.NORTH);
        add(new MaterialScrollPane(commentsPanel), BorderLayout.CENTER);
    }

    /**
     * A custom {@code JPanel} implementation that draws a background with rounded corners.
     * This is used to give the discussion and comment cards a modern, soft-edged look.
     */
    static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        /**
         * Constructs a {@code RoundedPanel}.
         *
         * @param radius The radius of the corners in pixels.
         * @param bgColor The background color of the panel.
         */
        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        /**
         * Overrides the paintComponent method to draw a filled round rectangle
         * as the background shape of the panel.
         *
         * @param g The {@code Graphics} context used for drawing.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2.dispose();
        }
    }
}