package MaterialSwingUI;

import javax.swing.*;
import java.awt.*;
/**
 * A custom {@code JPanel} component that renders a circular progress chart,
 * displaying a percentage value as a colored arc. It uses a material-design
 * inspired dark theme.
 */
public class MaterialCircularProgressChart extends JPanel {
    private final String title;
    private int percentage;
    private final Color progressColor;
    private final Color bgColor = new Color(60, 60, 60);

    /**
     * Constructs a {@code MaterialCircularProgressChart}.
     *
     * @param title The title displayed above the chart.
     * @param percentage The initial progress value (0-100).
     * @param progressColor The {@code Color} used to render the progress arc.
     */
    public MaterialCircularProgressChart(String title, int percentage, Color progressColor) {
        this.title = title;
        this.percentage = percentage;
        this.progressColor = progressColor;
        setPreferredSize(new Dimension(220, 200));
        Color cardBg = new Color(50, 50, 50);
        setBackground(cardBg);
        setOpaque(true);
    }

    /**
     * Custom painting method to draw the title, the background circle, the progress arc,
     * and the percentage text in the center.
     *
     * @param g The {@code Graphics} context used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, 25);

        // Circle params
        int diameter = 110;
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2 + 10;
        int strokeWidth = 12;

        // Background circle
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(bgColor);
        /**
         * Draws the full 360-degree background circle of the progress indicator.
         */
        g2.drawArc(x, y, diameter, diameter, 0, 360);

        // Progress arc
        g2.setColor(progressColor);
        /**
         * Calculates the sweep angle based on the current percentage value.
         */
        int angle = (int) (360 * (percentage / 100.0));
        /**
         * Draws the progress arc starting at the top (90 degrees) and sweeping counter-clockwise (-angle).
         */
        g2.drawArc(x, y, diameter, diameter, 90, -angle);

        // Center percentage text
        String percentText = percentage + "%";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(percentText);
        int textX = width / 2 - textWidth / 2;
        int textY = height / 2 + fm.getAscent() / 2;
        g2.setColor(new Color(180, 180, 180));
        g2.drawString(percentText, textX, textY);
    }

    /**
     * Updates the percentage value displayed in the chart and triggers a repaint
     * to reflect the change visually.
     *
     * @param percentage The new progress value (0-100).
     */
    public void setPercentage(int percentage) {
        this.percentage = percentage;
        repaint();
    }
}