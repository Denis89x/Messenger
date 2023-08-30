package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.ConversationParticipant;

import java.util.List;

public interface CommonParticipantsService {
    public List<ConversationParticipant> findCommonParticipants(String senderUsername, String receiverUsername);
}
