package Components;

import Repository.DiscussionCommentRepository;
import Models.DiscussionForum;
import Models.DiscussionComment;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialScrollPane;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
public class DiscussionDetailPanel extends JPanel {

    private final DiscussionForum discussion;
    private final DiscussionCommentRepository commentRepo;
    private final JPanel commentListPanel;
    private final long currentUserId;

    /**
     * Constructs a {@code DiscussionDetailPanel} to display a single discussion thread
     * and its associated comments, and allows the current user to add new comments.
     *
     * @param discussion The {@link Models.DiscussionForum} object representing the main thread.
     * @param comments The {@code List} of initial {@link Models.DiscussionComment} objects for this thread.
     * @param commentRepo The {@link Repository.DiscussionCommentRepository} for handling database operations related to comments.
     * @param currentUserId The ID of the currently logged-in user, used when creating new comments.
     */
    public DiscussionDetailPanel(DiscussionForum discussion, List<DiscussionComment> comments,
                                 DiscussionCommentRepository commentRepo, long currentUserId) {

        this.discussion = discussion;
        this.commentRepo = commentRepo;
        this.currentUserId = currentUserId;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // ==============================
        // DISCUSSION HEADER
        // ==============================
        JPanel discussionPanel = new JPanel(new BorderLayout());
        discussionPanel.setBackground(new Color(45, 45, 45));
        discussionPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(discussion.title());
        title.setForeground(new Color(0, 200, 255));

        JLabel message = new JLabel("<html><p style='color:#ccc;'>" + discussion.message() + "</p></html>");

        JButton addCommentBtn = new MaterialButton("Add Comment", new Color(0, 123, 255));
        addCommentBtn.setForeground(Color.WHITE);
        addCommentBtn.setFocusPainted(false);
        addCommentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        discussionPanel.add(title, BorderLayout.NORTH);
        discussionPanel.add(message, BorderLayout.CENTER);
        discussionPanel.add(addCommentBtn, BorderLayout.EAST);

        // ==============================
        // COMMENTS LIST PANEL
        // ==============================
        commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));
        commentListPanel.setBackground(new Color(35, 35, 35));

        /**
         * Logic to populate the comments list panel with existing comments provided upon construction.
         */
        if (comments != null) {
            for (DiscussionComment c : comments) {
                commentListPanel.add(createCommentCard(c));
            }
        }

        JScrollPane scrollPane = new MaterialScrollPane(commentListPanel);
        scrollPane.setBorder(null);

        add(discussionPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ==============================
        // ADD COMMENT ACTION
        // ==============================
        addCommentBtn.addActionListener(e -> {
            JTextArea inputArea = new JTextArea(4, 30);
            inputArea.setLineWrap(true);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(inputArea),
                    "Write a Comment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            /**
             * Handles the submission of a new comment: validates input, creates a
             * new DiscussionComment entity, saves it asynchronously via the repository,
             * and updates the UI upon successful saving.
             */
            if (result == JOptionPane.OK_OPTION) {
                String commentText = inputArea.getText().trim();

                if (!commentText.isEmpty()) {
                    DiscussionComment newComment = new DiscussionComment(
                            System.currentTimeMillis(),          // comment ID
                            commentText,
                            currentUserId,
                            discussion.discussionId(),
                            new Date()
                    );

                    commentRepo.addComment(newComment).thenAccept(success -> {
                        if (success) {
                            SwingUtilities.invokeLater(() -> appendNewComment(newComment));
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to save comment.");
                        }
                    });
                }
            }
        });
    }

    /**
     * Dynamically appends a newly created comment card to the bottom of the comments list panel
     * and refreshes the layout.
     *
     * @param comment The {@link Models.DiscussionComment} to add to the UI.
     */
    private void appendNewComment(DiscussionComment comment) {
        commentListPanel.add(createCommentCard(comment));
        commentListPanel.revalidate();
        commentListPanel.repaint();
    }

    /**
     * Creates a {@code JPanel} component to display the content of a single discussion comment.
     *
     * @param comment The {@link Models.DiscussionComment} containing the data to display.
     * @return A styled {@code JPanel} representing the comment card.
     */
    private JPanel createCommentCard(DiscussionComment comment) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(new EmptyBorder(8, 15, 8, 15));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel msg = new JLabel("<html><p style='color:#ddd;'>" + comment.message() + "</p></html>");
        panel.add(msg, BorderLayout.CENTER);
        return panel;
    }
}