package Components;

import javax.swing.*;
import java.awt.*;
// Logo panel
/**
 * A custom {@code JPanel} component responsible for drawing a stylized logo for the application.
 * The logo is rendered directly using Java 2D graphics, featuring concentric shapes
 * and application text.
 */
public class LogoPanel extends JPanel {
    /**
     * Overrides the {@code paintComponent} method to draw the custom logo.
     * The drawing logic includes rendering the outer circle, inner shape (simplified),
     * and the "CLUB CONNECT" text.
     *
     * @param g The {@code Graphics} context used for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw outer circle
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillOval(10, 10, 160, 160);
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(10, 10, 160, 160);

        // Draw inner shapes (simplified representation)
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(60, 50, 60, 60);

        // Draw "CLUB CONNECT" text
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        String text1 = "CLUB";
        String text2 = "CONNECT";
        int x1 = 90 - fm.stringWidth(text1) / 2;
        int x2 = 90 - fm.stringWidth(text2) / 2;
        g2d.drawString(text1, x1, 130);
        g2d.drawString(text2, x2, 145);
    }
}