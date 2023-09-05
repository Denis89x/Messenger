package by.lebenkov.messenger.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageView {
    private Message message;
    private boolean displayProfile;

    public MessageView(Message message) {
        this.message = message;
        this.displayProfile = false;
    }
}
