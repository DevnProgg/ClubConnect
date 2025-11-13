package Models;
import java.util.Date;

public record DiscussionForum(long discussionId, String title, String message, Date timestamp, long clubId) {
    // Constructor

}
