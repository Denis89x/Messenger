package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

class CommonParticipantsServiceImpTest {

    @Mock
    private ConversationPartRepository conversationPartRepository;

    private CommonParticipantsServiceImp commonParticipantsServiceImp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commonParticipantsServiceImp = new CommonParticipantsServiceImp(conversationPartRepository);
    }

    @Test
    void canFindCommonParticipants() {
        String senderUsername = "five";
        String receiverUsername = "admin";

        Account account1 = new Account();
        account1.setIdAccount(1);

        Account account2 = new Account();
        account2.setIdAccount(2);

        Conversation conversation1 = new Conversation();
        conversation1.setId(101);

        Conversation conversation2 = new Conversation();
        conversation2.setId(102);

        ConversationParticipant senderParticipant1 = new ConversationParticipant(1, account1, conversation1);
        ConversationParticipant senderParticipant2 = new ConversationParticipant(2, account1, conversation2);

        List<ConversationParticipant> senderParticipants = List.of(senderParticipant1, senderParticipant2);

        ConversationParticipant receiverParticipant1 = new ConversationParticipant(3, account2, conversation1);

        List<ConversationParticipant> receiverParticipants = List.of(receiverParticipant1);

        given(conversationPartRepository.findAllByAccountUsername(senderUsername)).willReturn(senderParticipants);
        given(conversationPartRepository.findAllByAccountUsername(receiverUsername)).willReturn(receiverParticipants);

        // When
        List<ConversationParticipant> commonParticipants = commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername);

        // Then
        assertEquals(2, commonParticipants.size());
    }
}