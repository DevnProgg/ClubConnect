package Components;

import javax.swing.*;
import java.awt.*;

public // Large Chart Section Component
/**
 * A specialized {@code JPanel} designed to serve as a container for a large,
 * dedicated chart or visualization component within a dashboard or report view.
 * It provides basic styling (dark background, padding) and a placeholder
 * for the actual chart implementation.
 */
class LargeChartSection extends JPanel {

    /**
     * Constructs a {@code LargeChartSection} panel.
     * Initializes the panel with a dark background, sets a preferred size,
     * and establishes a {@code BorderLayout} with padding.
     */
    public LargeChartSection() {
        Color cardBg = new Color(50, 50, 50);
        setBackground(cardBg);
        setPreferredSize(new Dimension(750, 250));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Placeholder for chart content
        JLabel placeholder = new JLabel("Chart Area", SwingConstants.CENTER);
        placeholder.setForeground(new Color(100, 100, 100));
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(placeholder, BorderLayout.CENTER);
    }
}