package Components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import Models.Club;
import Models.ClubMember;
import Models.User;
import MaterialSwingUI.MaterialScrollPane;
import MaterialSwingUI.MaterialButton;
import Repository.ClubMemberRepository;
import Repository.ClubRepository;
import Repository.ImageDatabaseHandler;

/**
 * A custom Swing JPanel component designed to display detailed information about a single club.
 * It includes club attributes, member lists, and provides options for joining/leaving,
 * and editing/deactivating the club based on the current user's role and membership status.
 */
public class ClubCard extends JPanel {
    private final Color cardBg = new Color(50, 50, 50);
    private final Color textColor = Color.WHITE;
    private final Color labelColor = new Color(180, 180, 180);
    private final Color memberSectionBg = new Color(40, 40, 40);

    private Club club;
    private final ImageDatabaseHandler ih;
    private Connection conn;
    private final User currentUser;
    private List<ClubMember> clubMembers;
    private final JLabel nameValueLabel, statusValueLabel, categoryValueLabel, descriptionValueLabel;

    // Store reference to the members list container
    private JPanel membersListContainer;

    /**
     * Constructs a {@code ClubCard} panel initialized with club details, the current user,
     * database connection, and image handler.
     * It sets up the card's layout and populates it with club data.
     *
     * @param club The {@link Models.Club} object containing the club details.
     * @param user The {@link Models.User} object representing the currently logged-in user.
     * @param conn The active {@link java.sql.Connection} to the database.
     * @param ih The {@link Repository.ImageDatabaseHandler} for loading club logo images.
     */
    public ClubCard(Club club, User user, Connection conn, ImageDatabaseHandler ih) {
        this.club = club;
        this.conn = conn;
        this.currentUser = user;
        this.ih = ih;
        this.clubMembers = new ArrayList<>();
        loadMembers();

        setLayout(new BorderLayout());
        setBackground(cardBg);
        setPreferredSize(new Dimension(350, 700));
        setBorder(createRoundedBorder());

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        Color headerBg = new Color(0, 90, 80);
        header.setBackground(headerBg);
        header.setPreferredSize(new Dimension(350, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel logoLabel = getCirclePanel(ih.getClubLogo(club.clubId()));
        header.add(logoLabel, BorderLayout.WEST);

        JLabel clubNameLabel = new JLabel(club.name());
        clubNameLabel.setForeground(Color.WHITE);
        clubNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        clubNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(clubNameLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setOpaque(false);

        if (hasPrivilege()) {
            JButton editBtn = new MaterialButton("Edit", new Color(25, 60, 200));
            JButton deleteBtn = new MaterialButton("Deactivate", new Color(180, 50, 50));

            editBtn.addActionListener(e -> onEdit());
            deleteBtn.addActionListener(e -> onDelete());

            buttonsPanel.add(editBtn);
            buttonsPanel.add(deleteBtn);
        }

        header.add(buttonsPanel, BorderLayout.EAST);

        // ===== CONTENT =====
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(cardBg);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        nameValueLabel = new JLabel(club.name());
        statusValueLabel = new JLabel(club.status());
        categoryValueLabel = new JLabel(club.category());
        descriptionValueLabel = new JLabel("<html><p style='width:250px;'>" + club.description() + "</p></html>");

        content.add(createFieldPanel("Name", nameValueLabel));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Status", statusValueLabel));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Category", categoryValueLabel));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Description", descriptionValueLabel));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Budget Proposal", new JLabel(String.valueOf(club.budgetProposal()))));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Member Capacity", new JLabel(String.valueOf(club.memberCapacity()))));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Approved Budget", new JLabel(String.valueOf(club.approvedBudget()))));
        content.add(Box.createVerticalStrut(10));
        content.add(createFieldPanel("Created Date", new JLabel(club.createdDate())));
        content.add(Box.createVerticalStrut(20));

        // ===== MEMBERS SECTION =====
        content.add(createMembersSection());

        JScrollPane mainScroll = new MaterialScrollPane(content);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(cardBg);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        if (!hasPrivilege()) {
            JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            joinPanel.setBackground(cardBg);

            JButton joinButton = new MaterialButton((isMember() ? "Leave Club" : "Join Club"), new Color(0, 128, 100));
            joinButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            joinButton.setPreferredSize(new Dimension(150, 35));
            joinButton.addActionListener(e -> onJoin());

            joinPanel.add(joinButton);
            add(joinPanel, BorderLayout.SOUTH);
        }

        add(header, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    /**
     * Creates and configures the panel section dedicated to displaying club members.
     * It includes a header, a status filter dropdown, and a container for member items.
     *
     * @return A {@code JPanel} containing the members section UI.
     */
    private JPanel createMembersSection() {
        JPanel membersSection = new JPanel();
        membersSection.setLayout(new BoxLayout(membersSection, BoxLayout.Y_AXIS));
        membersSection.setBackground(memberSectionBg);
        membersSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        membersSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -------- HEADER + FILTER --------
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(memberSectionBg);

        JLabel membersTitleLabel = new JLabel("Club Members");
        membersTitleLabel.setForeground(textColor);
        membersTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JComboBox<String> statusFilter = new JComboBox<>(new String[]{
                "All", "Pending", "Accepted", "Rejected"
        });
        statusFilter.addActionListener(e -> refreshFilteredMembers(statusFilter.getSelectedItem().toString()));

        sectionHeader.add(membersTitleLabel, BorderLayout.WEST);
        sectionHeader.add(statusFilter, BorderLayout.EAST);

        membersSection.add(sectionHeader);
        membersSection.add(Box.createVerticalStrut(10));

        // Members list container - Store as instance variable
        membersListContainer = new JPanel();
        membersListContainer.setLayout(new BoxLayout(membersListContainer, BoxLayout.Y_AXIS));
        membersListContainer.setBackground(memberSectionBg);

        membersSection.add(membersListContainer);

        // Initial load - now the container exists
        refreshFilteredMembers("All");

        return membersSection;
    }

    /**
     * Creates a header panel for the members section, showing the title and current member count.
     *
     * @return A {@code JPanel} configured as the members section header.
     */
    private JPanel getHeader() {
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(memberSectionBg);
        sectionHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel membersTitleLabel = new JLabel("Club Members");
        membersTitleLabel.setForeground(textColor);
        membersTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel membersCountLabel = new JLabel(clubMembers != null ?
                "(" + clubMembers.size() + ")" : "(0)");
        membersCountLabel.setForeground(labelColor);
        membersCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        sectionHeader.add(membersTitleLabel, BorderLayout.WEST);
        sectionHeader.add(membersCountLabel, BorderLayout.EAST);
        return sectionHeader;
    }

    /**
     * Creates a panel component to display a single club member's information.
     * Includes user ID, role, status, and action buttons (Approve/Promote/Remove) if the current user has privilege.
     *
     * @param member The {@link Models.ClubMember} object to display.
     * @return A {@code JPanel} representing the member list item.
     */
    private JPanel createMemberItem(ClubMember member) {
        JPanel memberItem = new JPanel(new BorderLayout());
        memberItem.setBackground(memberSectionBg);
        memberItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        memberItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(memberSectionBg);

        JLabel nameLabel = new JLabel(String.valueOf(member.user_id()));
        nameLabel.setForeground(textColor);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel roleLabel = new JLabel(member.membershipRole());
        roleLabel.setForeground(new Color(139, 195, 74));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        infoPanel.add(nameLabel);
        infoPanel.add(roleLabel);

        JLabel statusLabel = new JLabel("Status: " + member.membershipStatus());
        statusLabel.setForeground(labelColor);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(statusLabel);

        memberItem.add(infoPanel, BorderLayout.CENTER);

        // ===== ACTION BUTTONS ONLY FOR LEADERS/ADMINS =====
        if (hasPrivilege()) {
            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actionsPanel.setOpaque(false);

            if ("pending".equalsIgnoreCase(member.membershipStatus())) {
                JButton approveBtn = new MaterialButton("Approve", new Color(0, 140, 90));
                approveBtn.addActionListener(e -> approveMember(member));
                actionsPanel.add(approveBtn);
            }

            // Promote button (only show if member is NOT already leader)
            if (!"leader".equalsIgnoreCase(member.membershipRole())) {
                JButton promoteBtn = new MaterialButton("Promote", new Color(50, 75, 200));
                promoteBtn.addActionListener(e -> promoteToLeader(member));
                actionsPanel.add(promoteBtn);
            }

            JButton removeBtn = new MaterialButton("Remove", new Color(180, 40, 40));
            removeBtn.addActionListener(e -> removeMember(member));
            actionsPanel.add(removeBtn);

            memberItem.add(actionsPanel, BorderLayout.EAST);
        }

        return memberItem;
    }

    /**
     * Checks if the current user is an accepted member of the club, or if they are a system administrator (roleId=1).
     *
     * @return {@code true} if the current user is an accepted member or an admin, {@code false} otherwise.
     */
    private boolean isMember() {
        ClubMemberRepository cmr = new ClubMemberRepository(conn);
        try {
            Optional<ClubMember> member = cmr.getMemberById(currentUser.userId()).get();

            boolean isMember = member.isPresent() && "accepted".equalsIgnoreCase(member.get().membershipStatus());

            return isMember;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the current user has administrative privileges over this club.
     * This is true if the user is a system administrator (roleId=1) or an accepted club leader.
     *
     * @return {@code true} if the current user has edit/deactivate privileges, {@code false} otherwise.
     */
    private boolean hasPrivilege() {
        ClubMemberRepository cmr = new ClubMemberRepository(conn);
        try {
            Optional<ClubMember> member = cmr.getMemberById(currentUser.userId()).get();

            boolean isLeader = member.isPresent() &&
                    "leader".equalsIgnoreCase(member.get().membershipRole()) &&
                    "accepted".equalsIgnoreCase(member.get().membershipStatus());

            return currentUser.roleId() == 1 || isLeader;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles the user's action to join or leave the club.
     * If the user is already a member, it prompts for confirmation to leave.
     * If not a member, it prompts for confirmation to send a join request (pending status).
     * The method updates the database asynchronously and shows feedback messages.
     */
    private void onJoin() {
        if (isMember()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Would you like to leave this club?",
                    "Leave Club",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                ClubMemberRepository clubMemberRepository = new ClubMemberRepository(this.conn);
                clubMemberRepository.leaveClub(club.clubId(), currentUser.userId()).thenAcceptAsync(success -> {
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "You left club",
                                    "Left Club",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Failed to leave club",
                                    "Leave Club",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                });
            }
        } else {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Would you like to send a request to join this club?",
                    "Join Club",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                ClubMember newMember = new ClubMember(System.currentTimeMillis(),
                        currentUser.userId(),
                        club.clubId(),
                        "pending",
                        "member",
                        String.valueOf(LocalDateTime.now()),
                        null,
                        null,
                        null,
                        0);
                ClubMemberRepository clubMemberRepository = new ClubMemberRepository(this.conn);
                clubMemberRepository.addMember(newMember).thenAcceptAsync(success -> {
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Join request sent successfully!\nPlease wait for approval.",
                                    "Request Sent",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Join request not sent successfully!\nPlease wait for approval.",
                                    "Request Sent",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                });
            }
        }
    }

    /**
     * Displays a modal dialog allowing the user (with privilege) to edit the club's details
     * (name, status, category, description).
     * Upon confirmation, the club object is updated, the card's labels are refreshed,
     * and the changes are persisted to the database asynchronously.
     */
    private void onEdit() {
        Color bgColor = new Color(30, 30, 30);
        Color fieldColor = new Color(45, 45, 45);
        Color borderColor = new Color(70, 70, 70);
        Color textColor = new Color(220, 220, 220);
        Color accentColor = new Color(100, 149, 237);

        JTextField nameField = new JTextField(club.name());
        JTextField statusField = new JTextField(club.status());
        JTextField categoryField = new JTextField(club.category());
        JTextArea descriptionArea = new JTextArea(club.description(), 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        for (JTextField field : new JTextField[]{nameField, statusField, categoryField}) {
            field.setBackground(fieldColor);
            field.setForeground(textColor);
            field.setCaretColor(accentColor);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1, true),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        descriptionArea.setBackground(fieldColor);
        descriptionArea.setForeground(textColor);
        descriptionArea.setCaretColor(accentColor);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JScrollPane scrollPane = new MaterialScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                "Description", 0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                accentColor
        ));
        scrollPane.getViewport().setBackground(fieldColor);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        JLabel nameLabel = new JLabel("Club Name:");
        JLabel statusLabel = new JLabel("Status:");
        JLabel categoryLabel = new JLabel("Category:");
        nameLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        categoryLabel.setFont(labelFont);
        nameLabel.setForeground(textColor);
        statusLabel.setForeground(textColor);
        categoryLabel.setForeground(textColor);

        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(statusLabel, gbc);
        gbc.gridx = 1;
        panel.add(statusField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        UIManager.put("OptionPane.background", bgColor);
        UIManager.put("Panel.background", bgColor);
        UIManager.put("OptionPane.messageForeground", textColor);

        int result = JOptionPane.showConfirmDialog(
                this, panel, " Edit Club Information",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            club = new Club(
                    club.clubId(),
                    nameField.getText(),
                    statusField.getText(),
                    categoryField.getText(),
                    descriptionArea.getText(),
                    club.budgetProposal(),
                    club.memberCapacity(),
                    club.approvedBudget(),
                    club.approvedBy(),
                    club.createdDate(),
                    club.createdBy()
            );
            nameValueLabel.setText(club.name());
            statusValueLabel.setText(club.status());
            categoryValueLabel.setText(club.category());
            descriptionValueLabel.setText("<html><p style='width:250px; color:white;'>" + club.description() + "</p></html>");
            ClubRepository clubRepository = new ClubRepository(this.conn);
            clubRepository.updateClub(club).thenAcceptAsync(success -> {
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "Club information updated successfully!",
                                "Updated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "Club information not updated successfully!",
                                "Updated",
                                JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
            });
        }
    }

    /**
     * Handles the club deactivation action (available to privileged users).
     * Displays a confirmation dialog and, if confirmed, calls the repository to
     * delete the club asynchronously, then removes the card from its parent container.
     */
    private void onDelete() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate this club?\nThis action cannot be undone.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ClubRepository clubRepository = new ClubRepository(this.conn);
            clubRepository.deleteClub(club.clubId()).thenAcceptAsync(success -> {
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Club deactivated successfully!", "Deactivated", JOptionPane.INFORMATION_MESSAGE);
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Club not deactivated successfully!", "Deactivated", JOptionPane.ERROR_MESSAGE);
                        Container parent = getParent();
                        if (parent != null) {
                            parent.remove(this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                }
            });
        }
    }

    /**
     * Creates a simple panel displaying a label (e.g., "Name") and its corresponding value label.
     *
     * @param label      The descriptive label text.
     * @param valueLabel The {@code JLabel} containing the club attribute's value.
     * @return A {@code JPanel} formatted to display a single club field.
     */
    private JPanel createFieldPanel(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(labelColor);
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setForeground(textColor);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);

        return panel;
    }

    /**
     * Creates a custom JPanel that renders a circular area and draws the provided image (club logo)
     * inside it. If no image is provided, it draws a default "No Image" placeholder.
     *
     * @param profileImage The {@code Image} object for the club logo, or {@code null}.
     * @return A {@code JPanel} with custom drawing logic for the circular logo.
     */
    private static JPanel getCirclePanel(Image profileImage) {
        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int diameter = Math.min(getWidth(), getHeight());
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                Shape circle = new java.awt.geom.Ellipse2D.Double(x, y, diameter, diameter);
                g2.setClip(circle);

                if (profileImage != null) {
                    g2.drawImage(profileImage, x, y, diameter, diameter, this);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fill(circle);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString("No Image", x + diameter / 6, y + diameter / 2);
                }
                g2.setClip(null);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(circle);

                g2.dispose();
            }
        };

        circlePanel.setPreferredSize(new Dimension(60, 60));
        circlePanel.setOpaque(false);
        return circlePanel;
    }

    /**
     * Creates a custom {@code AbstractBorder} that renders a visually rounded background
     * for the club card.
     *
     * @return An {@code AbstractBorder} with rounded corners.
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
     * Fetches the current list of all members for this club from the database
     * and updates the internal {@code clubMembers} list.
     *
     * @throws RuntimeException if the member data retrieval fails.
     */
    private void loadMembers() {
        ClubMemberRepository clubMemberRepository = new ClubMemberRepository(this.conn);
        try {
            clubMembers = clubMemberRepository.getAllMembers(club.clubId()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the current list of member items and repopulates it with members
     * filtered by the specified status (e.g., "All", "Pending", "Accepted").
     *
     * @param filter The membership status to filter by, or "All" for all members.
     */
    private void refreshFilteredMembers(String filter) {
        if (membersListContainer == null) {
            System.err.println("Error: membersListContainer is null.");
            return;
        }

        membersListContainer.removeAll();

        clubMembers.stream()
                .filter(m -> filter.equals("All") || m.membershipStatus().equalsIgnoreCase(filter))
                .forEach(member -> membersListContainer.add(createMemberItem(member)));

        membersListContainer.revalidate();
        membersListContainer.repaint();
    }

    /**
     * Approves a pending club member. Updates the member's status in the database
     * asynchronously and refreshes the member list upon completion.
     *
     * @param member The {@link Models.ClubMember} to approve.
     */
    private void approveMember(ClubMember member) {
        ClubMemberRepository repo = new ClubMemberRepository(conn);
        repo.approveMember(member.member_id()).thenAcceptAsync(success -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        success ? "Member Approved!" : "Failed to approve",
                        "Approval", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
                loadMembers();
                refreshFilteredMembers("All");
            });
        });
    }

    /**
     * Removes a member from the club. Displays a confirmation dialog before
     * removing the member asynchronously and refreshing the member list.
     *
     * @param member The {@link Models.ClubMember} to remove.
     */
    private void removeMember(ClubMember member) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this member from the club?",
                "Remove Member",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ClubMemberRepository repo = new ClubMemberRepository(conn);
            repo.removeMember(member.member_id()).thenAcceptAsync(success -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            success ? "Member removed." : "Failed to remove member.",
                            "Remove Member", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                    );
                    loadMembers();
                    refreshFilteredMembers("All");
                });
            });
        }
    }

    /**
     * Promotes an existing club member to a leader role. Displays a confirmation dialog
     * before promoting the member asynchronously and refreshing the member list.
     *
     * @param member The {@link Models.ClubMember} to promote.
     */
    private void promoteToLeader(ClubMember member) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Promote this member to leader?\nCurrent leaders will remain leaders.",
                "Promote Member",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ClubMemberRepository repo = new ClubMemberRepository(conn);
            repo.promoteToLeader(member.member_id()).thenAcceptAsync(success -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            this,
                            success ? "Member promoted to leader!" : "Failed to promote member.",
                            "Promotion Result",
                            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                    );

                    loadMembers();
                    refreshFilteredMembers("All");
                });
            });
        }
    }
}