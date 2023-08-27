package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.service.AccountServiceImp;
import by.lebenkov.messenger.service.MessengerService;
import by.lebenkov.messenger.service.PictureServiceImp;
import com.backblaze.b2.client.exceptions.B2Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final MessengerService messengerService;
    private final PictureServiceImp pictureServiceImp;
    private final AccountServiceImp accountServiceImp;

    @Autowired
    public ProfileController(MessengerService messengerService, PictureServiceImp pictureServiceImp, AccountServiceImp accountServiceImp) {
        this.messengerService = messengerService;
        this.pictureServiceImp = pictureServiceImp;
        this.accountServiceImp = accountServiceImp;
    }

    @GetMapping()
    public String showSettings(Model model) {
        messengerService.getAccount(model);

        return "profile/user";
    }

    @PostMapping("/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, Model model) throws B2Exception {
        messengerService.getAccount(model);

        if (file.isEmpty()) {
            model.addAttribute("uploadError", "Please select a file to upload.");
            messengerService.getAccount(model);
            return "profile/user";
        }

        Account account = messengerService.getAuthenticatedAccount();
        if (account != null) {
            account.setProfilePicture(pictureServiceImp.uploadProfilePicture(file));
            accountServiceImp.save(account);
        } else {
            model.addAttribute("accountError", "Account was not found");
        }

        return "redirect:/profile";
    }
}
