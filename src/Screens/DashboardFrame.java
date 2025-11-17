package Screens;
import Models.User;
import Render.*;
import Repository.ImageDatabaseHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;
/**
 * The main application window for the dashboard interface.
 * This class sets up the primary {@code JFrame}, including the persistent sidebar navigation,
 * the top menu bar, and the central content area managed by a {@code CardLayout} for
 * switching between different application views (pages).
 */
public class DashboardFrame extends JFrame {
    private final Color sidebarBg = new Color(55, 55, 55);
    private final Color selectedBg = new Color(240, 240, 240);
    private final Color topBarBg = new Color(30, 30, 30);
    private final int notificationCount = 3;
    private final Connection conn;
    private User user;
    private final ImageDatabaseHandler imageHandler;
    private final String names;

    // Content area and card layout for switching pages
    private final JPanel contentArea;
    private final CardLayout cardLayout;

    // Menu buttons to track selection
    private JButton[] menuButtons;
    private String[] menuItems;

    /**
     * Constructs the main dashboard frame, initializing the UI components and data handlers.
     *
     * @param conn The active database connection used by the repositories.
     * @param user The currently logged-in {@code User} object.
     */
    public DashboardFrame(Connection conn, User user) {
        setTitle("Dashboard");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        this.conn = conn;
        this.user = user;
        imageHandler = new ImageDatabaseHandler(conn);
        this.names = user.fullNames();
        if(user.roleId() == 1){
            this.menuItems = new String[]{"Dashboard", "Clubs", "Users", "Events", "Discussions"};
        }
        else{
            this.menuItems = new String[]{"Dashboard", "Clubs", "Events", "Discussions"};
        }
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        Color darkBg = new Color(45, 45, 45);
        mainPanel.setBackground(darkBg);

        // Left Sidebar
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Right content area with top bar
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(darkBg);

        // Top Menu Bar
        JPanel topMenu = createTopMenuBar();
        rightPanel.add(topMenu, BorderLayout.NORTH);

        // Main content area with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(darkBg);

        // Add different pages to content area
        try {
            setupPages();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        rightPanel.add(contentArea, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    /**
     * Initializes and adds all application content panels (pages) to the central {@code CardLayout}.
     *
     * @throws ExecutionException If an error occurs during asynchronous data fetching required for a page setup.
     * @throws InterruptedException If the thread is interrupted during page setup.
     */
    private void setupPages() throws ExecutionException, InterruptedException {
        // Dashboard page
        JPanel dashboardWrapper = new JPanel(new BorderLayout());
        dashboardWrapper.setBackground(new Color(45, 45, 45));
        var dashboardRender = new DashboardRender();
        dashboardWrapper.add(dashboardRender, BorderLayout.CENTER);
        contentArea.add(dashboardWrapper, "Dashboard");

        // Clubs page - Using ClubsPagePanel with sample data
        JPanel clubsWrapper = new JPanel(new BorderLayout());
        clubsWrapper.setBackground(new Color(45, 45, 45));
        ClubsRender clubsPage = new ClubsRender(conn, user);
        clubsWrapper.add(clubsPage, BorderLayout.CENTER);
        contentArea.add(clubsWrapper, "Clubs");

        // Users page
        JPanel usersPage = new UsersRender(conn, user);
        contentArea.add(usersPage, "Users");

        // Events page (placeholder)
        JPanel eventsPage = new EventsRender(conn, user);
        contentArea.add(eventsPage, "Events");

        // Discussions page (placeholder)
        JPanel discussionsPage = new DiscussionsRender(this.conn, this.user );
        contentArea.add(discussionsPage, "Discussions");

        //notifications page
        JPanel notificationsPage = new NotificationsRender(conn, user);
        contentArea.add(notificationsPage, "Notifications");
    }

    /**
     * Switches the view in the central content area to the specified page name
     * and updates the visual style of the corresponding sidebar button.
     *
     * @param pageName The name of the card to display in the {@code CardLayout}.
     * @param buttonIndex The index of the button to mark as selected.
     */
    private void switchToPage(String pageName, int buttonIndex) {
        cardLayout.show(contentArea, pageName);

        // Update button styles
        for (int i = 0; i < menuButtons.length; i++) {
            if (i == buttonIndex) {
                menuButtons[i].setBackground(selectedBg);
                menuButtons[i].setForeground(topBarBg);
            } else {
                menuButtons[i].setBackground(sidebarBg);
                menuButtons[i].setForeground(selectedBg);
            }
        }
    }

    /**
     * Creates and configures the left sidebar panel containing the user profile
     * and the main navigation buttons.
     *
     * @return The configured {@code JPanel} representing the sidebar.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));

        // Top section with profile
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(sidebarBg);
        topSection.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        // Profile section
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        profilePanel.setBackground(sidebarBg);

        /**
         * Retrieve the user's profile picture and create a circular panel for it.
         */
        JPanel circlePanel = getCirclePanel(imageHandler.getUserProfilePicture(user.userId()));

        profilePanel.add(circlePanel);

        JLabel nameLabel = new JLabel();
        if (user.fullNames() == null || user.fullNames().isEmpty()){
            nameLabel.setText("Cannot get fullname");
        }
        else {
            nameLabel.setText("Username: " + names);
        }
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        profilePanel.add(nameLabel);
        profilePanel.revalidate();
        profilePanel.repaint();


        topSection.add(profilePanel);
        topSection.add(Box.createVerticalStrut(40));

        // Initialize menu buttons array
        menuButtons = new JButton[menuItems.length];

        // Menu buttons
        for (int i = 0; i < menuItems.length; i++) {
            final int index = i;
            final String pageName = menuItems[i];

            JButton btn = createMenuButton(menuItems[i], i == 0);
            menuButtons[i] = btn;

            // Add click listener to switch pages
            btn.addActionListener(e -> switchToPage(pageName, index));

            topSection.add(btn);
            topSection.add(Box.createVerticalStrut(15));
        }

        // Bottom section with logout
        JPanel bottomSection = getBottomSection();

        sidebar.add(topSection, BorderLayout.NORTH);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Creates a custom {@code JPanel} that renders a circular area, optionally displaying an image within the circle.
     * If no image is provided, a placeholder text is shown.
     *
     * @param profileImage The image to be displayed, or {@code null} for a placeholder.
     * @return The configured circular image panel.
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

                /**
                 * Use Ellipse2D to define a circular clip mask for the image.
                 */
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
     * Creates and configures the bottom section of the sidebar, primarily containing the Logout button.
     *
     * @return The configured {@code JPanel} for the bottom section.
     */
    private JPanel getBottomSection() {
        JPanel bottomSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        bottomSection.setBackground(sidebarBg);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(sidebarBg);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setContentAreaFilled(false);

        // Hover effect
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(70, 70, 70));
                logoutBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(sidebarBg);
                logoutBtn.repaint();
            }
        });


