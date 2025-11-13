package Components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialScrollPane;
import Repository.UserRepository;
// User Card Component
/**
 * A custom {@code JPanel} component designed to display detailed information about a single {@link Models.User}.
 * It presents data fields in a stylized card layout and includes administrative actions like
 * Edit and Delete if the current user has the necessary privileges (Role ID 1).
 */
public class UserCard extends JPanel {
    private final Color cardBg = new Color(50, 50, 50);
    private final Color textColor = Color.WHITE;
    private final Color labelColor = new Color(180, 180, 180);
    private final Color statusActiveColor = new Color(76, 175, 80);
    private final Color statusInactiveColor = new Color(244, 67, 54);
    private User user, currentUser;
    private Connection conn;
    private UserRepository userRepository;


    /**
     * Constructs a {@code UserCard}.
     * Initializes the UI, loads user data, and adds edit/delete buttons based on user privileges.
     *
     * @param user The {@link Models.User} whose details are to be displayed.
     * @param conn The active database {@code Connection} used for repository operations.
     */
    public UserCard(User user, Connection conn, User currentUser) {
        this.user = user;
        this.conn = conn;
        this.currentUser = currentUser;
        userRepository = new UserRepository(this.conn);

        setLayout(new BorderLayout());
        setBackground(cardBg);
        setPreferredSize(new Dimension(320, 420));
        setMaximumSize(new Dimension(320, 420));
        setBorder(createRoundedBorder());

        // Header with username
        JPanel header = new JPanel(new BorderLayout());
        Color headerBg = new Color(139, 195, 74);
        header.setBackground(headerBg);
        header.setPreferredSize(new Dimension(320, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));


        // User name
        JLabel userNameLabel = new JLabel(user.username());
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setOpaque(false);

        /**
         * Logic to check user privilege (Role ID 1 for administrative access)
         * and conditionally display Edit and Delete buttons.
         */
        if (hasPrivilege()) {
            JButton editBtn =  new MaterialButton("Edit", new Color(25, 60, 200));
            JButton deleteBtn =  new MaterialButton("Delete", new Color(180, 50, 50));

            editBtn.addActionListener(e -> onEdit());
            deleteBtn.addActionListener(e -> {
                try {
                    onDelete();
                } catch (ExecutionException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            buttonsPanel.add(editBtn);
            buttonsPanel.add(deleteBtn);
        }
        header.add(buttonsPanel, BorderLayout.EAST);
        header.add(userNameLabel, BorderLayout.CENTER);

        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(cardBg);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(Box.createVerticalStrut(12));

        // Add fields
        content.add(createFieldPanel("PK", "User ID", Long.toString(user.userId())));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("", "Full Names", user.fullNames()));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("", "Email", user.email()));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("", "Username", user.username()));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("FK", "Role ID", Long.toString(user.roleId())));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatusPanel(user.status()));
        content.add(Box.createVerticalStrut(10));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("", "Registration Date", user.registrationDate()));

        add(header, BorderLayout.NORTH);

        JScrollPane mainScroll = new MaterialScrollPane(content);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(cardBg);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScroll, BorderLayout.CENTER);
    }


    /**
     * Creates a panel dedicated to displaying the user's profile picture label (placeholder functionality).
     *
     * @return A styled {@code JPanel} for profile display.
     */
    private JPanel createProfileDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fieldLabel = new JLabel("Profile Picture");
        fieldLabel.setForeground(labelColor);
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel profileContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        profileContainer.setBackground(cardBg);
        profileContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(profileContainer);

        return panel;
    }

    /**
     * Creates a vertically aligned panel for displaying a labeled data field (key-value pair).
     *
     * @param prefix A short prefix (e.g., "PK", "FK") to label the field type.
     * @param label The descriptive label of the field (key).
     * @param value The value of the field.
     * @return A styled {@code JPanel} containing the labeled field.
     */
    private JPanel createFieldPanel(String prefix, String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelPanel = getLabelPanel(prefix, label);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(textColor);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(labelPanel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);

        return panel;
    }

    /**
     * Creates a horizontal panel to display the field label along with an optional prefix.
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
     * Creates a panel specifically for displaying the user's status, including a colored indicator dot.
     *
     * @param status The status string (e.g., "Active", "Inactive").
     * @return A styled {@code JPanel} displaying the status.
     */
    private JPanel createStatusPanel(String status) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fieldLabel = new JLabel("Status");
        fieldLabel.setForeground(labelColor);
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusContainer.setBackground(cardBg);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(textColor);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Status indicator dot
        JPanel dot = getDot(status);

        statusContainer.add(dot);
        statusContainer.add(Box.createHorizontalStrut(8));
        statusContainer.add(statusLabel);

        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(statusContainer);

        return panel;
    }

    /**
     * Checks if the user associated with this card has administrative privileges (Role ID 1).
     *
     * @return {@code true} if the user has privilege, {@code false} otherwise.
     */
    private boolean hasPrivilege() {
        return currentUser.roleId() == 1;
    }

    /**
     * Creates a small custom JPanel that draws a colored circle (dot) indicating the user's status.
     *
     * @param status The status string used to determine the dot color.
     * @return A small, custom painted {@code JPanel}.
     */
    private JPanel getDot(String status) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (status.equalsIgnoreCase("Active")) {
                    g2.setColor(statusActiveColor);
                } else {
                    g2.setColor(statusInactiveColor);
                }
                g2.fillOval(2, 2, 8, 8);
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);
        return dot;
    }

    /**
     * Handles the edit functionality by displaying a modal dialog where administrative users
     * can modify key user fields (Full Names, Status, Username).
     * The changes are saved asynchronously to the database.
     */
    private void onEdit() {
        Color bgColor = new Color(30, 30, 30);
        Color fieldColor = new Color(45, 45, 45);
        Color borderColor = new Color(70, 70, 70);
        Color textColor = new Color(220, 220, 220);
        Color accentColor = new Color(100, 149, 237);

        JTextField nameField = new JTextField(user.fullNames());
        JTextField emailField = new JTextField(user.email());
        JTextField usernameField = new JTextField(user.username());
        JTextField statusField = new JTextField(user.status());

        for (JTextField field : new JTextField[]{nameField, statusField, usernameField}) {
            field.setBackground(fieldColor);
            field.setForeground(textColor);
            field.setCaretColor(accentColor);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1, true),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        JLabel nameLabel = new JLabel("User Fullname:");
        JLabel statusLabel = new JLabel("Status:");
        JLabel usernameLabel = new JLabel("Username :");
        nameLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        usernameLabel.setFont(labelFont);
        nameLabel.setForeground(textColor);
        statusLabel.setForeground(textColor);
        usernameLabel.setForeground(textColor);

        panel.add(nameLabel, gbc); gbc.gridx = 1;
        panel.add(nameField, gbc); gbc.gridx = 0; gbc.gridy++;
        panel.add(statusLabel, gbc); gbc.gridx = 1;
        panel.add(statusField, gbc); gbc.gridx = 0; gbc.gridy++;
        panel.add(usernameLabel, gbc); gbc.gridx = 1;
        panel.add(usernameField, gbc); gbc.gridx = 0; gbc.gridy++;

        UIManager.put("OptionPane.background", bgColor);
        UIManager.put("Panel.background", bgColor);
        UIManager.put("OptionPane.messageForeground", textColor);

        int result = JOptionPane.showConfirmDialog(
                this, panel, " Edit User Information",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            user = new User(
                    user.userId(),
                    nameField.getText(),
                    emailField.getText(),
                    usernameField.getText(),
                    user.passwordHash(),
                    user.roleId(),
                    statusField.getText(),
                    user.registrationDate()
            );
            userRepository.updateUser(user).thenAcceptAsync(success->{
                if(success){
                    SwingUtilities.invokeLater(()->{
                        JOptionPane.showMessageDialog(
                                this,
                                "User information updated successfully!",
                                "Updated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                }else{
                    JOptionPane.showMessageDialog(
                            this,
                            "User information not updated successfully!",
                            "Updated",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });
        }
    }


    /**
     * Handles the delete functionality. Displays a confirmation dialog and, if confirmed,
     * deletes the user from the database and removes the card component from its parent panel.
     *
     * @throws ExecutionException if the asynchronous delete operation fails to complete.
     * @throws InterruptedException if the current thread is interrupted while waiting for deletion.
     */
    private void onDelete() throws ExecutionException, InterruptedException {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this user?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if(userRepository.deleteUser(user.userId()).get()){
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                Container parent = getParent();
                if (parent != null) {
                    parent.remove(this);
                    parent.revalidate();
                    parent.repaint();
                }
            }else {
                JOptionPane.showMessageDialog(this, "User not deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                Container parent = getParent();
                if (parent != null) {
                    parent.remove(this);
                    parent.revalidate();
                    parent.repaint();
                }
            }

        }
    }

    /**
     * Creates a custom {@code Border} that provides a visible rounded border for the card.
     *
     * @return An {@code AbstractBorder} implementation for rounded corners.
     */
    private Border createRoundedBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); // border color, not background
                g2.drawRoundRect(x, y, width - 1, height - 1, 15, 15);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 8, 8, 8);
            }
        };
    }

}