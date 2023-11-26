package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommonParticipantsServiceImp implements CommonParticipantsService {

    ConversationPartRepository conversationPartRepository;

    @Override
    public List<ConversationParticipant> findCommonParticipants(String senderUsername, String receiverUsername) {
        List<ConversationParticipant> senderParticipants = conversationPartRepository.findAllByAccountUsername(senderUsername);
        List<ConversationParticipant> receiverParticipants = conversationPartRepository.findAllByAccountUsername(receiverUsername);

        return senderParticipants.stream()
                .filter(senderParticipant -> senderParticipant.getConversation() != null)
                .flatMap(senderParticipant ->
                        receiverParticipants.stream()
                                .filter(receiverParticipant ->
                                        receiverParticipant.getConversation() != null &&
                                                senderParticipant.getConversation().getId().equals(receiverParticipant.getConversation().getId())
                                )
                                .map(receiverParticipant -> List.of(senderParticipant, receiverParticipant))
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}