        // Logout action
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                user = null;
                SwingUtilities.invokeLater(()->{
                    new LoginFrame(conn).setVisible(true);
                });
                dispose();

            }
        });

        bottomSection.add(logoutBtn);
        return bottomSection;
    }

    /**
     * Creates and styles a standard menu navigation button for the sidebar.
     * Includes hover effects and initial selection styling.
     *
     * @param text The text label for the button.
     * @param selected {@code true} if the button should be styled as currently selected, {@code false} otherwise.
     * @return The configured {@code JButton}.
     */
    private JButton createMenuButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setMaximumSize(new Dimension(getWidth(), 50));
        button.setPreferredSize(new Dimension(getWidth(), 50));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        // Apply base style
        if (selected) {
            button.setBackground(selectedBg);
            button.setForeground(topBarBg);
        } else {
            button.setBackground(sidebarBg);
            button.setForeground(selectedBg);
        }

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getBackground() != selectedBg) {
                    button.setBackground(new Color(70, 70, 70));
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getBackground() != selectedBg) {
                    button.setBackground(sidebarBg);
                    button.repaint();
                }
            }
        });

        // Left-align text
        button.setHorizontalAlignment(SwingConstants.RIGHT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        return button;
    }


    /**
     * Creates and configures the top menu bar, including the notification badge/icon.
     *
     * @return The configured {@code JPanel} representing the top bar.
     */
    private JPanel createTopMenuBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(topBarBg);
        topBar.setPreferredSize(new Dimension(getWidth(), 60));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        // Right side with notification button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightPanel.setBackground(topBarBg);

        // Notification button with badge
        JPanel notificationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw bell icon (simple representation)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));

                // Bell shape
                int x = 10;
                int y = 5;
                g2.drawArc(x, y, 20, 20, 0, -180);
                g2.drawLine(x + 2, y + 20, x + 2, y + 25);
                g2.drawLine(x + 18, y + 20, x + 18, y + 25);
                g2.drawLine(x, y + 25, x + 20, y + 25);

                // Badge
                if (notificationCount > 0) {
                    g2.setColor(Color.RED);
                    g2.fillOval(x + 18, y - 2, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String count = notificationCount > 9 ? "9+" : String.valueOf(notificationCount);
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(count);
                    g2.drawString(count, x + 25 - textWidth / 2, y + 8);
                }
            }
        };
        notificationPanel.setPreferredSize(new Dimension(40, 30));
        notificationPanel.setOpaque(false);
        notificationPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        notificationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToPage("Notifications", 0);
            }
        });

        rightPanel.add(notificationPanel);

        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }
}