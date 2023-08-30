package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Message;
import by.lebenkov.messenger.service.MessengerServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/messenger")
public class MessengerController {

    private final MessengerServiceImp messengerServiceImp;

    @Autowired
    public MessengerController(MessengerServiceImp messengerService) {
        this.messengerServiceImp = messengerService;
    }

    @GetMapping("")
    public String showMenu(Model model) {
        messengerServiceImp.getAccount(model);

        String currentUser = messengerServiceImp.getCurrentUserUsername();
        List<Account> dialogUsernames = messengerServiceImp.getDialogUsernames(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("dialogUsernames", dialogUsernames);

        return "messenger/messages";
    }

    @GetMapping("/{receiverUsername}")
    public String showMessageWithReceiver(@PathVariable String receiverUsername, Model model) {
        String currentUser = messengerServiceImp.getCurrentUserUsername();

        List<Message> messages = messengerServiceImp.getConversationMessages(currentUser, receiverUsername);

        messengerServiceImp.getAccount(model);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("receiver", receiverUsername);
        model.addAttribute("messages", messages);

        return "messenger/chat";
    }

    @PostMapping("/{receiverUsername}")
    public String sendMessageToReceiver(@PathVariable String receiverUsername,
                                        @RequestParam("messageContent") String messageContent) {
        String senderUsername = messengerServiceImp.getCurrentUserUsername();;

        messengerServiceImp.sendMessage(senderUsername, receiverUsername, messageContent);

        return "redirect:/messenger/{receiverUsername}";
    }

    @PostMapping("/start-dialog")
    public String startNewDialog(@RequestParam("receiverUsername") String receiverUsername) {
        String senderUsername = messengerServiceImp.getCurrentUserUsername();

        Optional<Account> receiverAccount = messengerServiceImp.findAccountByUsername(receiverUsername);
        if (receiverAccount.isEmpty()) {
            System.err.println("получателя нет");
            // Handle case when receiver account doesn't exist
            return "redirect:/messenger";
        }

        messengerServiceImp.sendMessage(senderUsername, receiverUsername, "New dialog started");

        return "redirect:/messenger/" + receiverUsername;
    }
}
