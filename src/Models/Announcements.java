package Models;

import java.util.Date;

public record Announcements(long announcement_id, long club_id, String content, String Title, long target_audience, Date expiryDate, Date createdDate){}