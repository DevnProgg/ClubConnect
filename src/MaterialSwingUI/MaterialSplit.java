package MaterialSwingUI;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
/**
 * A custom {@code JSplitPane} implementation that applies a dark, material-design
 * inspired theme to its divider. It customizes the divider's appearance, including
 * a hover/drag highlight effect and visual grab handles.
 */
public class MaterialSplit extends JSplitPane {

    private final Color dividerColor = new Color(60, 60, 60);      // divider background
    private final Color dragHighlightColor = new Color(90, 90, 90); // highlight on hover/drag

    /**
     * Constructs a {@code MaterialSplit} pane with the specified orientation and components.
     *
     * @param orientation {@code JSplitPane.HORIZONTAL_SPLIT} or {@code JSplitPane.VERTICAL_SPLIT}.
     * @param left The component to the left/top of the divider.
     * @param right The component to the right/bottom of the divider.
     */
    public MaterialSplit(int orientation, Component left, Component right) {
        super(orientation, left, right);
        setContinuousLayout(true);
        setDividerSize(8); // thickness of divider
        setOpaque(false);

        setUI(new DarkSplitPaneUI());
    }

    /**
     * Optional convenience constructor for single component setup.
     *
     * @param orientation {@code JSplitPane.HORIZONTAL_SPLIT} or {@code JSplitPane.VERTICAL_SPLIT}.
     */
    public MaterialSplit(int orientation) {
        this(orientation, null, null);
    }

    /**
     * Custom {@code BasicSplitPaneUI} implementation used to install the {@code DarkDivider}.
     */
    private class DarkSplitPaneUI extends BasicSplitPaneUI {

        /**
         * Overrides the default method to return the custom dark-themed divider.
         *
         * @return A new instance of {@code DarkDivider}.
         */
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new DarkDivider(this);
        }

        /**
         * Custom divider renderer responsible for painting the themed background,
         * hover effect, and grab line.
         */
        private class DarkDivider extends BasicSplitPaneDivider {
            private boolean hovering = false;

            /**
             * Constructs the custom divider, sets basic colors, and adds a mouse listener
             * to detect hover events.
             *
             * @param ui The {@code BasicSplitPaneUI} that owns this divider.
             */
            public DarkDivider(BasicSplitPaneUI ui) {
                super(ui);

                setBackground(dividerColor);
                setBorder(null);

                // Hover effect
                addMouseListener(new java.awt.event.MouseAdapter() {
                    /**
                     * Sets the hover flag and repaints the divider when the mouse enters its area.
                     * @param e The mouse event.
                     */
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    /**
                     * Clears the hover flag and repaints the divider when the mouse exits its area.
                     * @param e The mouse event.
                     */
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            /**
             * Custom painting logic for the divider.
             * It draws the background color, switching to {@code dragHighlightColor} if hovering,
             * and then draws a small visual grab line.
             *
             * @param g The {@code Graphics} context.
             */
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                /**
                 * Fills the divider area, using the highlight color if the mouse is hovering over it.
                 */
                g2.setColor(hovering ? dragHighlightColor : dividerColor);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw grab line (optional aesthetic)
                g2.setColor(new Color(110, 110, 110));
                int mid = getWidth() / 2;
                int midH = getHeight() / 2;
                /**
                 * Draws a pill-shaped indicator in the center of the divider based on its orientation.
                 */
                if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                    g2.fillRoundRect(mid - 2, midH - 20, 4, 40, 4, 4);
                } else {
                    g2.fillRoundRect(mid - 20, midH - 2, 40, 4, 4, 4);
                }

                g2.dispose();
            }
        }
    }
}