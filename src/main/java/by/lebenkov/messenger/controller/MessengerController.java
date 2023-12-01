package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.dto.MessageDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.model.MessageView;
import by.lebenkov.messenger.service.AccountServiceImp;
import by.lebenkov.messenger.service.MessengerServiceImp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
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
    AccountServiceImp accountServiceImp;

    private static final String MESSENGER_ENDPOINT = "/{receiverUsername}";
    private static final String START_DIALOG = "/start-dialog";

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

    @MessageMapping("/send-message/{receiver}")
    @SendTo("/topic/chatroom")
    public MessageDTO send(@DestinationVariable String receiver, @Payload String messageContent, Principal principal) {
        messengerServiceImp.commitMessageOrFindParticipants(principal.getName(), receiver, messageContent, "commit");
        return new MessageDTO(principal.getName(), receiver, messageContent);
    }


    @PostMapping("/clear-history/{receiver}")
    public String clearDialogHistory(@PathVariable String receiver) {
        messengerServiceImp.clearHistory(
                messengerServiceImp.getAuthenticatedAccount().getUsername(),
                receiver);
        return "redirect:/messenger/second";
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