package sample;

//An Exception to indicate a user's inputted name is too long
public class UsernameLengthExceeded extends Exception {
    public UsernameLengthExceeded(String errorMessage) {
        super(errorMessage);
    }

}
