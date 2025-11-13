package DatabaseConnector;

import MaterialSwingUI.MaterialScrollPane;
import Screens.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A specialized JFrame utility class designed for managing database persistence tasks,
 * including connection configuration, table creation, CSV import/export, and SQL dump import.
 * It serves as a pre-login setup tool to ensure the database environment is ready.
 */
public class DatabaseChef extends JFrame {

    private static final String[] TABLES = {"clubs", "club_membership", "announcements", "discussion_comments", "discussion_forum", "resources", "rsvps", "system_users", "roles", "events", "budgets"};

    private final JTextField hostField = createDarkField("");
    private final JTextField portField = createDarkField("");
    private final JTextField dbField = createDarkField("");
    private final JTextField userField = createDarkField("");
    private final JPasswordField passField = new JPasswordField();
    private final JTextArea logArea = createDarkTextArea();
    private final DefaultTableModel previewModel = new DefaultTableModel();
    // Config file path
    private static final String CONFIG_FILE = "config.properties";
    private final Properties config = new Properties();


    // threading
    private final ExecutorService bgExecutor = Executors.newFixedThreadPool(2);

    /**
     * Constructs the {@code DatabaseChef} frame.
     * Initializes the user interface, loads configuration, sets up action listeners,
     * and starts an initial background task for auto-importing CSV files.
     */
    public DatabaseChef() {
        super("Data Persistence Manager (MySQL + CSV + SQL)");
        setVisible(false);
        new DatabaseConnector(this);
        loadConfig();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(35, 35, 35));

        /*
           TOP DATABASE CONFIG PANEL
         */
        JPanel top = new JPanel(new GridLayout(2, 6, 10, 10));
        top.setBackground(new Color(45, 45, 45));
        top.setBorder(BorderFactory.createTitledBorder("Database Connection"));

        styleDark(passField);

        addFieldRow(top, "Host:", hostField);
        addFieldRow(top, "Port:", portField);
        addFieldRow(top, "Database:", dbField);
        addFieldRow(top, "User:", userField);
        addFieldRow(top, "Password:", passField);

        add(top, BorderLayout.NORTH);


        /*
           CENTER (BUTTONS + TABLE PREVIEW)
        */
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.setOpaque(false);

        JButton createBtn     = createButton("Create DB & Tables");
        JButton importCsvBtn  = createButton("Import CSV Files");
        JButton importSqlBtn  = createButton("Import SQL Dump");
        JButton exportCsvBtn  = createButton("Export DB → CSV");

        btns.add(createBtn);
        btns.add(importCsvBtn);
        btns.add(importSqlBtn);
        btns.add(exportCsvBtn);
        center.add(btns, BorderLayout.NORTH);

        JTable previewTable = new JTable(previewModel);
        makeTableDark(previewTable);

        center.add(new MaterialScrollPane(previewTable), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);


        /*
           LOG AREA (BOTTOM)
*/
        JScrollPane scrollLog = new MaterialScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log Output"));
        add(scrollLog, BorderLayout.SOUTH);


        /*
           SIDE PANEL (Preview buttons)
      */
        JPanel side = new JPanel(new GridLayout(0, 1, 6, 6));
        side.setBackground(new Color(45, 45, 45));
        side.setBorder(BorderFactory.createTitledBorder("Preview Tables"));

        for (String t : TABLES) {
            JButton b = createButton(" " + t);
            b.addActionListener(e -> previewTable(t));
            side.add(b);
        }

        add(side, BorderLayout.EAST);


        /*
           ACTION LISTENERS
     */
        createBtn.addActionListener(e -> bgExecutor.submit(this::testAndLaunchLogin));
        importCsvBtn.addActionListener(e -> bgExecutor.submit(this::importCsvIfPresent));
        importSqlBtn.addActionListener(e -> chooseSqlImport());
        exportCsvBtn.addActionListener(e -> bgExecutor.submit(this::exportAllTablesToCsv));


