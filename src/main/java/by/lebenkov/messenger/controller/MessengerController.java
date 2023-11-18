package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.model.MessageView;
import by.lebenkov.messenger.service.AccountService;
import by.lebenkov.messenger.service.MessengerServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/messenger")
public class MessengerController {

    private final MessengerServiceImp messengerServiceImp;
    private final AccountService accountService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessengerController(MessengerServiceImp messengerService, AccountService accountService, SimpMessagingTemplate messagingTemplate) {
        this.messengerServiceImp = messengerService;
        this.accountService = accountService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("")
    public String showMenu(Model model) {
        messengerServiceImp.getAccount(model);

        Account currentUser = messengerServiceImp.getAuthenticatedAccount();
        List<Account> dialogUsernames = messengerServiceImp.getDialogUsernames(currentUser.getUsername());

        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("dialogUsernames", dialogUsernames);

        return "messenger/main";
    }

    @GetMapping("/{receiverUsername}")
    public String showMessageWithReceiver(@PathVariable String receiverUsername, Model model) {
        Account currentUser = messengerServiceImp.getAuthenticatedAccount();

        List<Message> messages = messengerServiceImp.getConversationMessages(currentUser.getUsername(), receiverUsername);
        List<MessageView> list = messengerServiceImp.processMessages(messages);

        List<Account> dialogUsernames = messengerServiceImp.getDialogUsernames(currentUser.getUsername());

        Optional<Account> receiverOptional = accountService.findByUsername(receiverUsername);

        Account receiver = receiverOptional.get();
        messengerServiceImp.getAccount(model);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("receiver", receiver);
        model.addAttribute("messages", list);
        model.addAttribute("dialogUsernames", dialogUsernames);

        return "messenger/main";
    }

/*    @MessageMapping("/{receiverUsername}")
    @SendTo("/chat/{receiverUsername}")
    public Message sendMessageToReceiver(@DestinationVariable String receiverUsername, @Payload String messageContent, Principal principal) {
        System.out.println("1");
*//*        String senderUsername = messengerServiceImp.getAuthenticatedAccount().getUsername();
        String senderUsername = messengerServiceImp.getCurrentUserUsername();
        String senderUsername = messengerServiceImp.findT();*//*
        SecurityContextHolder.getContext().setAuthentication((Authentication) principal);
        String senderUsername = messengerServiceImp.getCurrentUserUsername();
        System.out.println("2");
        return messengerServiceImp.sendMessage(senderUsername, receiverUsername, messageContent);
    }*/

    @MessageMapping("/sendMessage/{receiverUsername}")
    @SendTo("/chat/{receiverUsername}")
    public Message send(@DestinationVariable String receiver, @Payload String messageContent) {
        String sender = messengerServiceImp.getAuthenticatedAccount().getUsername();
        return messengerServiceImp.sendMessage(sender, receiver, messageContent);
    }

/*        @MessageMapping("/{receiverUsername}")
        public void sendMessageToReceiver(@DestinationVariable String receiverUsername, @Payload String messageContent, Principal principal) {
            String senderUsername = principal.getName();
            Message message = messengerServiceImp.sendMessage(senderUsername, receiverUsername, messageContent);
            messagingTemplate.convertAndSend("/chat/" + receiverUsername, message);
        }*/


    @PostMapping("/start-dialog")
    public String startNewDialog(@RequestParam("receiverUsername") String receiverUsername) {
        Account senderUsername = messengerServiceImp.getAuthenticatedAccount();

        Optional<Account> receiverAccount = messengerServiceImp.findAccountByUsername(receiverUsername);
        if (receiverAccount.isEmpty()) {
            return "redirect:/messenger";
        }

        messengerServiceImp.findParticipants(senderUsername.getUsername(), receiverUsername);

        return "redirect:/messenger/" + receiverUsername;
    }
}
