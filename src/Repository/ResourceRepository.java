package Repository;

import Models.Resource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Repository class for managing C.R.U.D. operations on the {@code resources} table.
 * It provides asynchronous methods using {@code CompletableFuture} for all database interactions.
 * This class handles the management of shared resources such as venues, equipment, etc.
 */
public class ResourceRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code ResourceRepository} with an active database connection.
     *
     * @param connection The active database connection.
     */
    public ResourceRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new resource record to the database.
     *
     * @param resource The {@code Resource} object containing the data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the resource was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addResource(Resource resource) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO resources (resource_id, name, type, capacity, is_available, location) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, resource.resourceId());
                stmt.setString(2, resource.name());
                stmt.setString(3, resource.type());
                stmt.setInt(4, resource.capacity());
                stmt.setBoolean(5, resource.isAvailable());
                stmt.setString(6, resource.location());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a single resource record by its unique ID.
     *
     * @param resourceId The unique ID of the resource.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code Resource} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<Resource>> getResourceById(long resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM resources WHERE resource_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, resourceId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToResource(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a list of all resource records from the database.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code Resource} objects.
     */
    public CompletableFuture<List<Resource>> getAllResources() {
        return CompletableFuture.supplyAsync(() -> {
            List<Resource> resources = new ArrayList<>();
            String sql = "SELECT * FROM resources";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    resources.add(mapResultSetToResource(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return resources;
        }, executor);
    }

    /**
     * Updates the details of an existing resource record in the database.
     *
     * @param resource The {@code Resource} object containing the updated data and the resource ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateResource(Resource resource) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE resources SET name=?, type=?, capacity=?, is_available=?, location=? WHERE resource_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, resource.name());
                stmt.setString(2, resource.type());
                stmt.setInt(3, resource.capacity());
                stmt.setBoolean(4, resource.isAvailable());
                stmt.setString(5, resource.location());
                stmt.setLong(6, resource.resourceId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Deletes a resource record from the database using its unique ID.
     *
     * @param resourceId The unique ID of the resource to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the resource was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteResource(long resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM resources WHERE resource_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, resourceId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code resources} table to a {@code Resource} object.
     *
     * @param rs The {@code ResultSet} containing the resource data.
     * @return A new {@code Resource} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getLong("resource_id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getInt("capacity"),
                rs.getBoolean("is_available"),
                rs.getString("location")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}