package Repository;

import Models.Event;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;


public class EventRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    public EventRepository(Connection connection) {
        this.connection = connection;
    }

    /** CREATE - add a new event */
    public CompletableFuture<Boolean> addEvent(Event event) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO events (event_id, title, type, description, date, status, start_time, " +
                    "resource_id, end_time, is_budget_requested, budget_amount, budget_status, approved_by, " +
                    "created_date, created_by, club_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, event.eventId());
                stmt.setString(2, event.title());
                stmt.setString(3, event.type());
                stmt.setString(4, event.description());
                stmt.setDate(5, new java.sql.Date(event.date().getTime()));
                stmt.setString(6, event.status());
                stmt.setString(7, event.startTime());
                stmt.setLong(8, event.resourceId());
                stmt.setString(9, event.endTime());
                stmt.setBoolean(10, event.isBudgetRequested());
                stmt.setDouble(11, event.budgetAmount());
                stmt.setString(12, event.budgetStatus());
                stmt.setLong(13, event.approvedBy());
                stmt.setDate(14, new java.sql.Date(event.createdDate().getTime()));
                stmt.setLong(15, event.createdBy());
                stmt.setLong(16, event.clubId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /** READ - get an event by ID */
    public CompletableFuture<Optional<Event>> getEventById(long eventId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM events WHERE event_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, eventId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToEvent(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /** READ - get all events */
    public CompletableFuture<List<Event>> getAllEvents() {
        return CompletableFuture.supplyAsync(() -> {
            List<Event> events = new ArrayList<>();
            String sql = "SELECT * FROM events";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    events.add(mapResultSetToEvent(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return events;
        }, executor);
    }

    /** UPDATE - update event details */
    public CompletableFuture<Boolean> updateEvent(Event event) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE events SET title=?, type=?, description=?, date=?, status=?, start_time=?, " +
                    "resource_id=?, end_time=?, is_budget_requested=?, budget_amount=?, budget_status=?, " +
                    "approved_by=?, created_date=?, created_by=?, club_id=? WHERE event_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, event.title());
                stmt.setString(2, event.type());
                stmt.setString(3, event.description());
                stmt.setDate(4, new java.sql.Date(event.date().getTime()));
                stmt.setString(5, event.status());
                stmt.setString(6, event.startTime());
                stmt.setLong(7, event.resourceId());
                stmt.setString(8, event.endTime());
                stmt.setBoolean(9, event.isBudgetRequested());
                stmt.setDouble(10, event.budgetAmount());
                stmt.setString(11, event.budgetStatus());
                stmt.setLong(12, event.approvedBy());
                stmt.setDate(13, new java.sql.Date(event.createdDate().getTime()));
                stmt.setLong(14, event.createdBy());
                stmt.setLong(15, event.clubId());
                stmt.setLong(16, event.eventId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /** DELETE - remove event by ID */
    public CompletableFuture<Boolean> deleteEvent(long eventId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM events WHERE event_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, eventId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /** Helper method - convert ResultSet into Event object */
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getLong("event_id"),
                rs.getString("title"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getDate("date"),
                rs.getString("status"),
                rs.getString("start_time"),
                rs.getLong("resource_id"),
                rs.getString("end_time"),
                rs.getBoolean("is_budget_requested"),
                rs.getDouble("budget_amount"),
                rs.getString("budget_status"),
                rs.getLong("approved_by"),
                rs.getDate("created_date"),
                rs.getLong("created_by"),
                rs.getLong("club_id")
        );
    }

    /** Gracefully shut down executor */
    public void shutdown() {
        executor.shutdown();
    }
}
