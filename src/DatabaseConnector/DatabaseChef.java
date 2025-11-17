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

/**
 * A specialized JFrame utility class designed for managing database persistence tasks,
 * including connection configuration, table creation, CSV import/export, and SQL dump import.
 * It serves as a pre-login setup tool to ensure the database environment is ready.
 */
public class DatabaseChef extends JFrame {

    private static final String[] TABLES = {"clubs", "club_membership", "announcements", "discussion_comments", "discussion_forum", "resources", "rsvps", "system_users", "roles", "events"};

    private final JTextField hostField = createDarkField("");
    private final JTextField portField = createDarkField("");
    private final JTextField dbField = createDarkField("");
    private final JTextField userField = createDarkField("");
    private final JPasswordField passField = new JPasswordField();
    private final JTextArea logArea = createDarkTextArea();
    private final DefaultTableModel previewModel = new DefaultTableModel();
    private static final String CONFIG_FILE = "config.properties";
    private final Properties config = new Properties();

    private final ExecutorService bgExecutor = Executors.newFixedThreadPool(2);

    public DatabaseChef() {
        super("Data Persistence Manager (MySQL + CSV + SQL)");
        setVisible(false);
       // new DatabaseConnector(this);
        loadConfig();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(35, 35, 35));

        // TOP DATABASE CONFIG PANEL
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

        // CENTER (BUTTONS + TABLE PREVIEW)
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.setOpaque(false);

        JButton createBtn = createButton("Create DB & Tables");
        JButton importCsvBtn = createButton("Import CSV Files");
        JButton importSqlBtn = createButton("Import SQL Dump");
        JButton exportCsvBtn = createButton("Export DB → CSV");

        btns.add(createBtn);
        btns.add(importCsvBtn);
        btns.add(importSqlBtn);
        btns.add(exportCsvBtn);
        center.add(btns, BorderLayout.NORTH);

        JTable previewTable = new JTable(previewModel);
        makeTableDark(previewTable);

