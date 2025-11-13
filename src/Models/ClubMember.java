package Models;

public record ClubMember(long member_id, long user_id, long club_id, String membershipStatus, String membershipRole,
                         String applicationDate, String approvedDate, String leftDate, String rejectionReason, long approvedBY) {
}
