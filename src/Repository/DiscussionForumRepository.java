package Repository;

import Models.DiscussionForum;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Repository class for managing C.R.U.D. operations on the {@code discussion_forum} table.
 * It provides asynchronous methods using {@code CompletableFuture} for all database interactions.
 */
public class DiscussionForumRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code DiscussionForumRepository} with an active database connection.
     *
     * @param connection The active database connection.
     */
    public DiscussionForumRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new discussion entry to the forum table.
     *
     * @param discussion The {@code DiscussionForum} object containing the discussion data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the discussion was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addDiscussion(DiscussionForum discussion) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO discussion_forum (discussion_id, title, message, timestamp, club_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, discussion.discussionId());
                stmt.setString(2, discussion.title());
                stmt.setString(3, discussion.message());
                stmt.setTimestamp(4, new java.sql.Timestamp(discussion.timestamp().getTime()));
                stmt.setLong(5, discussion.clubId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a single discussion record by its unique ID.
     *
     * @param discussionId The unique ID of the discussion.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code DiscussionForum} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<DiscussionForum>> getDiscussionById(long discussionId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM discussion_forum WHERE discussion_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, discussionId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToDiscussion(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a list of all discussion records from the database, ordered by timestamp descending.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code DiscussionForum} objects.
     */
    public CompletableFuture<List<DiscussionForum>> getAllDiscussions() {
        return CompletableFuture.supplyAsync(() -> {
            List<DiscussionForum> discussions = new ArrayList<>();
            String sql = "SELECT * FROM discussion_forum ORDER BY timestamp DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    discussions.add(mapResultSetToDiscussion(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return discussions;
        }, executor);
    }

    /**
     * Updates the title, message, and club association of an existing discussion record.
     *
     * @param discussion The {@code DiscussionForum} object containing the updated data and the discussion ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateDiscussion(DiscussionForum discussion) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE discussion_forum SET title=?, message=?, timestamp=?, club_id=? WHERE discussion_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, discussion.title());
                stmt.setString(2, discussion.message());
                stmt.setTimestamp(3, new java.sql.Timestamp(discussion.timestamp().getTime()));
                stmt.setLong(4, discussion.clubId());
                stmt.setLong(5, discussion.discussionId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Deletes a discussion record from the database using its unique ID.
     *
     * @param discussionId The unique ID of the discussion to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the discussion was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteDiscussion(long discussionId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM discussion_forum WHERE discussion_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, discussionId);
                return stmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code discussion_forum} table to a {@code DiscussionForum} object.
     *
     * @param rs The {@code ResultSet} containing the discussion data.
     * @return A new {@code DiscussionForum} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private DiscussionForum mapResultSetToDiscussion(ResultSet rs) throws SQLException {
        return new DiscussionForum(
                rs.getLong("discussion_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getTimestamp("timestamp"),
                rs.getLong("club_id")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}