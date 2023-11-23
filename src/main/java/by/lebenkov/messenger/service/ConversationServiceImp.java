package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.ConversationRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationServiceImp implements ConversationService {

    ConversationRepository conversationRepository;

    @Override
    @Transactional
    public Conversation createConversationAndUpdateParticipants(ConversationParticipant sender, ConversationParticipant receiver) {
        Conversation newConversation = new Conversation();
        conversationRepository.save(newConversation);

        conversationRepository.updateParticipantConversation(sender.getId(), newConversation.getId());

        conversationRepository.updateParticipantConversation(receiver.getId(), newConversation.getId());

        return newConversation;
    }
}