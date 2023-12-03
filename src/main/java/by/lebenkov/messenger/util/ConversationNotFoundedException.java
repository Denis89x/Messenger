package by.lebenkov.messenger.util;

public class ConversationNotFoundedException extends RuntimeException {
    public ConversationNotFoundedException(String message) {
        super(message);
    }

    public ConversationNotFoundedException(String message, Throwable cause) {
        super(message, cause);
    }
}
