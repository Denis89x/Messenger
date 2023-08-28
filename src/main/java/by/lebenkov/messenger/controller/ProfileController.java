package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.service.AccountServiceImp;
import by.lebenkov.messenger.service.MessengerService;
import by.lebenkov.messenger.service.PictureServiceImp;
import by.lebenkov.messenger.util.ChangeUsernameValidator;
import com.backblaze.b2.client.exceptions.B2Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import by.lebenkov.messenger.dto.ChangeUsernameDTO;

import javax.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final MessengerService messengerService;
    private final PictureServiceImp pictureServiceImp;
    private final ChangeUsernameValidator changeUsernameValidator;
    private final AccountServiceImp accountServiceImp;

    @Autowired
    public ProfileController(MessengerService messengerService, PictureServiceImp pictureServiceImp, ChangeUsernameValidator changeUsernameValidator, AccountServiceImp accountServiceImp) {
        this.messengerService = messengerService;
        this.pictureServiceImp = pictureServiceImp;
        this.changeUsernameValidator = changeUsernameValidator;
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

    /*@PostMapping("/change-username")
    public String changeUsername(@RequestParam String newUsername, Model model) {
        Account account = messengerService.getAuthenticatedAccount();

        if (account != null) {
            if (!newUsername.matches("(?=.*[a-zA-Z])[a-zA-Z0-9_]+")) {
                System.out.println("Не прошёл по требованиям");
                model.addAttribute("usernameError", "Логин должен содержать только латинские буквы, цифры и символ '_'");
                messengerService.getAccount(model);
                return "profile/user";
            }
            if (accountServiceImp.findByUsername(newUsername).isPresent()) {
                model.addAttribute("usernameError", "Этот логин уже используется");
                messengerService.getAccount(model);
                return "profile/user";
            }
            account.setUsername(newUsername);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(
                    account.getUsername(), authentication.getCredentials(), authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);

            try {
                accountServiceImp.save(account);
            } catch (Exception e) {
                model.addAttribute("usernameError", "Ошибка при изменении логина. Попробуйте еще раз.");
                messengerService.getAccount(model);
                return "profile/user";
            }

            return "redirect:/profile";
        }
        model.addAttribute("usernameError", "Пользователь не найден");
        messengerService.getAccount(model);
        return "profile/user";
    }*/

    @PostMapping("/change-username")
    public String changeUsername(@Valid ChangeUsernameDTO changeUsernameDTO, BindingResult bindingResult, Model model) {
        Account account = messengerService.getAuthenticatedAccount();

        changeUsernameValidator.validate(changeUsernameDTO, bindingResult);
        model.addAttribute("changeUsernameDTO", new ChangeUsernameDTO());
        if (account != null) {
            if (bindingResult.hasErrors()) {
                messengerService.getAccount(model);
                model.addAttribute("usernameError", "Логин уже занят или некорректно введён");
                return "profile/user";
            }

            account.setUsername(changeUsernameDTO.getNewUsername());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(
                    account.getUsername(), authentication.getCredentials(), authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);

            try {
                accountServiceImp.save(account);
            } catch (Exception e) {
                model.addAttribute("usernameError", "Ошибка при изменении логина. Попробуйте еще раз.");
                messengerService.getAccount(model);
                return "profile/user";
            }

            return "redirect:/profile";
        } else {
            model.addAttribute("usernameError", "Пользователь не найден");
            messengerService.getAccount(model);
            return "profile/user";
        }
    }

}
