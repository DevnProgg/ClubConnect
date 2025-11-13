package Models;

public record Club(long clubId, String name, String status, String category, String description,
                   double budgetProposal, int memberCapacity, double approvedBudget, long approvedBy, String createdDate, long createdBy) {
}