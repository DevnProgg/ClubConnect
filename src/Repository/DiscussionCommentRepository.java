package Repository;

import Models.DiscussionComment;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Repository class for managing C.R.U.D. operations on the {@code discussion_comments} table.
 * All database operations are executed asynchronously using {@code CompletableFuture}
 * and a dedicated {@code ExecutorService}.
 */
public class DiscussionCommentRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code DiscussionCommentRepository} with an active database connection.
     *
     * @param connection The active database connection.
     */
    public DiscussionCommentRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new comment to a discussion in the database.
     *
     * @param comment The {@code DiscussionComment} object containing the message, user ID, and discussion ID.
     * @return A {@code CompletableFuture} that returns {@code true} if the comment was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addComment(DiscussionComment comment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO discussion_comments (message, user_id, discussion_id, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, comment.message());
                stmt.setLong(2, comment.userId());
                stmt.setLong(3, comment.discussionId());
                stmt.setTimestamp(4, new java.sql.Timestamp(comment.timestamp().getTime()));
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to add comment: " + e.getMessage());
                return false;
            }
        }, executor);
    }


    /**
     * Retrieves a single comment record by its unique ID.
     *
     * @param commentId The unique ID of the comment.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code DiscussionComment} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<DiscussionComment>> getCommentById(long commentId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM discussion_comments WHERE comment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, commentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToComment(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves all comments associated with a specific discussion ID, ordered by timestamp ascending.
     *
     * @param discussionId The ID of the discussion whose comments are to be retrieved.
     * @return A {@code CompletableFuture} that returns a {@code List} of {@code DiscussionComment} objects.
     */
    public CompletableFuture<List<DiscussionComment>> getCommentsByDiscussionId(long discussionId) {
        return CompletableFuture.supplyAsync(() -> {
            List<DiscussionComment> comments = new ArrayList<>();
            String sql = "SELECT * FROM discussion_comments WHERE discussion_id = ? ORDER BY timestamp ASC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, discussionId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return comments;
        }, executor);
    }

    /**
     * Retrieves all comment records from the database, ordered by timestamp descending.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code DiscussionComment} objects.
     */
    public CompletableFuture<List<DiscussionComment>> getAllComments() {
        return CompletableFuture.supplyAsync(() -> {
            List<DiscussionComment> comments = new ArrayList<>();
            String sql = "SELECT * FROM discussion_comments ORDER BY timestamp DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return comments;
        }, executor);
    }

    /**
     * Updates the content and other details of an existing comment record.
     *
     * @param comment The {@code DiscussionComment} object containing the updated data and the comment ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateComment(DiscussionComment comment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE discussion_comments SET message=?, user_id=?, discussion_id=?, timestamp=? WHERE comment_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, comment.message());
                stmt.setLong(2, comment.userId());
                stmt.setLong(3, comment.discussionId());
                stmt.setTimestamp(4, new java.sql.Timestamp(comment.timestamp().getTime()));
                stmt.setLong(5, comment.commentId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Deletes a comment record from the database using its unique ID.
     *
     * @param commentId The unique ID of the comment to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the comment was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteComment(long commentId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM discussion_comments WHERE comment_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, commentId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code discussion_comments} table to a {@code DiscussionComment} object.
     *
     * @param rs The {@code ResultSet} containing the comment data.
     * @return A new {@code DiscussionComment} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private DiscussionComment mapResultSetToComment(ResultSet rs) throws SQLException {
        return new DiscussionComment(
                rs.getLong("comment_id"),
                rs.getString("message"),
                rs.getLong("user_id"),
                rs.getLong("discussion_id"),
                rs.getTimestamp("timestamp")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}