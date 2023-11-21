package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.repository.ConversationPartRepository;
import by.lebenkov.messenger.repository.ConversationRepository;
import by.lebenkov.messenger.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import by.lebenkov.messenger.model.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessengerServiceImp implements MessengerService {

    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationPartRepository conversationPartRepository;
    private final ConversationServiceImp conversationServiceImp;
    private final CommonParticipantsServiceImp commonParticipantsServiceImp;

    @Autowired
    public MessengerServiceImp(AccountRepository accountRepository, MessageRepository messageRepository, ConversationRepository conversationRepository, ConversationPartRepository conversationPartRepository, ConversationServiceImp conversationServiceImp, CommonParticipantsServiceImp commonParticipantsServiceImp) {
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationPartRepository = conversationPartRepository;
        this.conversationServiceImp = conversationServiceImp;
        this.commonParticipantsServiceImp = commonParticipantsServiceImp;
    }

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

    public String findT() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Account> account = accountRepository.findByUsername(username);
        return account.get().getUsername();
    }

    /*@Override
    public void sendMessage(String senderUsername, String receiverUsername, String content) {
        List<ConversationParticipant> senderParticipants = conversationPartRepository.findAllByAccountUsername(senderUsername);
        List<ConversationParticipant> receiverParticipants = conversationPartRepository.findAllByAccountUsername(receiverUsername);
        System.out.println("senderParticipants111 = " + senderParticipants);
        System.out.println("receiverParticipants111 = " + receiverParticipants);

        List<ConversationParticipant> commonParticipants = commonParticipantsServiceImp.findCommonParticipants(senderUsername, receiverUsername);

        System.out.println("@@@@@@@@@@@@@@@@" + commonParticipants);

        ConversationParticipant sender = null;
        ConversationParticipant receiver = null;

        if (!commonParticipants.isEmpty()) {
            sender = commonParticipants.get(0);
            receiver = commonParticipants.get(1);
        }

        Optional<Account> senderM = accountRepository.findByUsername(senderUsername);
        System.out.println("sender = " + sender);
        Optional<Account> receiverM = accountRepository.findByUsername(receiverUsername);
        System.out.println("receiver = " + receiver);

        if (senderM.isPresent() && receiverM.isPresent() && (sender == null && receiver == null)) {
            System.out.println("я тут");
            List<ConversationParticipant> senders = conversationPartRepository.findAllByAccount(senderM.get());
            System.out.println("1");
            List<ConversationParticipant> receivers = conversationPartRepository.findAllByAccount(receiverM.get());
            System.out.println("2");
            ConversationParticipant senderParticipant;
            System.out.println("3");
            if (senders.isEmpty()) {
                System.out.println("4");
                senderParticipant = createParticipant(senderM.get());
                System.out.println("5");
            } else {
                System.out.println("6");
                senderParticipant = senders.get(0);
                System.out.println("7");
            }
            System.out.println("8");
            ConversationParticipant receiverParticipant;
            System.out.println("9");
            if (receivers.isEmpty()) {
                System.out.println("10");
                receiverParticipant = createParticipant(receiverM.get());
                System.out.println("11");
            } else {
                System.out.println("12");
                receiverParticipant = receivers.get(0);
                System.out.println("13");
            }
            System.out.println("14");
            commitMessage(senderParticipant, receiverParticipant, content);
        }

        if (sender != null && receiver != null) {
            System.out.println("я тута");
            commitMessage(sender, receiver, content);

        } else {
            System.out.println("не найден 222");
        }
    }*/

    @Transactional
    public void sendMessage(String senderUsername, String receiverUsername, String content) {
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
            List<ConversationParticipant> senders = conversationPartRepository.findAllByAccount(senderM.get());
            List<ConversationParticipant> receivers = conversationPartRepository.findAllByAccount(receiverM.get());
            ConversationParticipant senderParticipant;
            if (senders.isEmpty()) {
                senderParticipant = createParticipant(senderM.get());
            } else {
                senderParticipant = senders.get(0);
            }
            ConversationParticipant receiverParticipant;
            if (receivers.isEmpty()) {
                receiverParticipant = createParticipant(receiverM.get());
            } else {
                receiverParticipant = receivers.get(0);
            }
            commitMessage(senderParticipant, receiverParticipant, content);
        }

        if (sender != null && receiver != null) {
            commitMessage(sender, receiver, content);
        }
    }

    public void findParticipants(String senderUsername, String receiverUsername) {
        List<ConversationParticipant> senderParticipants = conversationPartRepository.findAllByAccountUsername(senderUsername);
        List<ConversationParticipant> receiverParticipants = conversationPartRepository.findAllByAccountUsername(receiverUsername);

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
            List<ConversationParticipant> senders = conversationPartRepository.findAllByAccount(senderM.get());
            List<ConversationParticipant> receivers = conversationPartRepository.findAllByAccount(receiverM.get());
            ConversationParticipant senderParticipant;
            if (senders.isEmpty()) {
                senderParticipant = createParticipant(senderM.get());
            } else {
                senderParticipant = senders.get(0);
            }
            ConversationParticipant receiverParticipant;
            if (receivers.isEmpty()) {
                receiverParticipant = createParticipant(receiverM.get());
            } else {
                receiverParticipant = receivers.get(0);
            }
/*            commitMessage(senderParticipant, receiverParticipant, content);*/
            Conversation conversation = findOrCreateConversation(senderParticipant, receiverParticipant);
        }

        if (sender != null && receiver != null) {
/*            commitMessage(sender, receiver, content);*/
            Conversation conversation = findOrCreateConversation(sender, receiver);
        }
    }

    @Override
    public Optional<Conversation> findByParticipants(List<ConversationParticipant> participants) {
        return conversationRepository.findByParticipantsIn(participants);
    }

    @Transactional
    public void createConversation(ConversationParticipant sender, ConversationParticipant receiver) {
        Conversation conversation = findOrCreateConversation(sender, receiver);

    }

