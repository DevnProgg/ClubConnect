package Repository;

import Models.ClubMember;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
/**
 * Repository class for managing C.R.U.D. operations on the {@code club_membership} table.
 * All database operations are performed asynchronously using {@code CompletableFuture}
 * and a dedicated {@code ExecutorService}.
 */
public class ClubMemberRepository {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Connection connection;

    /**
     * Constructs a {@code ClubMemberRepository} with a database connection.
     *
     * @param connection The active database connection.
     */
    public ClubMemberRepository(Connection connection) {
        this.connection = connection;
    }
    /**
     * Adds a new club member record to the database.
     *
     * @param member The {@code ClubMember} object containing the data to insert.
     * @return A {@code CompletableFuture} that returns {@code true} if the member was added successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addMember(ClubMember member) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO club_membership (membership_id, user_id, club_id, membership_status, membership_role, " +
                    "application_date, approved_date, left_date, rejection_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, member.member_id());
                stmt.setLong(2, member.user_id());
                stmt.setLong(3, member.club_id());
                stmt.setString(4, member.membershipStatus());
                stmt.setString(5, member.membershipRole());
                stmt.setString(6, member.applicationDate());
                stmt.setString(7, member.approvedDate());
                stmt.setString(8, member.leftDate());
                stmt.setString(9, member.rejectionReason());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Retrieves a club member record by their unique membership ID.
     *
     * @param memberId The unique ID of the club membership.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code ClubMember} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<ClubMember>> getMemberById(long memberId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM club_membership WHERE membership_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, memberId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a club member record using their user ID.
     * This assumes a user can only be a member of one club or returns the first match.
     *
     * @param userId The ID of the user whose membership is to be retrieved.
     * @return A {@code CompletableFuture} that returns an {@code Optional} containing the {@code ClubMember} if found, or an empty {@code Optional}.
     */
    public CompletableFuture<Optional<ClubMember>> getMemberById(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM club_membership WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }, executor);
    }

    /**
     * Retrieves a list of all club member records from the database.
     *
     * @return A {@code CompletableFuture} that returns a {@code List} of all {@code ClubMember} objects.
     */
    public CompletableFuture<List<ClubMember>> getAllMembers() {
        return CompletableFuture.supplyAsync(() -> {
            List<ClubMember> members = new ArrayList<>();
            String sql = "SELECT * FROM club_membership";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    members.add(mapResultSetToMember(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return members;
        }, executor);
    }

    /**
     * Retrieves a list of all members belonging to a specific club.
     *
     * @param club_id The ID of the club whose members are to be retrieved.
     * @return A {@code CompletableFuture} that returns a {@code List} of {@code ClubMember} objects for the specified club.
     */
    public CompletableFuture<List<ClubMember>> getAllMembers(long club_id) {
        return CompletableFuture.supplyAsync(() -> {
            List<ClubMember> members = new ArrayList<>();
            String sql = "SELECT * FROM club_membership WHERE club_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, club_id);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    members.add(mapResultSetToMember(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return members;
        }, executor);
    }

    /**
     * Updates the details of an existing club member record in the database.
     *
     * @param member The {@code ClubMember} object containing the updated data and the membership ID for identification.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateMember(ClubMember member) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE club_membership SET membership_status=?, membership_role=?, " +
                    "application_date=?, approved_date=?, left_date=?, rejection_reason=? WHERE membership_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, member.membershipStatus());
                stmt.setString(2, member.membershipRole());
                stmt.setString(3, member.applicationDate());
                stmt.setString(4, member.approvedDate());
                stmt.setString(5, member.leftDate());
                stmt.setString(6, member.rejectionReason());
                stmt.setLong(7, member.member_id());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Marks a user's membership as "Alumni" and sets the {@code left_date} when they leave a club.
     *
     * @param club_id The ID of the club being left.
     * @param user_id The ID of the user leaving the club.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> leaveClub(long club_id , long user_id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE club_membership SET membership_role='Alumni', " +
                    "left_date=? WHERE user_id = ? AND club_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                /** Sets the current timestamp for the left_date column. */
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                stmt.setLong(2, user_id);
                stmt.setLong(3, club_id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Marks a specific club member record as "Alumni" and sets the {@code left_date}.
     * This is typically used by an admin to remove a member.
     *
     * @param member_id The unique membership ID to be removed.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> removeMember(long member_id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE club_membership SET membership_role='Alumni', " +
                    "left_date=? WHERE membership_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                /** Sets the current timestamp for the left_date column. */
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                stmt.setLong(2, member_id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Approves a pending club member application, setting the role to 'member' and updating the {@code approved_date}.
     *
     * @param member_id The unique membership ID to be approved.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> approveMember(long member_id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE club_membership SET membership_role='member', " +
                    "approved_date=?, approved_by = 1 WHERE membership_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                /** Sets the current timestamp for the approved_date column. */
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                stmt.setLong(2, member_id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Promotes an existing member to the 'leader' role.
     *
     * @param member_id The unique membership ID to be promoted.
     * @return A {@code CompletableFuture} that returns {@code true} if the record was updated successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> promoteToLeader(long member_id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE club_membership SET membership_role='leader', " +
                    " WHERE membership_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, member_id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Permanently deletes a club member record from the database.
     *
     * @param memberId The unique ID of the club membership to delete.
     * @return A {@code CompletableFuture} that returns {@code true} if the member was deleted successfully, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteMember(long memberId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM club_membership WHERE membership_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, memberId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * Maps a {@code ResultSet} row from the {@code club_membership} table to a {@code ClubMember} object.
     *
     * @param rs The {@code ResultSet} containing the member data.
     * @return A new {@code ClubMember} object populated with data from the current row.
     * @throws SQLException If a database access error occurs or the column names are invalid.
     */
    private ClubMember mapResultSetToMember(ResultSet rs) throws SQLException {
        return new ClubMember(
                rs.getLong("membership_id"),
                rs.getInt("user_id"),
                rs.getInt("club_id"),
                rs.getString("membership_status"),
                rs.getString("membership_role"),
                rs.getString("application_date"),
                rs.getString("approved_date"),
                rs.getString("left_date"),
                rs.getString("rejection_reason"),
                rs.getInt("approved_by")
        );
    }

    /**
     * Shuts down the {@code ExecutorService} gracefully, allowing previously submitted tasks to complete.
     */
    public void shutdown() {
        executor.shutdown();
    }
}