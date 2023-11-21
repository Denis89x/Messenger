package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.dto.MessageDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.model.MessageView;
import by.lebenkov.messenger.service.AccountService;
import by.lebenkov.messenger.service.MessengerServiceImp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/messenger")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MessengerController {

    MessengerServiceImp messengerServiceImp;
    AccountService accountService;

    @GetMapping
    public String showMenu(Model model) {
        messengerServiceImp.getAccount(model);

        List<Account> dialogUsernames = messengerServiceImp.getDialogUsernames(messengerServiceImp.getAuthenticatedAccount().getUsername());

        model.addAttribute("currentUser", messengerServiceImp.getAuthenticatedAccount().getUsername());
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

    @MessageMapping("/send-message/{receiver}")
    @SendTo("/topic/chatroom")
    public MessageDTO send(@DestinationVariable String receiver, @Payload String messageContent, Principal principal) {
        messengerServiceImp.sendMessage(principal.getName(), receiver, messageContent);
        return new MessageDTO(principal.getName(), receiver, messageContent);
    }

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