/*    @Override
    @Transactional
    public void commitMessage(ConversationParticipant sender, ConversationParticipant receiver, String content) {
        Conversation conversation = findOrCreateConversation(sender, receiver);
        System.out.println("conversation1 " + conversation);

        System.out.println("Content: " + content);
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender.getAccount());
        message.setContent(content);
        message.setDateTime(LocalDateTime.now());
        message.setIsChecked(false);

        System.out.println("перед message.save");
        messageRepository.save(message);
        System.out.println("после message.save");
    }*/


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

/*        return message;*/
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
        if (sender.getConversation() == null && receiver.getConversation() == null) {
            return conversationServiceImp.createConversationAndUpdateParticipants(sender, receiver);
        }

        if (sender.getConversation() == null) {
            ConversationParticipant newReceiver = createParticipant(receiver.getAccount());
            return conversationServiceImp.createConversationAndUpdateParticipants(sender, newReceiver);
        }

        if (receiver.getConversation() == null) {
            ConversationParticipant newSender = createParticipant(sender.getAccount());
            return conversationServiceImp.createConversationAndUpdateParticipants(newSender, receiver);
        }

        if (sender.getConversation().getId().equals(receiver.getConversation().getId())) {
            return sender.getConversation();
        }

        if (!Objects.equals(sender.getConversation().getId(), receiver.getConversation().getId())) {
            ConversationParticipant newSender = createParticipant(sender.getAccount());
            ConversationParticipant newReceiver = createParticipant(receiver.getAccount());
            return conversationServiceImp.createConversationAndUpdateParticipants(newSender, newReceiver);
        }
        return null;
    }

    @Override
    public String getCurrentUserUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public List<String> getOtherUsernames() {
        // Получение текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserUsername = authentication.getName();

        // Извлечение всех пользователей, кроме текущего
        List<Account> allAccountsExceptCurrent = accountRepository.findAllByUsernameNot(currentUserUsername);

        // Формирование списка имен других пользователей
        List<String> otherUsernames = new ArrayList<>();
        for (Account account : allAccountsExceptCurrent) {
            otherUsernames.add(account.getUsername());
        }

        return otherUsernames;
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
                            e.printStackTrace();
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