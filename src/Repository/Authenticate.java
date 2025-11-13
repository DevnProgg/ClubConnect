package Repository;

import Models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Handles the authentication process for a system user by querying the database.
 * Authentication is performed asynchronously using a {@code CompletableFuture}.
 */
public class Authenticate {
    private final Connection conn;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final String username;

    /**
     * Constructs an {@code Authenticate} instance.
     *
     * @param conn The active database connection.
     * @param username The username provided by the user for authentication.
     */
    public Authenticate(Connection conn, String username){
        this.conn = conn;
        this.username = username;
    }

    /**
     * Attempts to authenticate a user asynchronously by checking the provided username
     * against the database for an active user record.
     *
     * @return A {@code CompletableFuture} that, upon completion, returns an {@code Optional<?>}.
     * The {@code Optional} will contain the {@code User} object if authentication is successful and the user is active,
     * or an empty {@code Optional} otherwise. The actual object type is {@code User}.
     */
    public CompletableFuture<Optional<?>> AuthenticateUser(){
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM system_users WHERE username = ?  AND status = 'Active'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                /**
                 * If a matching and active user record is found, map the result set to a User object.
                 */
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }
    /**
     * Maps a {@code ResultSet} row from the {@code system_users} table to a {@code User} object.
     *
     * @param rs The {@code ResultSet} containing the user data.
     * @return A new {@code User} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("full_names"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getLong("role_id"),
                rs.getString("status"),
                rs.getString("registration_date")
        );
    }

}