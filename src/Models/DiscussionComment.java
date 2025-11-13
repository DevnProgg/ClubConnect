package Models;

import java.util.Date;

public record DiscussionComment(long commentId, String message, long userId, long discussionId, Date timestamp) {
    // Constructor
}
