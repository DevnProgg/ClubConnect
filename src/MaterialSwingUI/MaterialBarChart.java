package MaterialSwingUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

/**
 * A custom {@code JPanel} component that integrates a JFreeChart bar chart
 * with a material-design inspired dark theme. It is designed to display
 * categorical data (labels) against numerical values.
 */
public class MaterialBarChart extends JPanel {

    private final String[] labels;
    private final int[] values;

    private final Color backgroundDark = new Color(25, 25, 25);
    private final Color gridLine = new Color(80, 80, 80);
    private final Color axisText = new Color(230, 230, 230);
    private final Color barColor = new Color(52, 152, 219);

    /**
     * Constructs a {@code MaterialBarChart} panel.
     *
     * @param title The main title of the chart, used for the chart title and the range axis label.
     * @param labels An array of {@code String} labels for the categories (X-axis).
     * @param values An array of {@code int} values corresponding to the labels (Y-axis bar height).
     */
    public MaterialBarChart(String title, String[] labels, int[] values) {
        this.labels = labels;
        this.values = values;

        setLayout(new BorderLayout());
        setBackground(backgroundDark);

        add(createChartPanel(title), BorderLayout.CENTER);
    }

    /**
     * Creates and configures the JFreeChart {@code ChartPanel} containing the bar chart.
     * This method handles data population, chart creation, and extensive styling for a dark theme.
     *
     * @param title The title of the chart.
     * @return A fully configured {@code ChartPanel}.
     */
    private ChartPanel createChartPanel(String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        /**
         * Populates the dataset using the provided labels and values.
         * The series key is derived from the chart title.
         */
        for (int i = 0; i < labels.length; i++) {
            dataset.addValue(values[i], title.split(" ")[0] + " / Month", labels[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Month", title.split(" ")[0], dataset
        );

        chart.setAntiAlias(true);
        chart.setBackgroundPaint(backgroundDark);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        // Title styling
        chart.getTitle().setPaint(axisText);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Chart plot styling
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(35, 35, 35));
        plot.setRangeGridlinePaint(gridLine);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.getRangeAxis().setLowerBound(0);

        // Axes styling
        plot.getRangeAxis().setTickLabelPaint(axisText);
        plot.getRangeAxis().setLabelPaint(axisText);
        plot.getDomainAxis().setTickLabelPaint(axisText);
        plot.getDomainAxis().setLabelPaint(axisText);

        // Renderer (bar appearance)
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Bar color
        renderer.setSeriesPaint(0, barColor);

        // Optional: Rounded look
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.10);

        // ChartPanel setup
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(backgroundDark);
        chartPanel.setMouseWheelEnabled(false);

        return chartPanel;
    }
}