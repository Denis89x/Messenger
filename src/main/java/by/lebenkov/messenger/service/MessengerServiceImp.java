package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.*;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import by.lebenkov.messenger.repository.ConversationRepository;
import by.lebenkov.messenger.repository.MessageRepository;
import by.lebenkov.messenger.util.ConversationNotFoundedException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public Message commitMessageOrFindParticipants(String senderUsername, String receiverUsername, String content, String type) {
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
            return processParticipants(senderM, receiverM, content, type);
        }

        if (sender != null && receiver != null) {
            return processIndividualParticipants(sender, receiver, content, type);
        }

        return null;
    }

    @Transactional
    public Message processParticipants(Optional<Account> senderM, Optional<Account> receiverM, String content, String type) {
        List<ConversationParticipant> senders = conversationPartRepository.findAllByAccount(senderM.get());
        List<ConversationParticipant> receivers = conversationPartRepository.findAllByAccount(receiverM.get());
        ConversationParticipant senderParticipant = senders.isEmpty() ? createParticipant(senderM.get()) : senders.get(0);
        ConversationParticipant receiverParticipant = receivers.isEmpty() ? createParticipant(receiverM.get()) : receivers.get(0);

        if (type.equals("commit")) {
            return commitMessage(senderParticipant, receiverParticipant, content);
        } else {
            findOrCreateConversation(senderParticipant, receiverParticipant);
        }
        return null;
    }

    @Transactional
    public Message processIndividualParticipants(ConversationParticipant sender, ConversationParticipant receiver, String content, String type) {
        if (type.equals("commit")) {
            return commitMessage(sender, receiver, content);
        } else {
            findOrCreateConversation(sender, receiver);
        }
        return null;
    }

    @Override
    public Optional<Conversation> findByParticipants(List<ConversationParticipant> participants) {
        return conversationRepository.findByParticipantsIn(participants);
    }


    @Transactional
    public Message commitMessage(ConversationParticipant sender, ConversationParticipant receiver, String content) {
        Conversation conversation = findOrCreateConversation(sender, receiver);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender.getAccount());
        message.setContent(content);
        message.setDateTime(LocalDateTime.now());
        message.setIsChecked(false);

        return messageRepository.save(message);
    }

    @Override
    public List<Message> getConversationMessages(String senderUsername, String receiverUsername) {
        return messageRepository.findByConversationOrderByDateTimeAsc(
                findConversationByParticipants(senderUsername, receiverUsername).orElseThrow(() ->
                        new ConversationNotFoundedException("Conversation not founded!")));
    }

    public String getLastMessage(String senderUsername, String receiverUsername) {
        return findConversationByParticipants(senderUsername, receiverUsername)
                .flatMap(conversation -> Optional.ofNullable(messageRepository.findFirstByConversationOrderByDateTimeDesc(conversation)))
                .map(this::checkMessage)
                .orElse("null");
    }

    private String checkMessage(Message message) {
        if (message.getSender().getIdAccount() == getAuthenticatedAccount().getIdAccount()) {
            if (message.getContent().length() > 12)
                return "You: " + message.getContent().substring(0, 12) + "...";
            return "You: " + message.getContent();
        } else {
            if (message.getContent().length() > 12)
                return message.getContent().substring(0, 12) + "...";
            return message.getContent();
        }
    }

    public List<String> lastMessages(List<Account> dialogUsers) {
        List<String> receiversUsername = new ArrayList<>();

        for (Account acc : dialogUsers) {
            receiversUsername.add(acc.getUsername());
        }

        List<String> lastMessages = new ArrayList<>();

        for (String name : receiversUsername) {
            lastMessages.add(getLastMessage(
                    getAuthenticatedAccount().getUsername(),
                    name));
        }

        return lastMessages;
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

    private Optional<Conversation> findConversationByParticipants(String senderUsername, String receiverUsername) {
        return findByParticipants(findParticipants(senderUsername, receiverUsername));
    }

    private List<ConversationParticipant> findParticipants(String senderUsername, String receiverUsername) {
        List<ConversationParticipant> commonParticipants = commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername);

        return commonParticipants.size() == 2 ? commonParticipants.subList(0, 2) : Collections.emptyList();
    }

    @Transactional
    public void clearHistory(String sender, String receiver) {
        if (sender != null && receiver != null)
            messageRepository.deleteAllByConversation(findConversationByParticipants(sender, receiver).orElseThrow(() ->
                    new ConversationNotFoundedException("Conversation not founded!")));
    }

    @Transactional
    public void deleteConversation(String sender, String receiver) {
        if (sender != null && receiver != null) {
            clearHistory(sender, receiver);

            Conversation conversation = findConversationByParticipants(sender, receiver)
                    .orElseThrow(() -> new ConversationNotFoundedException("Conversation not founded!"));

            conversationPartRepository.deleteAllByConversation(conversation);

            conversationRepository.deleteById(conversation.getId());
        }
    }

    public void deleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }
}