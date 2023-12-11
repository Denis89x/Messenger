package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.service.MessengerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/messenger/settings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SettingsController {

    MessengerService messengerService;

    @GetMapping
    public String showSettings(Model model) {
        messengerService.getAccount(model);

        return "profile/settings";
    }
}