        center.add(new MaterialScrollPane(previewTable), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // LOG AREA (BOTTOM)
        JScrollPane scrollLog = new MaterialScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log Output"));
        add(scrollLog, BorderLayout.SOUTH);

        // SIDE PANEL (Preview buttons)
        JPanel side = new JPanel(new GridLayout(0, 1, 6, 6));
        side.setBackground(new Color(45, 45, 45));
        side.setBorder(BorderFactory.createTitledBorder("Preview Tables"));

        for (String t : TABLES) {
            JButton b = createButton(" " + t);
            b.addActionListener(e -> previewTable(t));
            side.add(b);
        }

        add(side, BorderLayout.EAST);

        // ACTION LISTENERS
        createBtn.addActionListener(e -> bgExecutor.submit(this::autoCheckAndConnect));
        importCsvBtn.addActionListener(e -> bgExecutor.submit(this::importCsvIfPresent));
        importSqlBtn.addActionListener(e -> chooseSqlImport());
        exportCsvBtn.addActionListener(e -> bgExecutor.submit(this::exportAllTablesToCsv));

        // ON WINDOW CLOSE: Export CSV in background
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfig();
                log("Exiting... exporting DB to CSV first.");
                Future<?> task = bgExecutor.submit(DatabaseChef.this::exportAllTablesToCsv);
                try { task.get(30, TimeUnit.SECONDS); } catch (Exception ignored) {}
                bgExecutor.shutdown();
            }
        });

        // AUTO-CHECK AND CONNECT AT STARTUP
        bgExecutor.submit(() -> {
            log("Auto-checking database and tables...");
            autoCheckAndConnect();
        });
    }

    /* DARK UI HELPERS */

    private static JTextField createDarkField(String text) {
        JTextField f = new JTextField(text);
        styleDark(f);
        return f;
    }

    private static JTextArea createDarkTextArea() {
        JTextArea a = new JTextArea();
        styleDark(a);
        a.setFont(new Font("Consolas", Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private static void styleDark(JComponent comp) {
        comp.setForeground(Color.WHITE);
        comp.setBackground(new Color(55, 55, 55));
        comp.setBorder(new EmptyBorder(6, 8, 6, 8));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void addFieldRow(JPanel parent, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        parent.add(lbl);
        parent.add(field);
    }

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

    /* LOGGING */

    void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("• " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /* DATABASE HELPERS */

    private Connection connectNoDB() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/?serverTimezone=UTC",
                hostField.getText().trim(), portField.getText().trim());

        Properties props = new Properties();
        props.setProperty("user", userField.getText().trim());
        props.setProperty("password", new String(passField.getPassword()));

        return DriverManager.getConnection(url, props);
    }

    private Connection connectWithDB() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC",
                hostField.getText().trim(), portField.getText().trim(), dbField.getText().trim());

        Properties props = new Properties();
        props.setProperty("user", userField.getText().trim());
        props.setProperty("password", new String(passField.getPassword()));

        return DriverManager.getConnection(url, props);
    }

    /* AUTO-CHECK DATABASE AND TABLES */

    /**
     * Automatically checks if the database exists and if all required tables exist.
     * If they don't exist, creates them. Then attempts to connect to the database.
     */
    private void autoCheckAndConnect() {
        try {
            log("Checking MySQL server connection...");

            // Step 1: Connect to MySQL server (without database)
            try (Connection conn = connectNoDB()) {
                log("✓ MySQL server connected successfully");

                // Step 2: Check if database exists
                String dbName = dbField.getText().trim();
                boolean dbExists = databaseExists(conn, dbName);

                if (!dbExists) {
                    log("⚠ Database '" + dbName + "' does not exist. Creating...");
                    createDatabaseAndTables();
                } else {
                    log("✓ Database '" + dbName + "' exists");

                    // Step 3: Check if all tables exist
                    if (!allTablesExist()) {
                        log("⚠ Some tables are missing. Creating tables...");
                        createMissingTables();
                    } else {
                        log("✓ All required tables exist");
                    }
                }

                // Step 4: Auto-import CSV files if present
                importCsvIfPresent();

                // Step 5: Test final connection and launch login
                testAndLaunchLogin();

            }

        } catch (SQLException e) {
            log("✗ MySQL server connection failed: " + e.getMessage());
            log("Please verify your credentials and try again.");
        }
    }

    /**
     * Checks if a database exists on the MySQL server.
     */
    private boolean databaseExists(Connection conn, String dbName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                if (rs.getString(1).equalsIgnoreCase(dbName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if all required tables exist in the database.
     */
    private boolean allTablesExist() {
        try (Connection conn = connectWithDB()) {
            DatabaseMetaData meta = conn.getMetaData();

            for (String table : TABLES) {
                try (ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        log("  Missing table: " + table);
                        return false;
                    }
                }
            }
            return true;

        } catch (SQLException e) {
            log("Error checking tables: " + e.getMessage());
            return false;
        }
    }

    /* DB + TABLE CREATION */

    private void createDatabaseAndTables() {
        try (Connection conn = connectNoDB(); Statement st = conn.createStatement()) {

            String dbName = dbField.getText().trim();
            log("Creating database: " + dbName);

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4");
            log("✓ Database created");

            createMissingTables();

        } catch (SQLException ex) {
            log("✗ Database creation error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Creates all missing tables in the database.
     */
    private void createMissingTables() {
        try (Connection dbConn = connectWithDB(); Statement s2 = dbConn.createStatement()) {
            log("Creating tables...");

            s2.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `clubs` (
                       `Club_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
                      `Membership_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
                      `Event_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
                      `Announcement_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
                      `Comment_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
                      `Message` text NOT NULL,
                      `User_ID` int(11) NOT NULL,
                      `Discussion_ID` int(11) NOT NULL,
                      `TimeStamp` timestamp NULL DEFAULT current_timestamp()
                    )
                    """);
            s2.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `discussion_forum` (
                      `Discussion_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
                      `Title` varchar(255) NOT NULL,
                      `Message` text NOT NULL,
                      `TimeStamp` timestamp NULL DEFAULT current_timestamp(),
                      `Club_ID` int(11) DEFAULT NULL
                    )
                    """);
            s2.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `resources` (
                      `Resource_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
                      `Role_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
                      `Role_Name` varchar(100) NOT NULL,
                      `Description` text DEFAULT NULL,
                      `Created_Date` timestamp NULL DEFAULT current_timestamp()
                    )
                    """);
            s2.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `rsvps` (
                      `RSVP_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
                      `User_ID` int(11) NOT NULL,
                      `Event_ID` int(11) NOT NULL,
                      `Status` varchar(50) DEFAULT 'Pending',
                      `Date` timestamp NULL DEFAULT current_timestamp(),
                      `Attendance_Marked` tinyint(1) DEFAULT 0
                    ) 
                    """);
            s2.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `system_users` (
                      `User_ID` bigint(20) UNSIGNED NOT NULL PRIMARY KEY,
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
            s2.executeUpdate("""
                    INSERT IGNORE INTO `resources` (`Resource_ID`, `Name`, `Type`, `Capacity`, `Is_Available`, `Location`, `Description`, `Created_Date`, `Updated_Date`) VALUES
                    (1, 'Main Auditorium', 'Venue', 300, 1, 'Building A, Floor 1', NULL, '2025-11-06 08:02:59', '2025-11-06 08:02:59'),
                    (2, 'Conference Room A', 'Meeting Room', 30, 1, 'Building B, Floor 2', NULL, '2025-11-06 08:02:59', '2025-11-06 08:02:59'),
                    (3, 'Photography Studio', 'Studio', 15, 1, 'Arts Building, Floor 3', NULL, '2025-11-06 08:02:59', '2025-11-06 08:02:59'),
                    (4, 'Computer Lab 1', 'Lab', 50, 1, 'Tech Building, Floor 1', NULL, '2025-11-06 08:02:59', '2025-11-06 08:02:59');
                    """);
            s2.executeUpdate("""
                    INSERT IGNORE INTO `roles` (`Role_ID`, `Role_Name`, `Description`, `Created_Date`) VALUES
                    (1, 'admin', 'Full system access and management', '2025-11-06 08:02:59'),
                    (4, 'guest', 'General student user', '2025-11-06 08:02:59');
                    """);

            log("✓ Tables created successfully");
        } catch (SQLException ex) {
            log("✗ Table creation error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /* CSV IMPORT */

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

    private void exportAllTablesToCsv() {
        try {
            Files.createDirectories(Path.of("exported_csv"));
        } catch (IOException ignored) {}

        try (Connection conn = connectWithDB()) {
            for (String t : TABLES) {
                exportTableToCsv(conn, t, Path.of("exported_csv", t + ".csv"));
            }
            log("✓ Export complete → exported_csv folder.");
        } catch (SQLException ex) {
            log("✗ Export error: " + ex.getMessage());
        }
    }

    private void exportTableToCsv(Connection conn, String table, Path outFile) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            try (BufferedWriter w = Files.newBufferedWriter(outFile)) {
                for (int i = 1; i <= cols; i++) {
                    w.write(md.getColumnName(i));
                    if (i < cols) w.write(",");
                }
                w.write("\n");

                int count = 0;
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        String val = rs.getString(i);
                        if (val != null && val.contains(",")) val = "\"" + val.replace("\"", "\"\"") + "\"";
                        w.write(val == null ? "" : val);
                        if (i < cols) w.write(",");
                    }
                    w.write("\n");
                    count++;
                }
                log("✓ Exported " + count + " rows from " + table);
            }

        } catch (Exception e) {
            log("✗ Failed to export " + table);
        }
    }

    /* SQL FILE IMPORT */

    private void chooseSqlImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            bgExecutor.submit(() -> importSqlFile(file.toPath()));
        }
    }

    private void importSqlFile(Path file) {
        log("Importing SQL dump: " + file.getFileName());

        try (Connection conn = connectWithDB(); BufferedReader r = Files.newBufferedReader(file)) {
            StringBuilder batch = new StringBuilder();
            Statement st = conn.createStatement();

            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) continue;

                batch.append(line).append(" ");
                if (line.endsWith(";")) {
                    try {
                        st.execute(batch.toString());
                    } catch (SQLException ex) {
                        log("SQL failed: " + ex.getMessage());
                    }
                    batch.setLength(0);
                }
            }

            log("✓ SQL import done.");

        } catch (Exception e) {
            log("✗ SQL import error: " + e.getMessage());
        }
    }

    /* TABLE PREVIEW */

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
                log("✓ Previewed table: " + table + " (" + data.size() + " rows)");

            } catch (SQLException ex) {
                log("✗ Preview failed: " + ex.getMessage());
            }
        });
    }

    /* CONFIG MANAGEMENT */

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

        hostField.setText(config.getProperty("db.host"));
        portField.setText(config.getProperty("db.port"));
        dbField.setText(config.getProperty("db.name"));
        userField.setText(config.getProperty("db.user"));
        passField.setText(config.getProperty("db.password"));
    }

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

    private void testAndLaunchLogin() {
        log("Testing database connection...");

        try {
            Connection conn = connectWithDB();
            log("✓ DATABASE CONNECTED SUCCESSFULLY!");

            saveConfig();

            SwingUtilities.invokeLater(() -> {
                dispose();
                new LoginFrame(conn).setVisible(true);
            });

        } catch (SQLException e) {
            log("✗ Database connection failed: " + e.getMessage());
        }
    }
}