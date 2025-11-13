package Render;

import Models.DiscussionComment;
import Models.DiscussionForum;
import Models.User;
import Components.CreateDiscussionPanel;
import Components.DiscussionDetailPanel;
import Components.DiscussionListPanel;
import Repository.DiscussionCommentRepository;
import Repository.DiscussionForumRepository;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
/**
 * The main container panel for the Discussion Forum feature.
 * It uses a {@code CardLayout} to switch between three views:
 * the list of discussions, the form to create a new discussion, and the detail view for a single discussion.
 */
public class DiscussionsRender extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Connection conn;
    private final User user;

    // Panels
    private DiscussionListPanel listPanel;
    private CreateDiscussionPanel createPanel;
    private DiscussionDetailPanel detailPanel;

    // Repository
    private final DiscussionForumRepository repo;

    /**
     * Constructs the {@code DiscussionsRender} page.
     * Initializes the database connection, user data, repositories, and the main card layout.
     *
     * @param conn The active database connection.
     * @param user The currently logged-in {@code User}.
     */
    public DiscussionsRender(Connection conn, User user) {
        this.conn = conn;
        this.user = user;
        this.repo = new DiscussionForumRepository(conn);

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        // Initialize UI
        initPanels();
        add(mainPanel, BorderLayout.CENTER);

        // Load data from database initially
        loadDiscussions();
    }

    /**
     * Initializes and configures the individual view panels (List, Create) and adds them to the {@code CardLayout}.
     * Also sets up the action listeners for switching between the List and Create views.
     */
    private void initPanels() {
        listPanel = new DiscussionListPanel(List.of()); // temporary empty list
        createPanel = new CreateDiscussionPanel(conn, user);

        listPanel.setOpaque(false);
        createPanel.setOpaque(false);

        mainPanel.add(listPanel, "LIST");
        mainPanel.add(createPanel, "CREATE");

        // Button routing
        /**
         * Sets up the listener on the List Panel's create button to switch to the "CREATE" card.
         */
        listPanel.createBtn.addActionListener(e -> cardLayout.show(mainPanel, "CREATE"));

        createPanel.createBtn.addActionListener(e -> {
            // Once create is done, CreateDiscussionPanel logic calls repo and
            // then we just return to list & reload database contents
            /**
             * Sets up the listener on the Create Panel's create button to reload data
             * and switch back to the "LIST" card after a successful creation.
             */
            loadDiscussions();
            cardLayout.show(mainPanel, "LIST");
        });
    }

    /**
     * Asynchronously fetches all discussions from the database, updates the {@code DiscussionListPanel}
     * with the new data, and sets up the click listener for viewing discussion details.
     */
    private void loadDiscussions() {
        repo.getAllDiscussions().thenAccept(discussions -> {
            SwingUtilities.invokeLater(() -> {
                listPanel.updateDiscussions(discussions);

                // Add click event to show discussion detail
                listPanel.setDiscussionClickListener(selectedDiscussion -> {
                    loadDiscussionDetail(selectedDiscussion.discussionId());
                });
            });
        });
    }

    /**
     * Asynchronously fetches the details of a specific discussion and its associated comments
     * concurrently, then displays the {@code DiscussionDetailPanel}.
     *
     * @param discussionId The unique identifier of the discussion to load.
     */
    private void loadDiscussionDetail(long discussionId) {
        DiscussionCommentRepository commentRepo = new DiscussionCommentRepository(conn);

        // Fetch discussion AND comments in parallel
        CompletableFuture<Optional<DiscussionForum>> discussionFuture = repo.getDiscussionById(discussionId);
        CompletableFuture<List<DiscussionComment>> commentsFuture = commentRepo.getCommentsByDiscussionId(discussionId);

        /**
         * Combines the results of the discussion and comments fetching futures.
         * If the discussion is found, it creates and shows the detail panel.
         */
        discussionFuture.thenCombine(commentsFuture, (optionalDiscussion, comments) -> {
            optionalDiscussion.ifPresent(discussion -> {
                SwingUtilities.invokeLater(() -> {
                    detailPanel = new DiscussionDetailPanel(discussion, comments, commentRepo, user.userId());
                    detailPanel.setOpaque(false);

                    mainPanel.add(detailPanel, "DETAIL");
                    cardLayout.show(mainPanel, "DETAIL");
                });
            });
            return null;
        });
    }

}