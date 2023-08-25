package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.service.MessengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/messenger")
public class MessengerController {

    private final MessengerService messengerService;

    @Autowired
    public MessengerController(MessengerService messengerService) {
        this.messengerService = messengerService;
    }

    @GetMapping("")
    public String showMenu(Model model) {
        messengerService.getAccount(model);

        return "messenger/main";
    }
}
