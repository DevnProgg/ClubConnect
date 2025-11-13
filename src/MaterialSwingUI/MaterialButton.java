package MaterialSwingUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Custom rounded button

/**
 * A custom {@code JButton} implementation that renders itself with a material-design
 * aesthetic, featuring a solid colored background with rounded corners and a distinct
 * hover effect.
 */
public class MaterialButton extends JButton {

    private final Color backgroundColor;
    private int cornerRadius = 20;

    /**
     * Constructs a {@code MaterialButton} with the specified text and background color.
     *
     * @param text The text to display on the button.
     * @param bgColor The primary background {@code Color} of the button.
     */
    public MaterialButton(String text, Color bgColor) {
        super(text);
        this.backgroundColor = bgColor;

        // Base visual settings
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
        setBorder(null);

        updatePadding();
    }

    /**
     * Calculates and sets the button's internal padding based on the current font size.
     */
    private void updatePadding() {
        int padding = getFont().getSize() / 2 + 6;
        setBorder(BorderFactory.createEmptyBorder(padding, padding * 2, padding, padding * 2));
    }

    /**
     * Overrides the preferred size to ensure the button has a minimum size
     * and adjusts the corner radius to match the height for pill-shaped corners.
     *
     * @return The calculated preferred {@code Dimension}.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += 12;
        size.height += 8;
        /**
         * Sets the corner radius to match the button's height, effectively making it pill-shaped.
         */
        cornerRadius = size.height;
        return size;
    }

    /**
     * Custom painting method to draw the rounded background and hover effect.
     *
     * @param g The {@code Graphics} context used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Hover outline
        if (getModel().isRollover()) {
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(255, 255, 255, 180));
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}