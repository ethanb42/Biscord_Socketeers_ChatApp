package sample;

//An Exception to indicate a username already exists on the server
public class DuplicateUsernameException extends Exception {
    public DuplicateUsernameException(String errorMessage) {
        super(errorMessage);
    }
}