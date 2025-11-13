package Repository;

import Models.Club;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Repository class for managing C.R.U.D. operations on the {@code clubs} table.
 * All database operations are performed asynchronously using {@code CompletableFuture}
 * and a dedicated {@code ExecutorService}.
 */
public class ClubRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code ClubRepository} with a database connection.
     *
     * @param connection The active database connection.
     */
    public ClubRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new club record into the database.
     *
     * @param club The {@code Club} object containing the data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the club was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addClub(Club club) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO clubs (club_id, name, status, category, description, budget_proposal, " +
                    "member_capacity, approved_budget, approved_by, created_date, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, club.clubId());
                stmt.setString(2, club.name());
                stmt.setString(3, club.status());
                stmt.setString(4, club.category());
                stmt.setString(5, club.description());
                stmt.setDouble(6, club.budgetProposal());
                stmt.setInt(7, club.memberCapacity());
                stmt.setDouble(8, club.approvedBudget());
                stmt.setLong(9, club.approvedBy());
                stmt.setString(10, club.createdDate());
                stmt.setLong(11, club.createdBy());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a club record by its unique ID.
     *
     * @param clubId The unique ID of the club (expected to be a String representing a long value).
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code Club} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<Club>> getClubById(String clubId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM clubs WHERE club_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, clubId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToClub(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a list of all club records from the database.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code Club} objects.
     */
    public CompletableFuture<List<Club>> getAllClubs() {
        return CompletableFuture.supplyAsync(() -> {
            List<Club> clubs = new ArrayList<>();
            String sql = "SELECT * FROM clubs";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    clubs.add(mapResultSetToClub(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return clubs;
        }, executor);
    }

    /**
     * Updates the details of an existing club record in the database.
     *
     * @param club The {@code Club} object containing the updated data and the club ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateClub(Club club) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE clubs SET name=?, status=?, category=?, description=?, budget_proposal=?, " +
                    "member_capacity=?, approved_budget=?, approved_by=?, created_date=?, created_by=? " +
                    "WHERE club_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, club.clubId());
                stmt.setString(2, club.name());
                stmt.setString(3, club.status());
                stmt.setString(4, club.category());
                stmt.setString(5, club.description());
                stmt.setDouble(6, club.budgetProposal());
                stmt.setInt(7, club.memberCapacity());
                stmt.setDouble(8, club.approvedBudget());
                stmt.setLong(9, club.approvedBy());
                stmt.setString(10, club.createdDate());
                stmt.setLong(11, club.createdBy());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Updates a club's status to 'Inactive' instead of physically deleting the record (soft delete).
     *
     * @param clubId The unique ID of the club to be marked as inactive.
     * @return A {@code CompletableFuture} that returns {@code true} if the status was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteClub(long clubId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE clubs SET status = 'Inactive' WHERE club_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, clubId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code clubs} table to a {@code Club} object.
     *
     * @param rs The {@code ResultSet} containing the club data.
     * @return A new {@code Club} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private Club mapResultSetToClub(ResultSet rs) throws SQLException {
        return new Club(
                rs.getLong("club_id"),
                rs.getString("name"),
                rs.getString("status"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("budget_proposal"),
                rs.getInt("member_capacity"),
                rs.getDouble("approved_budget"),
                rs.getInt("approved_by"),
                rs.getString("created_date"),
                rs.getInt("created_by")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}