package MaterialSwingUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
/**
 * A custom {@code JTextField} component that implements a material design aesthetic.
 * It features a rounded background, a subtle border, and custom placeholder text
 * that is displayed when the field is empty.
 */
public class MaterialField extends JTextField {
    private final String placeholder;

    /**
     * Constructs a default {@code MaterialField} with no initial placeholder text and default columns.
     */
    public MaterialField() {
        super();
        placeholder = "";
        setBackground(new Color (35,35,35));
        setOpaque(false);
        setFont(new Font("Arial", Font.PLAIN, 14));
        setForeground(new Color(120, 120, 120));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    /**
     * Constructs a {@code MaterialField} with specified placeholder text.
     *
     * @param placeholder The text to display when the field is empty.
     */
    public MaterialField(String placeholder){
        super(placeholder);
        this.placeholder = placeholder;
        setBackground(new Color (35,35,35));
        setOpaque(false);
        setFont(new Font("Arial", Font.PLAIN, 14));
        setForeground(new Color(120, 120, 120));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    /**
     * Constructs a {@code MaterialField} with specified placeholder text and column width.
     *
     * @param placeholder The text to display when the field is empty.
     * @param columns The number of columns for the field's preferred width.
     */
    public MaterialField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        setBackground(new Color (35,35,35));
        setOpaque(false);
        setFont(new Font("Arial", Font.PLAIN, 14));
        setForeground(new Color(120, 120, 120));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    /**
     * Custom painting method to draw the rounded background and border of the text field.
     *
     * @param g The {@code Graphics} context used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /**
         * Fills the component area with a rounded rectangle for the background.
         */
        g2.setColor(new Color(55,55,55));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
        /**
         * Draws a thin rounded rectangle as the field's border.
         */
        g2.setColor(new Color(100, 100, 100));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 50, 50));
        super.paintComponent(g);
        g2.dispose();
    }

    /**
     * Overrides the default border painting to prevent the default look, as the border
     * is drawn as part of {@code paintComponent}.
     *
     * @param g The {@code Graphics} context.
     */
    @Override
    protected void paintBorder(Graphics g) {
        // Border painted in paintComponent
    }

    /**
     * Overrides the paint method to handle drawing the placeholder text if the
     * text field is currently empty.
     *
     * @param g The {@code Graphics} context used for painting.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(150, 150, 150));
            g2.setFont(getFont());
            /**
             * Draws the placeholder text inside the component at a fixed position (20, 28).
             */
            g2.drawString(placeholder, 20, 28);
        }
    }
}