        /*
           ON WINDOW CLOSE: Export CSV in background
 */
        /**
         * Sets up a {@code WindowListener} to save the configuration and attempt to export
         * all database tables to CSV files upon closing the window, limiting the background task
         * to 30 seconds to ensure timely shutdown.
         */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfig();
                log("Exiting... exporting DB to CSV first.");
                Future<?> task = bgExecutor.submit(DatabaseChef.this::exportAllTablesToCsv);
                try { task.get(30, TimeUnit.SECONDS); } catch (Exception ignored) {}
                bgExecutor.shutdown();
            }
        });


        /*
           AUTO-CSV IMPORT AT STARTUP */
        /**
         * Submits an asynchronous task to check for and import existing CSV files on startup.
         */
        bgExecutor.submit(() -> {
            log("Auto-import: searching for CSV files...");
            importCsvIfPresent();
        });
    }

    /* DARK UI HELPERS  */

    /**
     * Creates a new {@code JTextField} and applies the dark theme styling.
     *
     * @param text The initial text for the field.
     * @return A styled {@code JTextField}.
     */
    private static JTextField createDarkField(String text) {
        JTextField f = new JTextField(text);
        styleDark(f);
        return f;
    }

    /**
     * Creates a new {@code JTextArea} for logging and applies the dark theme styling.
     *
     * @return A styled {@code JTextArea}.
     */
    private static JTextArea createDarkTextArea() {
        JTextArea a = new JTextArea();
        styleDark(a);
        a.setFont(new Font("Consolas", Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    /**
     * Applies a standard dark theme style (colors, borders, cursor) to a Swing component.
     *
     * @param comp The {@code JComponent} to style.
     */
    private static void styleDark(JComponent comp) {
        comp.setForeground(Color.WHITE);
        comp.setBackground(new Color(55, 55, 55));
        comp.setBorder(new EmptyBorder(6, 8, 6, 8));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Adds a label and its corresponding input field (JComponent) to a parent panel,
     * typically used for the connection configuration section.
     *
     * @param parent The {@code JPanel} (usually using {@code GridLayout}) to add components to.
     * @param label The text for the label.
     * @param field The input {@code JComponent} (e.g., {@code JTextField} or {@code JPasswordField}).
     */
    private void addFieldRow(JPanel parent, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        parent.add(lbl);
        parent.add(field);
    }

    /**
     * Creates a styled button with dark theme colors and hover effects.
     *
     * @param text The text to display on the button.
     * @return A styled {@code JButton}.
     */
    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(70, 70, 70));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(95, 95, 95));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(70, 70, 70));
            }
        });

        return b;
    }

    /**
     * Applies the dark theme styling to a {@code JTable}, including header and cell renderer settings.
     *
     * @param table The {@code JTable} to style.
     */
    private void makeTableDark(JTable table) {
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(55, 55, 55));
        table.getTableHeader().setBackground(new Color(35, 35, 35));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(80, 80, 80));
        table.setFillsViewportHeight(true);
        table.setOpaque(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, center);
    }


    /* LOGGING*/

    /**
     * Appends a message to the log area on the Swing Event Dispatch Thread (EDT)
     * and automatically scrolls to the bottom.
     *
     * @param message The log message to display.
     */
    void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("• " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }


    /* DATABASE HELPERS */

    /**
     * Attempts to establish a JDBC {@code Connection} to the MySQL server without
     * specifying a default database. This is used primarily for creating the database.
     *
     * @return A valid {@code Connection} to the MySQL server.
     * @throws SQLException if the connection fails due to invalid credentials or host/port.
     */
    private Connection connectNoDB() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/?serverTimezone=UTC",
                hostField.getText().trim(), portField.getText().trim());

        Properties props = new Properties();
        props.setProperty("user", userField.getText().trim());
        props.setProperty("password", new String(passField.getPassword()));

        return DriverManager.getConnection(url, props);
    }

    /**
     * Attempts to establish a JDBC {@code Connection} to the specified database.
     *
     * @return A valid {@code Connection} to the configured database.
     * @throws SQLException if the connection fails (e.g., database doesn't exist, invalid credentials).
     */
    private Connection connectWithDB() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC",
                hostField.getText().trim(), portField.getText().trim(), dbField.getText().trim());

        Properties props = new Properties();
        props.setProperty("user", userField.getText().trim());
        props.setProperty("password", new String(passField.getPassword()));

        return DriverManager.getConnection(url, props);
    }


    /* DB + TABLE CREATION */

    /**
     * Executes SQL commands to ensure the configured database exists and that all required
     * application tables are created if they do not already exist.
     */
    private void createDatabaseAndTables() {
        try (Connection conn = connectNoDB(); Statement st = conn.createStatement()) {

            String dbName = dbField.getText().trim();
            log("Ensuring database exists: " + dbName);

            // Step 1: Create the database if it doesn't exist
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4");

            try (Connection dbConn = connectWithDB(); Statement s2 = dbConn.createStatement()) {
                log("Connected. Ensuring tables exist...");
                // Step 2: Create all required application tables with IF NOT EXISTS
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `clubs` (
                           `Club_ID` bigint(20) UNSIGNED NOT NULL,
                           `Name` varchar(200) NOT NULL,
                           `Status` varchar(50) DEFAULT 'Active',
                           `Category` varchar(100) DEFAULT NULL,
                           `Description` text DEFAULT NULL,
                           `Budget_Proposal` decimal(12,2) DEFAULT NULL,
                           `Member_Capacity` int(11) DEFAULT NULL,
                           `Approved_Budget` decimal(12,2) DEFAULT NULL,
                           `Approved_By` int(11) DEFAULT NULL,
                           `Logo` longblob DEFAULT NULL,
                           `Logo_Type` varchar(50) DEFAULT NULL,
                           `Logo_Size` int(11) DEFAULT NULL,
                           `Created_Date` timestamp NULL DEFAULT current_timestamp(),
                           `Created_By` int(11) DEFAULT NULL
                         )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `club_membership` (
                          `Membership_ID` bigint(20) UNSIGNED NOT NULL,
                          `User_ID` int(11) NOT NULL,
                          `Club_ID` int(11) NOT NULL,
                          `Membership_Status` varchar(50) DEFAULT 'Pending',
                          `Membership_Role` varchar(100) DEFAULT 'Member',
                          `Application_Date` timestamp NULL DEFAULT current_timestamp(),
                          `Approved_Date` timestamp NULL DEFAULT NULL,
                          `Approved_By` int(11) DEFAULT NULL,
                          `Left_Date` timestamp NULL DEFAULT NULL,
                          `Rejection_Reason` text DEFAULT NULL
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `events` (
                          `Event_ID` bigint(20) UNSIGNED NOT NULL,
                          `Title` varchar(255) NOT NULL,
                          `Type` varchar(100) DEFAULT NULL,
                          `Description` text DEFAULT NULL,
                          `Date` date NOT NULL,
                          `Status` varchar(50) DEFAULT 'Scheduled',
                          `Start_Time` time DEFAULT NULL,
                          `End_Time` time DEFAULT NULL,
                          `Resource_ID` int(11) DEFAULT NULL,
                          `Is_Budget_Requested` tinyint(1) DEFAULT 0,
                          `Budget_Amount` decimal(12,2) DEFAULT NULL,
                          `Budget_Status` varchar(50) DEFAULT NULL,
                          `Approved_By` int(11) DEFAULT NULL,
                          `Created_Date` timestamp NULL DEFAULT current_timestamp(),
                          `Created_By` int(11) DEFAULT NULL,
                          `Club_ID` int(11) DEFAULT NULL
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `announcements` (
                          `Announcement_ID` bigint(20) UNSIGNED NOT NULL,
                          `Club_ID` int(11) DEFAULT NULL,
                          `Created_By` int(11) DEFAULT NULL,
                          `Content` text NOT NULL,
                          `Title` varchar(255) NOT NULL,
                          `Target_Audience` varchar(100) DEFAULT NULL,
                          `Expiry_Date` date DEFAULT NULL,
                          `Created_Date` timestamp NULL DEFAULT current_timestamp()
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `discussion_comments` (
                          `Comment_ID` bigint(20) UNSIGNED NOT NULL,
                          `Message` text NOT NULL,
                          `User_ID` int(11) NOT NULL,
                          `Discussion_ID` int(11) NOT NULL,
                          `TimeStamp` timestamp NULL DEFAULT current_timestamp()
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `discussion_forum` (
                          `Discussion_ID` bigint(20) UNSIGNED NOT NULL,
                          `Title` varchar(255) NOT NULL,
                          `Message` text NOT NULL,
                          `TimeStamp` timestamp NULL DEFAULT current_timestamp(),
                          `Club_ID` int(11) DEFAULT NULL
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `resources` (
                          `Resource_ID` bigint(20) UNSIGNED NOT NULL,
                          `Name` varchar(200) NOT NULL,
                          `Type` varchar(100) DEFAULT NULL,
                          `Capacity` int(11) DEFAULT NULL,
                          `Is_Available` tinyint(1) DEFAULT 1,
                          `Location` varchar(255) DEFAULT NULL,
                          `Description` text DEFAULT NULL,
                          `Created_Date` timestamp NULL DEFAULT current_timestamp(),
                          `Updated_Date` timestamp NULL DEFAULT current_timestamp()
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `roles` (
                          `Role_ID` bigint(20) UNSIGNED NOT NULL,
                          `Role_Name` varchar(100) NOT NULL,
                          `Description` text DEFAULT NULL,
                          `Created_Date` timestamp NULL DEFAULT current_timestamp()
                        )
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `rsvps` (
                          `RSVP_ID` bigint(20) UNSIGNED NOT NULL,
                          `User_ID` int(11) NOT NULL,
                          `Event_ID` int(11) NOT NULL,
                          `Status` varchar(50) DEFAULT 'Pending',
                          `Date` timestamp NULL DEFAULT current_timestamp(),
                          `Attendance_Marked` tinyint(1) DEFAULT 0
                        )\s
                        """);
                s2.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `system_users` (
                          `User_ID` bigint(20) UNSIGNED NOT NULL,
                          `Full_Names` varchar(200) NOT NULL,
                          `Email` varchar(255) NOT NULL,
                          `Username` varchar(100) NOT NULL,
                          `Password_Hash` varchar(255) NOT NULL,
                          `Role_ID` int(11) DEFAULT 4,
                          `Profile_Picture` longblob DEFAULT NULL,
                          `Profile_Picture_Type` varchar(50) DEFAULT NULL,
                          `Profile_Picture_Size` int(11) DEFAULT NULL,
                          `Status` varchar(50) DEFAULT 'Active',
                          `Registration_Date` timestamp NULL DEFAULT current_timestamp()
                        )
                        """);

                log("Tables ensured (clubs, members, events, budgets)");
            }

        } catch (SQLException ex) {
            log(" Database creation error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    /* CSV IMPORT  */

    /**
     * Checks for the existence of CSV files matching the table names in the root directory
     * and attempts to import any found CSV file into the corresponding database table.
     */
    private void importCsvIfPresent() {
        try {
            for (String table : TABLES) {
                Path csv = Path.of(table + ".csv");
                if (Files.exists(csv)) {
                    log("Found CSV: " + csv + " → Importing...");
                    importTableFromCsv(table, csv);
                }
            }
        } catch (Exception e) {
            log("CSV import error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imports data from a single CSV file into the specified database table using JDBC batching.
     * Assumes the first line of the CSV is the header (column names).
     *
     * @param table The name of the database table.
     * @param csvPath The {@code Path} to the CSV file.
     */
    private void importTableFromCsv(String table, Path csvPath) {
        try (Connection conn = connectWithDB()) {
            conn.setAutoCommit(false);

            try (BufferedReader r = Files.newBufferedReader(csvPath)) {
                String header = r.readLine();
                if (header == null) return;

                String[] cols = header.split(",");
                String placeholders = String.join(",", Collections.nCopies(cols.length, "?"));

                String sql = "INSERT INTO " + table + "(" + String.join(",", cols) + ") VALUES(" + placeholders + ")";
                PreparedStatement ps = conn.prepareStatement(sql);

                String line;
                int count = 0;
                while ((line = r.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    for (int i = 0; i < cols.length; i++) {
                        ps.setString(i + 1, (i < parts.length) ? parts[i] : null);
                    }
                    ps.addBatch();
                    /**
                     * Executes the batch of prepared statements after every 500 rows to optimize insertion speed.
                     */
                    if (count % 500 == 0) ps.executeBatch();
                    count++;
                }

                ps.executeBatch();
                conn.commit();
                log("Imported " + count + " rows into " + table);
            }
        } catch (Exception ex) {
            log("CSV import failed (" + table + "): " + ex.getMessage());
        }
    }


    /* CSV EXPORT */

    /**
     * Exports the data from all configured database tables into separate CSV files.
     * Files are saved in a subdirectory named "exported_csv".
     */
    private void exportAllTablesToCsv() {
        try {
            Files.createDirectories(Path.of("exported_csv"));
        } catch (IOException ignored) {}

        try (Connection conn = connectWithDB()) {
            for (String t : TABLES) {
                exportTableToCsv(conn, t, Path.of("exported_csv", t + ".csv"));
            }
            log(" Export complete → exported_csv folder.");
        } catch (SQLException ex) {
            log(" Export error: " + ex.getMessage());
        }
    }

    /**
     * Exports data from a single database table to a specified CSV file.
     * Handles comma escaping for CSV integrity.
     *
     * @param conn The active database {@code Connection}.
     * @param table The name of the table to export.
     * @param outFile The {@code Path} where the CSV file should be written.
     */
    private void exportTableToCsv(Connection conn, String table, Path outFile) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            try (BufferedWriter w = Files.newBufferedWriter(outFile)) {

                // Write header row
                for (int i = 1; i <= cols; i++) {
                    w.write(md.getColumnName(i));
                    if (i < cols) w.write(",");
                }
                w.write("\n");

                // Write data rows
                int count = 0;
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        String val = rs.getString(i);
                        /**
                         * Logic to handle CSV escaping: enclose values containing commas in double quotes
                         * and escape existing double quotes by doubling them.
                         */
                        if (val != null && val.contains(",")) val = "\"" + val.replace("\"", "\"\"") + "\"";
                        w.write(val == null ? "" : val);
                        if (i < cols) w.write(",");
                    }
                    w.write("\n");
                    count++;
                }
                log(" Exported " + count + " rows from " + table);
            }

        } catch (Exception e) {
            log(" Failed to export " + table);
        }
    }


    /* SQL FILE IMPORT  */

    /**
     * Opens a {@code JFileChooser} to allow the user to select an SQL dump file
     * and then submits the import task to the background executor.
     */
    private void chooseSqlImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            bgExecutor.submit(() -> importSqlFile(file.toPath()));
        }
    }

    /**
     * Imports SQL commands from a selected file, executing them line by line or statement by statement.
     * Handles multiline statements by checking for the statement terminator (';').
     *
     * @param file The {@code Path} to the SQL dump file.
     */
    private void importSqlFile(Path file) {
        log("Importing SQL dump: " + file.getFileName());

        try (Connection conn = connectWithDB(); BufferedReader r = Files.newBufferedReader(file)) {
            StringBuilder batch = new StringBuilder();
            Statement st = conn.createStatement();

            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                // Skip empty lines or comments
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) continue;

                batch.append(line).append(" ");
                // Execute when a statement terminator is found
                if (line.endsWith(";")) {
                    try {
                        st.execute(batch.toString());
                    } catch (SQLException ex) {
                        log("SQL failed: " + ex.getMessage());
                    }
                    batch.setLength(0);
                }
            }

            log(" SQL import done.");

        } catch (Exception e) {
            log(" SQL import error: " + e.getMessage());
        }
    }


    /* TABLE PREVIEW */

    /**
     * Clears the current table preview and asynchronously loads up to 200 rows of data
     * from the specified table for display in the {@code JTable} component.
     *
     * @param table The name of the database table to preview.
     */
    private void previewTable(String table) {
        SwingUtilities.invokeLater(() -> {
            previewModel.setColumnCount(0);
            previewModel.setRowCount(0);
        });

        bgExecutor.submit(() -> {
            try (Connection conn = connectWithDB();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 200")) {

                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                Vector<String> colNames = new Vector<>();
                for (int i = 1; i <= cols; i++)
                    colNames.add(md.getColumnName(i));

                Vector<Vector<Object>> data = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= cols; i++)
                        row.add(rs.getObject(i));
                    data.add(row);
                }

                SwingUtilities.invokeLater(() -> previewModel.setDataVector(data, colNames));
                log(" Previewed table: " + table + " (" + data.size() + " rows)");

            } catch (SQLException ex) {
                log(" Preview failed: " + ex.getMessage());
            }
        });
    }
    /**
     * Loads database connection settings from the configuration file ({@value #CONFIG_FILE}).
     * If the file is not found, default settings are used. The loaded settings are then
     * used to populate the UI fields.
     */
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            config.load(fis);
            log("Loaded config from " + CONFIG_FILE);
        } catch (IOException ex) {
            log("Config not found — using defaults");
            config.setProperty("db.host", "localhost");
            config.setProperty("db.port", "32768");
            config.setProperty("db.name", "khalapaqokolo2333533");
            config.setProperty("db.user", "root");
            config.setProperty("db.password", "root");
        }

        // Update UI fields
        hostField.setText(config.getProperty("db.host"));
        portField.setText(config.getProperty("db.port"));
        dbField.setText(config.getProperty("db.name"));
        userField.setText(config.getProperty("db.user"));
        passField.setText(config.getProperty("db.password"));
    }

    /**
     * Saves the current database connection settings from the UI fields back to the
     * configuration file ({@value #CONFIG_FILE}).
     */
    private void saveConfig() {
        config.setProperty("db.host", hostField.getText());
        config.setProperty("db.port", portField.getText());
        config.setProperty("db.name", dbField.getText());
        config.setProperty("db.user", userField.getText());
        config.setProperty("db.password", new String(passField.getPassword()));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "Database Configuration (auto updated)");
            log("Saved configuration.");
        } catch (IOException e) {
            log("Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Attempts to connect to the configured database. On success, it saves the configuration
     * and launches the main application {@code LoginFrame}, closing the current setup window.
     * On failure, it logs the error.
     */
    private void testAndLaunchLogin() {
        log("Testing database connection...");

        try {
            Connection conn = connectWithDB();
            log(" DATABASE CONNECTED SUCCESSFULLY!");

            saveConfig();  // save config on success

            SwingUtilities.invokeLater(() -> {
                dispose(); // close DatabaseChef window
                new LoginFrame(conn).setVisible(true); // launch login
            });

        } catch (SQLException e) {
            log(" Database connection failed: " + e.getMessage());
        }
    }

}