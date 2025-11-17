package Repository;

import Models.Announcements;

import java.sql.Connection;
import java.sql.SQLException;

public class AnnouncementsRepository {
    public AnnouncementsRepository(Connection conn) {
    }

    public Announcements getAnnouncementByID(int id){
        return (Announcements) new Object();
    }

    public void insertAnnouncement(Announcements ann) throws SQLException {
    }
}
