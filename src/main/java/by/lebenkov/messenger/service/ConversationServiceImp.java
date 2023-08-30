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
    private final ConversationPartRepository conversationPartRepository;

    @Autowired
    public ConversationServiceImp(ConversationRepository conversationRepository, ConversationPartRepository conversationPartRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationPartRepository = conversationPartRepository;
    }

    @Override
    @Transactional
    public Conversation createConversationAndUpdateParticipants(ConversationParticipant sender, ConversationParticipant receiver) {
        System.out.println("Creating new conversation...");
        Conversation newConversation = new Conversation();
        System.out.println("newConversation = " + newConversation);
        conversationRepository.save(newConversation);
        System.out.println("Saved.");

        conversationRepository.updateParticipantConversation(sender.getId(), newConversation.getId());
        System.out.println("After updating sender: " + sender);
        System.out.println("After updating sender conversation: " + sender.getConversation());

        conversationRepository.updateParticipantConversation(receiver.getId(), newConversation.getId());
        Optional<ConversationParticipant> newReceiver = conversationPartRepository.findById(receiver.getId());
        System.out.println("new receiver = " + newReceiver);
        System.out.println("After updating receiver conversation: " + receiver.getConversation());

        System.out.println("New conversation created: " + newConversation.getId());
        return newConversation;
    }
}
