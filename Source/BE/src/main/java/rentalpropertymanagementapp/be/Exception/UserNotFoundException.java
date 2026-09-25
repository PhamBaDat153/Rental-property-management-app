package rentalpropertymanagementapp.be.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User không tồn tại");
    }
}
