package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.*;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import by.lebenkov.messenger.repository.ConversationRepository;
import by.lebenkov.messenger.repository.MessageRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessengerServiceImp implements MessengerService {

    AccountRepository accountRepository;
    MessageRepository messageRepository;
    ConversationRepository conversationRepository;
    ConversationPartRepository conversationPartRepository;
    ConversationServiceImp conversationServiceImp;
    CommonParticipantsServiceImp commonParticipantsServiceImp;

    Logger logger = LoggerFactory.getLogger(MessengerServiceImp.class);

    @Override
    public void getAccount(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Account> account = accountRepository.findByUsername(username);

        model.addAttribute("account", account.orElse(null));
    }

    @Override
    public Account getAuthenticatedAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findByUsername(authentication.getName()).orElse(null);
    }

    @Transactional
    public void commitMessageOrFindParticipants(String senderUsername, String receiverUsername, String content, String type) {
        List<ConversationParticipant> commonParticipants = commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername);

        ConversationParticipant sender = null;
        ConversationParticipant receiver = null;

        if (!commonParticipants.isEmpty()) {
            sender = commonParticipants.get(0);
            receiver = commonParticipants.get(1);
        }

        Optional<Account> senderM = accountRepository.findByUsername(senderUsername);
        Optional<Account> receiverM = accountRepository.findByUsername(receiverUsername);

        if (senderM.isPresent() && receiverM.isPresent() && (sender == null && receiver == null)) {
            processParticipants(senderM, receiverM, content, type);
        }

        if (sender != null && receiver != null) {
            processIndividualParticipants(sender, receiver, content, type);
        }
    }

    @Transactional
    public void processParticipants(Optional<Account> senderM, Optional<Account> receiverM, String content, String type) {
        List<ConversationParticipant> senders = conversationPartRepository.findAllByAccount(senderM.get());
        List<ConversationParticipant> receivers = conversationPartRepository.findAllByAccount(receiverM.get());
        ConversationParticipant senderParticipant = senders.isEmpty() ? createParticipant(senderM.get()) : senders.get(0);
        ConversationParticipant receiverParticipant = receivers.isEmpty() ? createParticipant(receiverM.get()) : receivers.get(0);

        if (type.equals("commit")) {
            commitMessage(senderParticipant, receiverParticipant, content);
        } else {
            findOrCreateConversation(senderParticipant, receiverParticipant);
        }
    }

    @Transactional
    public void processIndividualParticipants(ConversationParticipant sender, ConversationParticipant receiver, String content, String type) {
        if (type.equals("commit")) {
            commitMessage(sender, receiver, content);
        } else {
            findOrCreateConversation(sender, receiver);
        }
    }

    @Override
    public Optional<Conversation> findByParticipants(List<ConversationParticipant> participants) {
        return conversationRepository.findByParticipantsIn(participants);
    }


    @Transactional
    public void commitMessage(ConversationParticipant sender, ConversationParticipant receiver, String content) {
        Conversation conversation = findOrCreateConversation(sender, receiver);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender.getAccount());
        message.setContent(content);
        message.setDateTime(LocalDateTime.now());
        message.setIsChecked(false);

        messageRepository.save(message);
    }

    @Override
    public List<Message> getConversationMessages(String senderUsername, String receiverUsername) {
        List<ConversationParticipant> commonParticipants = commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername);

        ConversationParticipant sender = commonParticipants.get(0);
        ConversationParticipant receiver = commonParticipants.get(1);

        if (sender != null && receiver != null) {
            List<Message> messages = new ArrayList<>();

            List<ConversationParticipant> participants = new ArrayList<>();
            participants.add(sender);
            participants.add(receiver);

            Optional<Conversation> conversation = findByParticipants(participants);

            if (conversation.isPresent()) {
                messages = messageRepository.findByConversationOrderByDateTimeAsc(conversation.get());
            }

            return messages;
        }
        return Collections.emptyList();
    }

    @Override
    public ConversationParticipant createParticipant(Account account) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setAccount(account);

        return conversationPartRepository.save(participant);
    }

    @Override
    @Transactional
    public Conversation findOrCreateConversation(ConversationParticipant sender, ConversationParticipant receiver) {
        if (haveNoConversation(sender, receiver)) {
            return conversationServiceImp.createConversationAndUpdateParticipants(sender, receiver);
        }

        Optional<Conversation> senderConversation = Optional.ofNullable(sender.getConversation());
        Optional<Conversation> receiverConversation = Optional.ofNullable(receiver.getConversation());

        if (senderConversation.equals(receiverConversation)) {
            return senderConversation.orElse(null);
        }

        return conversationServiceImp.createConversationAndUpdateParticipants(
                createParticipant(sender.getAccount()),
                createParticipant(receiver.getAccount())
        );
    }

    private boolean haveNoConversation(ConversationParticipant sender, ConversationParticipant receiver) {
        return sender.getConversation() == null && receiver.getConversation() == null;
    }

    @Override
    public List<Account> getDialogUsernames(String currentUser) {
        List<ConversationParticipant> currentUserParticipants = conversationPartRepository.findAllByAccountUsername(currentUser);

        List<Account> dialogUsernames = new ArrayList<>();

        for (ConversationParticipant participant : currentUserParticipants) {
            List<ConversationParticipant> participants = new ArrayList<>();
            participants.add(participant);

            Optional<Conversation> conversation = findByParticipants(participants);
            conversation.ifPresent(value -> {
                for (ConversationParticipant otherParticipant : value.getParticipants()) {
                    if (!otherParticipant.getAccount().getUsername().equals(currentUser)) {
                        try {
                            dialogUsernames.add(otherParticipant.getAccount());
                        } catch (Exception e) {
                            logger.error("Error processing participant account: {}", e.getMessage());
                        }
                    }
                }
            });
        }

        return dialogUsernames;
    }

    @Override
    public Optional<Account> findAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public List<MessageView> processMessages(List<Message> messages) {
        List<MessageView> messagesWithInfo = new ArrayList<>();
        Account previousSender = null;

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            MessageView messageInfo = new MessageView(message);

            if (message.getSender().equals(getAuthenticatedAccount()))
                messageInfo.setMyAccount(true);

            if (i < messages.size() - 1) {
                Message nextMessage = messages.get(i + 1);

                if (!message.getSender().equals(nextMessage.getSender())) {
                    messageInfo.setPicture(true);
                }
            } else {
                messageInfo.setPicture(true);
            }

            if (previousSender == null || !previousSender.equals(message.getSender())) {
                messageInfo.setDisplayProfile(true);
                previousSender = message.getSender();
            }
            messagesWithInfo.add(messageInfo);
        }

        return messagesWithInfo;
    }
}