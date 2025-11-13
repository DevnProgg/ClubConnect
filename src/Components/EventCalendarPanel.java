package Components;

import Models.Event;
import Models.RSVP;
import Models.User;
import MaterialSwingUI.MaterialButton;
import MaterialSwingUI.MaterialScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
public class EventCalendarPanel extends JPanel {

    private final Calendar calendar = Calendar.getInstance();
    private final JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
    private final JLabel monthLabel = new JLabel("Month", SwingConstants.CENTER);

    private final List<Event> events;
    private final Connection conn;
    private final User user;
    private final List<RSVP> rsvps = new ArrayList<>();

    /**
     * Constructs the {@code EventCalendarPanel}.
     * This panel displays a calendar view, highlights days with scheduled events,
     * and provides navigation controls to change months.
     *
     * @param events The {@code List} of all {@link Models.Event}s to be displayed and checked against the calendar dates.
     * @param conn The active database {@link java.sql.Connection} used for components within the event popups (e.g., RSVP handling).
     * @param user The currently logged-in {@link Models.User}, passed to event detail components for context.
     */
    public EventCalendarPanel(List<Event> events, Connection conn, User user) {
        this.events = events;
        this.conn = conn;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(new Color(28, 28, 28));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(Color.WHITE);

        JButton prevBtn = createNavButton("←");
        JButton nextBtn = createNavButton("→");

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 28, 28));
        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));

        calendarGrid.setBackground(new Color(40, 40, 40));

        add(header, BorderLayout.NORTH);
        add(calendarGrid, BorderLayout.CENTER);

        renderCalendar();
    }

    /**
     * Clears and re-populates the calendar grid for the month currently set in the internal {@code calendar} instance.
     * This method handles displaying day-of-week headers, empty cells for padding, and date cells.
     */
    private void renderCalendar() {
        calendarGrid.removeAll();

        String[] dow = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dow) {
            calendarGrid.add(createHeaderCell(day));
        }

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDay = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(calendar.getTime()));

        /**
         * Adds empty cells for days preceding the first day of the month to align the dates
         * correctly under the day-of-week headers.
         */
        for (int i = 1; i < firstDay; i++) {
            calendarGrid.add(createEmptyCell());
        }

        /**
         * Adds a date cell for every day in the current month.
         */
        for (int day = 1; day <= daysInMonth; day++) {
            calendarGrid.add(createDateCell(day));
        }

        revalidate();
        repaint();
    }

    /**
     * Creates a {@code JLabel} for displaying the day of the week header (e.g., "Mon").
     *
     * @param text The text for the header cell.
     * @return A styled {@code JLabel} for the calendar header.
     */
    private JLabel createHeaderCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(new Color(200, 200, 200));
        return label;
    }

    /**
     * Creates an empty, transparent {@code JPanel} used for padding cells at the start of the month.
     *
     * @return A transparent {@code JPanel}.
     */
    private JPanel createEmptyCell() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a calendar grid cell for a specific day of the month.
     * If one or more events are scheduled for that day, an indicator is added, and a
     * {@code MouseListener} is attached to show the event popup.
     *
     * @param day The day number (1 to 31).
     * @return A styled {@code JPanel} representing the date cell.
     */
    private JPanel createDateCell(int day) {
        JPanel cell = new JPanel();
        cell.setBackground(new Color(60, 63, 65));
        cell.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        cell.setLayout(new BorderLayout());

        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setForeground(Color.WHITE);
        dayLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
        cell.add(dayLabel, BorderLayout.NORTH);

        // Look for events on this date
        List<Event> todaysEvents = getEventsForDay(day);
        if (!todaysEvents.isEmpty()) {
            JLabel indicator = new JLabel("● Event", SwingConstants.CENTER);
            indicator.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            indicator.setForeground(new Color(0, 180, 255));
            cell.add(indicator, BorderLayout.CENTER);

            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showEventPopup(todaysEvents);
                }
            });
        }

        return cell;
    }

    /**
     * Filters the total list of events to find those scheduled for a specific day in the currently viewed month and year.
     *
     * @param day The day of the month to check (1-31).
     * @return A {@code List} of {@link Models.Event}s occurring on that date.
     */
    private List<Event> getEventsForDay(int day) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            Calendar c = Calendar.getInstance();
            c.setTime(e.date());
            if (c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    c.get(Calendar.DAY_OF_MONTH) == day) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Creates a styled button for navigating between calendar months (Previous/Next).
     *
     * @param text The text or symbol (e.g., "←") for the button.
     * @return A styled {@code JButton}.
     */
    private JButton createNavButton(String text) {
        JButton button = new MaterialButton(text, new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusable(false);
        return button;
    }

    /**
     * Displays a modal dialog containing the details and RSVP options for all events
     * associated with the selected calendar day.
     *
     * @param eventList The {@code List} of {@link Models.Event}s to display in the popup.
     */
    private void showEventPopup(List<Event> eventList) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30,30,30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;


        for (Event e : eventList) {
            JPanel item = renderEventItem(e);
            panel.add(item, gbc);
        }


        JScrollPane scrollPane = new MaterialScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(900, 600));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Event Details",
                JOptionPane.PLAIN_MESSAGE
        );
    }


    /**
     * Creates a container panel that holds an {@code EventCard} component for a single event.
     *
     * @param event The {@link Models.Event} to be rendered.
     * @return A {@code JPanel} containing the event card structure.
     */
    private JPanel renderEventItem(Event event) {
        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(new Color(45, 45, 45));
        placeholder.setBorder(new EmptyBorder(10, 10, 10, 10));

        EventCard card = new EventCard(event, conn, user);
        card.setBackground(new Color(45, 45, 45));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        placeholder.add(card, BorderLayout.CENTER);

        return placeholder;
    }

    /**
     * Updates the internal calendar instance to the previous or next month,
     * based on the amount, and then calls {@code renderCalendar()} to update the UI.
     *
     * @param amount The number of months to change by (e.g., -1 for previous, 1 for next).
     */
    private void changeMonth(int amount) {
        calendar.add(Calendar.MONTH, amount);
        renderCalendar();
    }
}