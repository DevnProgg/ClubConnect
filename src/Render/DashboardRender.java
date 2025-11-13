package Render;

import MaterialSwingUI.MaterialBarChart;
import MaterialSwingUI.MaterialCircularProgressChart;
import Components.LargeChartSection;
import MaterialSwingUI.UIUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Renders the main dashboard panel, organizing key metrics and charts into a single view.
 * It features three circular progress charts at the top and a larger bar chart section below.
 */
public class DashboardRender extends JPanel {
    private final MaterialCircularProgressChart clubsChart;
    private final MaterialCircularProgressChart usersChart;
    private final MaterialCircularProgressChart discussionsChart;
    private final LargeChartSection largeChart = new LargeChartSection();

    /**
     * Constructs the {@code DashboardRender} panel.
     * Initializes the panel layout using {@code GridBagLayout} and places the three circular
     * charts and the large chart section.
     */
    public DashboardRender() {
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Create circular progress charts
        clubsChart = new MaterialCircularProgressChart("Clubs", 10, new Color(76, 175, 80));
        usersChart = new MaterialCircularProgressChart("Users", 60, new Color(233, 30, 99));
        discussionsChart = new MaterialCircularProgressChart("Discussions", 40, new Color(33, 150, 243));

        // Position circular charts in top row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.33;
        gbc.weighty = 0.4;
        add(clubsChart, gbc);

        gbc.gridx = 1;
        add(usersChart, gbc);

        gbc.gridx = 2;
        add(discussionsChart, gbc);

        // Add large chart section
        populateCart();
        largeChart.setBorder(createRoundedBorder());

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        add(largeChart, gbc);
    }

    /**
     * Creates a custom rounded border using a utility class.
     *
     * @return A {@code Border} instance with rounded corners.
     */
    private Border createRoundedBorder() {
        return new UIUtilities.RoundedBorder(20);
    }

    /**
     * Updates the percentage value of the "Clubs" circular progress chart.
     *
     * @param percentage The new progress value (0-100).
     */
    public void updateClubsPercentage(int percentage) {
        clubsChart.setPercentage(percentage);
    }

    /**
     * Updates the percentage value of the "Users" circular progress chart.
     *
     * @param percentage The new progress value (0-100).
     */
    public void updateUsersPercentage(int percentage) {
        usersChart.setPercentage(percentage);
    }

    /**
     * Updates the percentage value of the "Discussions" circular progress chart.
     *
     * @param percentage The new progress value (0-100).
     */
    public void updateDiscussionsPercentage(int percentage) {
        discussionsChart.setPercentage(percentage);
    }

    /**
     * Populates the {@code largeChart} section with a {@code MaterialBarChart}
     * showing event trends, and sets a titled border.
     */
    private void populateCart(){
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] eventsPerMonth = {3,4,5,6,2,6,7,3,6,7,3,6};
        MaterialBarChart eventTrends = new MaterialBarChart("Event Trends", months, eventsPerMonth);

        largeChart.setBorder(BorderFactory.createTitledBorder("Event Trends"));
        largeChart.add(eventTrends);
    }
}