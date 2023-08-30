package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;

public interface ConversationService {
    public Conversation createConversationAndUpdateParticipants(ConversationParticipant sender, ConversationParticipant receiver);
}
