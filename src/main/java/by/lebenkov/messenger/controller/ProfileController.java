package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.dto.ChangeEmailDTO;
import by.lebenkov.messenger.dto.ChangePasswordDTO;
import by.lebenkov.messenger.dto.VerificationCodeDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.service.*;
import by.lebenkov.messenger.util.ChangeEmailValidator;
import by.lebenkov.messenger.util.ChangePasswordValidator;
import by.lebenkov.messenger.util.ChangeUsernameValidator;
import com.backblaze.b2.client.exceptions.B2Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import by.lebenkov.messenger.dto.ChangeUsernameDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final MessengerServiceImp messengerService;
    private final LinkServiceImp linkServiceImp;
    private final ChangeUsernameValidator changeUsernameValidator;
    private final ChangePasswordValidator changePasswordValidator;
    private final ChangeEmailValidator changeEmailValidator;
    private final EmailServiceImp emailServiceImp;
    private final PasswordEncoder passwordEncoder;
    private final AccountServiceImp accountServiceImp;
    private final Map<Integer, VerificationCodeDTO> verificationCodesMap = new HashMap<>();

    @Autowired
    public ProfileController(MessengerServiceImp messengerService, LinkServiceImp linkServiceImp, ChangeUsernameValidator changeUsernameValidator, ChangePasswordValidator changePasswordValidator, ChangeEmailValidator changeEmailValidator, EmailServiceImp emailServiceImp, PasswordEncoder passwordEncoder, AccountServiceImp accountServiceImp) {
        this.messengerService = messengerService;
        this.linkServiceImp = linkServiceImp;
        this.changeUsernameValidator = changeUsernameValidator;
        this.changePasswordValidator = changePasswordValidator;
        this.changeEmailValidator = changeEmailValidator;
        this.emailServiceImp = emailServiceImp;
        this.passwordEncoder = passwordEncoder;
        this.accountServiceImp = accountServiceImp;
    }

    @GetMapping()
    public String showSettings(Model model) {
        messengerService.getAccount(model);

        Account account = messengerService.getAuthenticatedAccount();

        String email = account.getEmail();
        String maskedEmail = emailServiceImp.getEmailMasked(email);
        model.addAttribute("maskedEmail", maskedEmail);

        return "profile/user";
    }

    @PostMapping("/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, Model model) {
        messengerService.getAccount(model);

        if (file.isEmpty()) {
            model.addAttribute("uploadError", "Please select a file to upload.");
            messengerService.getAccount(model);
            return "profile/user";
        }

        Account account = messengerService.getAuthenticatedAccount();
        if (account != null) {
            account.setProfilePicture(linkServiceImp.uploadProfilePicture(file));
            accountServiceImp.save(account);
        } else {
            model.addAttribute("accountError", "Account was not found");
        }

        return "redirect:/profile";
    }

    @PostMapping("/change-username")
    public String changeUsername(@Valid ChangeUsernameDTO changeUsernameDTO, BindingResult bindingResult, Model model) {
        Account account = messengerService.getAuthenticatedAccount();

        if (account != null) {
            changeUsernameValidator.validate(changeUsernameDTO, bindingResult);

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
                model.addAttribute("usernameError", "Ошибка при изменении логина. Попробуйте еще раз");
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

    @PostMapping("/change-password")
    public String changePassword(@Valid ChangePasswordDTO changePasswordDTO, BindingResult bindingResult, Model model) {
        Account account = messengerService.getAuthenticatedAccount();
        messengerService.getAccount(model);
        if (account != null) {
            changePasswordValidator.validate(changePasswordDTO, bindingResult);

            if (bindingResult.hasErrors()) {
                messengerService.getAccount(model);
                model.addAttribute("passwordError", "Неверный формат нового пароля");
                return "profile/user";
            }

            if (passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), account.getPassword())) {
                account.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
                accountServiceImp.save(account);

                model.addAttribute("successMessage", "Пароль успешно обновлен");
            } else {
                model.addAttribute("passwordError", "Неверный текущий пароль");
            }
        } else {
            model.addAttribute("passwordError", "Пользователь не найден");
        }
        return "profile/user";
    }

    @PostMapping("/change-email")
    public String changeEmail(@Valid ChangeEmailDTO changeEmailDTO, BindingResult bindingResult, Model model) {
        Account account = messengerService.getAuthenticatedAccount();
        messengerService.getAccount(model);

        if (account != null) {
            changeEmailValidator.validate(changeEmailDTO, bindingResult);

            if (bindingResult.hasErrors()) {
                messengerService.getAccount(model);
                model.addAttribute("emailError", "This e-mail is already taken");
                return "profile/user";
            }

            account.setEmail(changeEmailDTO.getNewEmail());

            try {
                accountServiceImp.save(account);
            } catch (Exception e) {
                model.addAttribute("emailError", "Error when changing mail. Try again.");
                messengerService.getAccount(model);
                return "profile/user";
            }

            return "redirect:/profile";
        }
        model.addAttribute("emailError", "E-mail not found");
        messengerService.getAccount(model);
        return "profile/user";
    }

    @PostMapping("/send-verification-code")
    public String sendVerificationCode(RedirectAttributes redirectAttributes) {
        Account account = messengerService.getAuthenticatedAccount();

        if (account != null) {
            String verificationCode = emailServiceImp.generateVerificationCode();

            VerificationCodeDTO verificationCodeDTO = new VerificationCodeDTO();
            verificationCodeDTO.setUserId(account.getIdAccount());
            verificationCodeDTO.setVerificationCode(verificationCode);

            verificationCodesMap.put(account.getIdAccount(), verificationCodeDTO);

            emailServiceImp.sendVerificationCode(account.getEmail(), verificationCode);

            redirectAttributes.addFlashAttribute("successSend", "Verification code has been sent to your email.");
        } else {
            redirectAttributes.addFlashAttribute("emailError", "User not found");
        }

        return "redirect:/profile";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String verificationCode, RedirectAttributes redirectAttributes) {
        Account account = messengerService.getAuthenticatedAccount();

        if (account != null) {
            VerificationCodeDTO verificationCodeDTO = verificationCodesMap.get(account.getIdAccount());

            if (verificationCodeDTO != null) {
                if (verificationCode.equals(verificationCodeDTO.getVerificationCode())) {
                    account.setIsVerifiedEmail(true);
                    accountServiceImp.save(account);

                    redirectAttributes.addFlashAttribute("successVerify", "Your email has been verified successfully.");

                    verificationCodesMap.remove(account.getIdAccount());
                } else {
                    redirectAttributes.addFlashAttribute("InvalidCode", "Invalid verification code");
                }
            } else {
                redirectAttributes.addFlashAttribute("verificationError", "Verification code not found");
            }
        } else {
            redirectAttributes.addFlashAttribute("emailError", "User not found");
        }

        return "redirect:/profile";
    }

}
