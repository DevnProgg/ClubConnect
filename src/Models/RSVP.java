package Models;

public record RSVP(long RSVP_ID, long user_id, long event_id, String status, String rsvpDate, boolean attendanceMarked) {
}
