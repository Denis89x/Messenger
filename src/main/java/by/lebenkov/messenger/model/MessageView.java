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
    private boolean picture;
    private boolean myAccount;

    public MessageView(Message message) {
        this.message = message;
        this.displayProfile = false;
    }
}
