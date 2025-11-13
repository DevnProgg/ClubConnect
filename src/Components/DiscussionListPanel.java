package Components;

import Models.DiscussionForum;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
public class DiscussionListPanel extends JPanel {

    public final JButton yourClubBtn;
    public final JButton allDiscussionsBtn;
    public final JButton createBtn;

    private JPanel discussionListPanel;
    private List<DiscussionForum> discussions;

    /**
     * Listener callback interface used to notify a parent view when a discussion
     * card in the list has been clicked, triggering navigation to the detail view.
     */
    public interface DiscussionClickListener {
        /**
         * Called when a discussion card is selected by the user.
         *
         * @param forum The {@link Models.DiscussionForum} object representing the selected discussion.
         */
        void onDiscussionSelected(DiscussionForum forum);
    }

    private DiscussionClickListener discussionClickListener;

    /**
     * Constructs a {@code DiscussionListPanel}, which displays a scrollable list of discussion threads.
     * It includes tab buttons for filtering and a button to create a new discussion.
     *
     * @param discussions The initial {@code List} of {@link Models.DiscussionForum} objects to display.
     */
    public DiscussionListPanel(List<DiscussionForum> discussions) {
        this.discussions = discussions;

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // === Top Tabs ===
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabPanel.setBackground(new Color(30, 30, 30));

        yourClubBtn = createTabButton("Your Club Discussions");
        allDiscussionsBtn = createTabButton("All Discussions");

        tabPanel.add(yourClubBtn);
        tabPanel.add(allDiscussionsBtn);

        // === Discussion List Wrapper ===
        discussionListPanel = new JPanel();
        discussionListPanel.setLayout(new BoxLayout(discussionListPanel, BoxLayout.Y_AXIS));
        discussionListPanel.setBackground(new Color(35, 35, 35));

        displayDiscussions();

        JScrollPane scrollPane = new MaterialScrollPane(discussionListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setBackground(new Color(35, 35, 35));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(80, 80, 80);
            }
        });

        // === Create Button ===
        createBtn = new MaterialButton("Create Discussion", new Color(0, 123, 255));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.add(createBtn);

        add(tabPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the internal list of discussions and refreshes the displayed list
     * by calling {@code displayDiscussions()}. This is typically used after fetching
     * new data from the database (e.g., after filtering).
     *
     * @param newList The new {@code List} of {@link Models.DiscussionForum} objects to display.
     */
    public void updateDiscussions(List<DiscussionForum> newList) {
        this.discussions = newList;
        displayDiscussions();
    }

    /**
     * Clears the current discussion list panel and dynamically draws a card for
     * every discussion in the {@code discussions} list. If the list is empty, a "No data" message is displayed.
     */
    private void displayDiscussions() {
        discussionListPanel.removeAll();

        if (discussions == null || discussions.isEmpty()) {
            JLabel noData = new JLabel("No discussions available.");
            noData.setForeground(Color.LIGHT_GRAY);
            noData.setBorder(new EmptyBorder(15, 0, 15, 0));
            discussionListPanel.add(noData);
        } else {
            for (DiscussionForum d : discussions) {
                discussionListPanel.add(createDiscussionCard(d));
            }
        }

        discussionListPanel.revalidate();
        discussionListPanel.repaint();
    }

    /**
     * Sets the listener that will be called when a discussion card is clicked.
     *
     * @param listener The implementation of the {@code DiscussionClickListener} interface.
     */
    public void setDiscussionClickListener(DiscussionClickListener listener) {
        this.discussionClickListener = listener;
    }

    /**
     * Creates a single, interactive JPanel component to display a summary of a discussion thread.
     * The card includes a click listener to trigger navigation via the {@code discussionClickListener}.
     *
     * @param discussion The {@link Models.DiscussionForum} object containing the card data.
     * @return A styled {@code JPanel} representing the discussion card.
     */
    private JPanel createDiscussionCard(DiscussionForum discussion) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel title = new JLabel(discussion.title());
        title.setForeground(new Color(0, 200, 255));

        JLabel msg = new JLabel(
                "<html><p style='color:#aaa;'>" + discussion.message() + "</p></html>"
        );

        panel.add(title, BorderLayout.NORTH);
        panel.add(msg, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (discussionClickListener != null) {
                    discussionClickListener.onDiscussionSelected(discussion);
                }
            }
        });

        return panel;
    }

    /**
     * Creates a uniformly styled button suitable for use as a tab or filter control.
     *
     * @param text The text to display on the button.
     * @return A styled {@code JButton}.
     */
    private JButton createTabButton(String text) {
        JButton btn = new MaterialButton(text, new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(70, 70, 70)));
        return btn;
    }
}