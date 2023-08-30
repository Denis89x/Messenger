package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommonParticipantsServiceImp implements CommonParticipantsService {

    private final ConversationPartRepository conversationPartRepository;

    @Autowired
    public CommonParticipantsServiceImp(ConversationPartRepository conversationPartRepository) {
        this.conversationPartRepository = conversationPartRepository;
    }

    @Override
    public List<ConversationParticipant> findCommonParticipants(String senderUsername, String receiverUsername) {
        List<ConversationParticipant> senderParticipants = conversationPartRepository.findAllByAccountUsername(senderUsername);
        List<ConversationParticipant> receiverParticipants = conversationPartRepository.findAllByAccountUsername(receiverUsername);

        List<ConversationParticipant> commonParticipants = new ArrayList<>();
        System.err.println("common first" + commonParticipants);

        for (ConversationParticipant senderParticipant : senderParticipants) {
            for (ConversationParticipant receiverParticipant : receiverParticipants) {
                if (senderParticipant.getConversation() != null && receiverParticipant.getConversation() != null) {
                    if (senderParticipant.getConversation().getId().equals(receiverParticipant.getConversation().getId())) {
                        commonParticipants.add(senderParticipant);
                        commonParticipants.add(receiverParticipant);
                    }
                }
            }
        }

        return commonParticipants;
    }
}
