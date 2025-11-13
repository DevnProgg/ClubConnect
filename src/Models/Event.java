package Models;

import java.util.Date;

public record Event(long eventId, String title, String type, String description, Date date, String status,
                    String startTime, long resourceId, String endTime, boolean isBudgetRequested, double budgetAmount,
                    String budgetStatus, long approvedBy, Date createdDate, long createdBy, long clubId) {

    /* Constructor */

}
