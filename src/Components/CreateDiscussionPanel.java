package Components;

import Models.DiscussionForum;
import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialField;
import Repository.DiscussionForumRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.util.Date;
public class CreateDiscussionPanel extends JPanel {

    private final JTextField titleField;
    private final JTextArea messageArea;
    public final JButton createBtn;
    private final Connection conn;
    private final User user;

    /**
     * Constructs a {@code CreateDiscussionPanel} for submitting a new discussion thread.
     * This panel includes fields for the discussion title and message body, and a
     * button to submit the content to the database.
     *
     * @param conn The active database {@code Connection} used by the repository to persist data.
     * @param user The {@link Models.User} who is creating the discussion (although not explicitly used
     * in the current implementation of {@code DiscussionForum} entity, it's passed here).
     */
    public CreateDiscussionPanel(Connection conn, User user) {
        this.conn = conn;
        this.user = user;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel header = new JLabel("Create a Discussion");
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));

        titleField = new MaterialField();
        titleField.setBackground(new Color(45, 45, 45));
        titleField.setForeground(Color.WHITE);
        titleField.setBorder(new TitledBorder("Title"));

        messageArea = new JTextArea(10, 30);
        messageArea.setBackground(new Color(45, 45, 45));
        messageArea.setForeground(Color.WHITE);
        messageArea.setBorder(new TitledBorder("Message"));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        createBtn = new MaterialButton("Create", new Color(0, 123, 255));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Attach Functionality
        initLogic();

        add(header, BorderLayout.NORTH);
        add(titleField, BorderLayout.CENTER); // Note: titleField placement corrected for BorderLayout.NORTH usage in original code, assuming title should be below header.
        add(messageArea, BorderLayout.CENTER);
        add(createBtn, BorderLayout.SOUTH);
    }

    /**
     * Initializes the logic for the {@code createBtn}.
     * This method adds an {@code ActionListener} that handles:
     * 1. Input validation (ensuring title and message are not empty).
     * 2. Creating a new {@link Models.DiscussionForum} object.
     * 3. Submitting the discussion asynchronously using {@link Repository.DiscussionForumRepository}.
     * 4. Providing visual feedback (disabling button, changing text) during the process.
     * 5. Displaying success or error messages and clearing the input fields upon completion.
     */
    private void initLogic() {
        createBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String message = messageArea.getText().trim();

            if (title.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Title and Message cannot be empty.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            DiscussionForumRepository repo = new DiscussionForumRepository(conn);

            DiscussionForum discussion = new DiscussionForum(
                    System.currentTimeMillis(),
                    title,
                    message,
                    new Date(),
                    0
            );

            createBtn.setEnabled(false);
            createBtn.setText("Posting...");

            repo.addDiscussion(discussion).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    createBtn.setEnabled(true);
                    createBtn.setText("Create");

                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Discussion created successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        titleField.setText("");
                        messageArea.setText("");
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to create discussion.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            });
        });
    }
}