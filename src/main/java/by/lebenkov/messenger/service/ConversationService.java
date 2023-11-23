package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;

public interface ConversationService {
    Conversation createConversationAndUpdateParticipants(ConversationParticipant sender, ConversationParticipant receiver);
}
