package MaterialSwingUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.*;

/**
 * A highly structured {@code JPanel} designed as a flexible layout container
 * for dashboard or complex application views. It divides the main area into
 * several distinct, accessible sections (top-left, top-center, top-right,
 * middle, bottom, and a large right section) using {@code JSplitPane}s
 * with a custom dark/material theme.
 */
public class PageStructure extends JPanel {

    public final JPanel topLeft;
    public final JPanel topCenter;
    public final JPanel topRight;
    public final JPanel rightLarge;
    public final JPanel middle;
    public final JPanel bottom;

    /**
     * Constructs the {@code PageStructure}, initializing all internal panels
     * and setting up the nested {@code JSplitPane} layout.
     */
    public PageStructure() {
        setLayout(new BorderLayout());
        setBackground(new Color(45,45,45));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ***** TOP SECTION *****
        JPanel topContainer = new JPanel(new GridLayout(1, 3, 10, 10));
        topContainer.setOpaque(false);

        topLeft = createRoundedPanel();
        topCenter = createRoundedPanel();
        topRight = createRoundedPanel();

        // Allow stacking cards inside top panels
        /**
         * Sets {@code BoxLayout} for vertical stacking within the top panels, enabling
         * multiple "cards" to be added sequentially.
         */
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        topCenter.setLayout(new BoxLayout(topCenter, BoxLayout.Y_AXIS));
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));

        topContainer.add(topLeft);
        topContainer.add(topCenter);
        topContainer.add(topRight);

        JScrollPane topScroll = wrapScroll(topContainer);

        // ***** MIDDLE SECTION *****
        middle = createRoundedPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));

        // ***** BOTTOM SECTION *****
        bottom = createRoundedPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JScrollPane bottomScroll = wrapScroll(bottom);

        // Split panes
        /**
         * The first vertical split separates the top section from the middle section.
         */
        JSplitPane verticalSplit1 = new MaterialSplit(JSplitPane.VERTICAL_SPLIT, topScroll, middle);
        verticalSplit1.setResizeWeight(0.35);

        /**
         * The second vertical split separates the combination of (top + middle) from the bottom section.
         */
        JSplitPane verticalSplit2 = new MaterialSplit(JSplitPane.VERTICAL_SPLIT, verticalSplit1, bottomScroll);
        verticalSplit2.setResizeWeight(0.75);

        // RIGHT LARGE SECTION
        rightLarge = createRoundedPanel();
        rightLarge.setLayout(new BoxLayout(rightLarge, BoxLayout.Y_AXIS));
        JScrollPane rightScroll = wrapScroll(rightLarge);

        /**
         * The main horizontal split separates the combined vertical sections on the left
         * from the large right section.
         */
        JSplitPane mainSplit = new MaterialSplit(JSplitPane.HORIZONTAL_SPLIT, verticalSplit2, rightScroll);
        mainSplit.setResizeWeight(0.7);

        add(mainSplit, BorderLayout.CENTER);
    }


    /* ---------------------------
        UTIL METHODS FOR CARDS
       --------------------------- */

    /**
     * Adds a component (card) to one of the public section panels and refreshes the layout.
     *
     * @param section The target {@code JPanel} (e.g., {@code topLeft}, {@code middle}).
     * @param card The {@code Component} to be added.
     */
    public void addCard(JPanel section, Component card) {
        section.add(card);
        refresh(section);
    }

    /**
     * Removes a specific component (card) from a section panel and refreshes the layout.
     *
     * @param section The target {@code JPanel}.
     * @param card The {@code Component} to be removed.
     */
    public void removeCard(JPanel section, Component card) {
        section.remove(card);
        refresh(section);
    }

    /**
     * Removes all components (cards) from a specified section panel and refreshes the layout.
     *
     * @param section The target {@code JPanel}.
     */
    public void clearSection(JPanel section) {
        section.removeAll();
        refresh(section);
    }

    /**
     * Forces a panel to revalidate its layout and repaint itself to reflect
     * structural changes (add/remove components).
     *
     * @param panel The {@code JPanel} to be refreshed.
     */
    private void refresh(JPanel panel) {
        panel.revalidate();
        panel.repaint();
    }


    /* ---------------------------
       Internal UI helper methods
       --------------------------- */

    /**
     * Wraps a panel in a {@code MaterialScrollPane}, ensuring a clean, borderless,
     * and non-opaque appearance suitable for the dark theme.
     *
     * @param panel The {@code JPanel} to wrap.
     * @return A styled {@code JScrollPane} instance.
     */
    private JScrollPane wrapScroll(JPanel panel) {
        JScrollPane scroll = new MaterialScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    /**
     * Creates a standard {@code JPanel} that overrides {@code paintComponent} to draw
     * a dark, rounded background, giving it a card-like appearance.
     *
     * @return A styled, rounded {@code JPanel}.
     */
    private JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            /**
             * Overrides {@code paintComponent} to draw a rounded background rectangle
             * with a specific color (70,70,70).
             * @param g The {@code Graphics} context.
             */
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(70,70,70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setBackground(new Color(20,20,20));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }
}