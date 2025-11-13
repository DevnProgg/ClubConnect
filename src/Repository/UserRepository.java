package Repository;

import Models.User;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
/**
 * Repository class for managing C.R.U.D. operations on the {@code system_users} table.
 * All database operations are executed asynchronously using {@code CompletableFuture}
 * and a dedicated {@code ExecutorService}.
 */
public class UserRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code UserRepository} with an active database connection.
     *
     * @param connection The active database connection.
     */
    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new user record to the database.
     *
     * @param user The {@code User} object containing the data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the user was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO system_users (user_id, full_names, email, username, password_hash, role_id, " +
                    " status) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1,user.userId());
                stmt.setString(2, user.fullNames());
                stmt.setString(3, user.email());
                stmt.setString(4, user.username());
                stmt.setString(5, user.passwordHash());
                stmt.setLong(6, user.roleId());
                stmt.setString(7, user.status());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a single user record by their unique ID.
     *
     * @param userId The unique ID of the user.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code User} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<User>> getUserById(long userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM system_users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();
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
     * Retrieves a list of all user records from the database.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code User} objects.
     */
    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM system_users";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }, executor);
    }

    /**
     * Updates the basic profile details (full name, email, username, and status) of an existing user record.
     *
     * @param user The {@code User} object containing the updated data and the user ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE system_users SET fullnames=?, email=?, username=?, " +
                    "status=? WHERE user_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.fullNames());
                stmt.setString(2, user.email());
                stmt.setString(3, user.username());
                stmt.setString(4, user.status());
                stmt.setLong(5, user.userId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Deletes a user record from the database using their unique ID.
     *
     * @param userId The unique ID of the user to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the user was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteUser(long userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM system_users WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
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

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}