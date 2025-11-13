package Models;

public record User(long userId, String fullNames, String email, String username, String passwordHash, long roleId, String status,String registrationDate) {
}
