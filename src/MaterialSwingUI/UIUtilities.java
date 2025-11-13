package MaterialSwingUI;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
/**
 * A collection of static utility methods and nested classes for common UI tasks,
 * primarily focusing on custom appearance and component presentation.
 */
public class UIUtilities {
    /**
     * An implementation of {@code AbstractBorder} that draws a rounded background
     * for a component, effectively giving it rounded corners.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;

        /**
         * Constructs a {@code RoundedBorder} with the specified corner radius.
         *
         * @param radius The radius of the corners in pixels.
         */
        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        /**
         * Paints the rounded background of the component, using the component's
         * background color to fill the rounded rectangle.
         *
         * @param c The component for which this border is being painted.
         * @param g The paint graphics.
         * @param x The x position of the border.
         * @param y The y position of the border.
         * @param width The width of the border.
         * @param height The height of the border.
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        /**
         * Returns the insets of the border, providing space inside the border
         * based on half the corner radius.
         *
         * @param c The component for which this border insets value applies.
         * @return An {@code Insets} object providing padding.
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        /**
         * Reinitializes the given insets object with the values of the border insets.
         *
         * @param c The component for which this border insets value applies.
         * @param insets The {@code Insets} object to be reinitialized.
         * @return The reinitialized {@code Insets} object.
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }

    /**
     * Creates and displays a {@code JPanel} within a modal {@code JDialog},
     * effectively presenting the panel as a pop-up window centered on the parent frame.
     *
     * @param parent The parent {@code JFrame} the dialog will be centered relative to.
     * @param panel The {@code JPanel} content to be displayed in the dialog.
     * @param title The title of the dialog window.
     */
    public static void showPanelAsDialog(JFrame parent, JPanel panel, String title) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}