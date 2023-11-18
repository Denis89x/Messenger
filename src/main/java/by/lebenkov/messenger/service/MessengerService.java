package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.model.Message;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

public interface MessengerService {
    void getAccount(Model model);
    Account getAuthenticatedAccount();
    /*void sendMessage(String senderUsername, String receiverUsername, String content);*/
    Optional<Conversation> findByParticipants(List<ConversationParticipant> participants);
    /*void commitMessage(ConversationParticipant sender, ConversationParticipant receiver, String content);*/
    List<Message> getConversationMessages(String senderUsername, String receiverUsername);
    ConversationParticipant createParticipant(Account account);
    Conversation findOrCreateConversation(ConversationParticipant sender, ConversationParticipant receiver);
    String getCurrentUserUsername();
    List<String> getOtherUsernames();
    List<Account> getDialogUsernames(String currentUser);
    Optional<Account> findAccountByUsername(String username);
}
