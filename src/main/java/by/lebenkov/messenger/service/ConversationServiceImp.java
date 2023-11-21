package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import by.lebenkov.messenger.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConversationServiceImp implements ConversationService {

    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationServiceImp(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

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