package Repository;

import Models.RSVP;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
/**
 * Repository class for managing C.R.U.D. operations on the {@code rsvps} table.
 * It provides asynchronous methods using {@code CompletableFuture} for all database interactions.
 */
public class RSVPRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs an {@code RSVPRepository} with an active database connection.
     *
     * @param connection The active database connection.
     */
    public RSVPRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new RSVP (Response/Reservation) record to the database for an event.
     *
     * @param rsvp The {@code RSVP} object containing the response data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the RSVP was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addRSVP(RSVP rsvp) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO rsvps (rsvp_id, user_id, event_id, status, date, attendance_marked) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, rsvp.RSVP_ID());
                stmt.setLong(2, rsvp.user_id());
                stmt.setLong(3, rsvp.event_id());
                stmt.setString(4, rsvp.status());
                stmt.setString(5, String.valueOf(rsvp.rsvpDate()));
                stmt.setBoolean(6, rsvp.attendanceMarked());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a single RSVP record by its unique ID.
     *
     * @param rsvpId The unique ID of the RSVP record.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code RSVP} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<RSVP>> getRSVPById(long rsvpId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM rsvps WHERE rsvp_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, rsvpId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToRSVP(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a list of all RSVP records associated with a specific event ID.
     *
     * @param eventId The ID of the event whose RSVPs are to be retrieved.
     * @return A {@code CompletableFuture} that returns a {@code List} of {@code RSVP} objects for the specified event.
     */
    public CompletableFuture<List<RSVP>> getRSVPByEventId(long eventId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM rsvps WHERE event_id = ?";
            List<RSVP> list = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, eventId);
                ResultSet rs = stmt.executeQuery();
                /**
                 * Loop through all resulting rows to build the list of RSVPs for the event.
                 */
                if (rs.next()) {
                    list.add(mapResultSetToRSVP(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }, executor);
    }

    /**
     * Retrieves a list of all RSVP records from the database.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code RSVP} objects.
     */
    public CompletableFuture<List<RSVP>> getAllRSVPs() {
        return CompletableFuture.supplyAsync(() -> {
            List<RSVP> list = new ArrayList<>();
            String sql = "SELECT * FROM rsvps";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToRSVP(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }, executor);
    }

    /**
     * Updates the details of an existing RSVP record in the database.
     *
     * @param rsvp The {@code RSVP} object containing the updated data and the RSVP ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateRSVP(RSVP rsvp) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE rsvps SET user_id=?, event_id=?, status=?, date=?, attendance_marked=? " +
                    "WHERE rsvp_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, rsvp.user_id());
                stmt.setLong(2, rsvp.event_id());
                stmt.setString(3, rsvp.status());
                stmt.setString(4, String.valueOf(rsvp.rsvpDate()));
                stmt.setBoolean(5, rsvp.attendanceMarked());
                stmt.setLong(6, rsvp.RSVP_ID());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Deletes an RSVP record from the database using its unique ID.
     *
     * @param rsvpId The unique ID of the RSVP to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the RSVP was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteRSVP(long rsvpId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM rsvps WHERE rsvp_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, rsvpId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code rsvps} table to an {@code RSVP} object.
     *
     * @param rs The {@code ResultSet} containing the RSVP data.
     * @return A new {@code RSVP} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private RSVP mapResultSetToRSVP(ResultSet rs) throws SQLException {
        return new RSVP(
                rs.getLong("rsvp_id"),
                rs.getLong("user_id"),
                rs.getLong("event_id"),
                rs.getString("status"),
                rs.getString("date"),
                rs.getBoolean("attendance_marked")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}