package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.dto.MessageDTO;
import by.lebenkov.messenger.dto.NotificationDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.model.MessageView;
import by.lebenkov.messenger.service.AccountServiceImp;
import by.lebenkov.messenger.service.MessengerServiceImp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/messenger")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MessengerController {

    MessengerServiceImp messengerServiceImp;
    AccountServiceImp accountServiceImp;
    SimpMessagingTemplate messagingTemplate;

    private static final String MESSENGER_ENDPOINT = "/{receiverUsername}";
    private static final String START_DIALOG = "/start-dialog";
    private static final String CLEAR_HISTORY = "/clear-history/{receiver}";

    @GetMapping
    public String showMenu(Model model) {
        messengerServiceImp.getAccount(model);

        List<Account> dialogUsers = messengerServiceImp.getDialogUsernames(messengerServiceImp.getAuthenticatedAccount().getUsername());

        model.addAttribute("currentUser", messengerServiceImp.getAuthenticatedAccount().getUsername());
        model.addAttribute("dialogUsernames", dialogUsers);
        model.addAttribute("lastMessage", messengerServiceImp.lastMessages(dialogUsers));

        return "messenger/main";
    }

    @GetMapping(MESSENGER_ENDPOINT)
    public String showMessageWithReceiver(@PathVariable String receiverUsername, Model model) {
        Account currentUser = messengerServiceImp.getAuthenticatedAccount();

        List<Message> messages = messengerServiceImp.getConversationMessages(currentUser.getUsername(), receiverUsername);
        List<MessageView> list = messengerServiceImp.processMessages(messages);

        List<Account> dialogUsers = messengerServiceImp.getDialogUsernames(currentUser.getUsername());

        Optional<Account> receiverOptional = accountServiceImp.findByUsername(receiverUsername);

        Account receiver = receiverOptional.get();
        messengerServiceImp.getAccount(model);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("receiver", receiver);
        model.addAttribute("messages", list);
        model.addAttribute("dialogUsernames", dialogUsers);
        model.addAttribute("lastMessage", messengerServiceImp.lastMessages(dialogUsers));

        return "messenger/main";
    }

    @MessageMapping("/send-message/{chatroom}/{receiver}")
    @SendTo("/topic/chatroom/{chatroom}")
    public MessageDTO send(@DestinationVariable String receiver, @Payload String messageContent, Principal principal) {
        Message message = messengerServiceImp.commitMessageOrFindParticipants(principal.getName(), receiver, messageContent, "commit");

        sendNotification(receiver, principal.getName(), messageContent);

        return new MessageDTO(message.getId(), principal.getName(), receiver, messageContent);
    }

    private void sendNotification(String receiver, String sender, String content) {
        log.info("In the notification method");
        String destination = "/topic/notifications/" + receiver;
        messagingTemplate.convertAndSend(destination, new NotificationDTO(sender, content));
    }

    @MessageMapping("/delete-message/{chatroom}/{messageId}")
    public void deleteMessage(@Payload Integer messageId) {
        messengerServiceImp.deleteMessage(messageId);
        messagingTemplate.convertAndSend("/topic/chatroom/{chatroom}", "MessageDeleted:" + messageId);
    }

    @PostMapping(CLEAR_HISTORY)
    public String clearDialogHistory(@PathVariable String receiver) {
        messengerServiceImp.clearHistory(
                messengerServiceImp.getAuthenticatedAccount().getUsername(),
                receiver);
        return "redirect:/messenger/" + receiver;
    }

    @PostMapping("/delete-conversation/{receiver}")
    public String deleteConversation(@PathVariable String receiver) {
        messengerServiceImp.deleteConversation(
                messengerServiceImp.getAuthenticatedAccount().getUsername(),
                receiver);
        return "redirect:/messenger";
    }

    @PostMapping(START_DIALOG)
    public String startNewDialog(@RequestParam("receiverUsername") String receiverUsername) {
        Account senderUsername = messengerServiceImp.getAuthenticatedAccount();

        Optional<Account> receiverAccount = messengerServiceImp.findAccountByUsername(receiverUsername);
        if (receiverAccount.isEmpty()) {
            return "redirect:/messenger";
        }

        messengerServiceImp.commitMessageOrFindParticipants(senderUsername.getUsername(), receiverUsername, null, "find");

        return "redirect:/messenger/" + receiverUsername;
    }
}