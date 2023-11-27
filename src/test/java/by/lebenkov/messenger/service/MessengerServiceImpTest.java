package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import by.lebenkov.messenger.repository.ConversationRepository;
import by.lebenkov.messenger.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

class MessengerServiceImpTest {

    @Mock
    private CommonParticipantsServiceImp commonParticipantsServiceImp;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationPartRepository conversationPartRepository;
    @Mock
    private ConversationServiceImp conversationServiceImp;

    private MessengerServiceImp messengerServiceImp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messengerServiceImp = new MessengerServiceImp(
                accountRepository,
                messageRepository,
                conversationRepository,
                conversationPartRepository,
                conversationServiceImp,
                commonParticipantsServiceImp
        );
    }

    @Test
    void canGetLastMessage() {
        String senderUsername = "five";
        String receiverUsername = "admin";

        Account account1 = new Account();
        account1.setIdAccount(1);

        Account account2 = new Account();
        account2.setIdAccount(2);

        Conversation conversation1 = new Conversation();
        conversation1.setId(47);

        List<ConversationParticipant> commonParticipants = getConversationParticipants(account1, account2, conversation1);

        given(commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername)).willReturn(commonParticipants);
        when(conversationRepository.findByParticipantsIn(commonParticipants)).thenReturn(Optional.of(conversation1));

        String lastMessage = messengerServiceImp.getLastMessage(senderUsername, receiverUsername);

        assertEquals(lastMessage.length() > 1, lastMessage.length());
    }

    private static List<ConversationParticipant> getConversationParticipants(Account account1, Account account2, Conversation conversation1) {
        List<ConversationParticipant> commonParticipants = new ArrayList<>();

        ConversationParticipant senderParticipant = new ConversationParticipant(116, account1, conversation1);
        ConversationParticipant receiverParticipant = new ConversationParticipant(117, account2, conversation1);

        commonParticipants.add(senderParticipant);
        commonParticipants.add(receiverParticipant);
        return commonParticipants;
    }
}