package by.lebenkov.messenger.controller;

import by.lebenkov.messenger.dto.AccountDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.service.CaptchaService;
import by.lebenkov.messenger.service.RegistrationService;
import by.lebenkov.messenger.util.AccountValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    RegistrationService registrationService;
    AccountValidator accountValidator;
    CaptchaService captchaService;

    private static final String PERFORM_LOGIN = "/login";
    private static final String PERFORM_REGISTRATION = "/registration";

    @GetMapping(PERFORM_LOGIN)
    public String loginPage() {
        return "authentication/login";
    }

    @GetMapping(PERFORM_REGISTRATION)
    public String registrationPage(@ModelAttribute("account") Account account) {
        return "authentication/registration";
    }

    @PostMapping(PERFORM_REGISTRATION)
    public String performRegistration(@ModelAttribute("account") @Valid AccountDTO accountDTO, BindingResult bindingResult,
                                      @RequestParam(name = "g-recaptcha-response") String recaptchaResponse) {
        accountValidator.validate(accountDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            return "authentication/registration";
        }

        if (!captchaService.isCaptchaValid(recaptchaResponse)) {
            bindingResult.reject("captchaError", "Проверка reCaptcha не прошла. Пожалуйста, попробуйте еще раз.");
        }

        registrationService.register(registrationService.convertToAccount(accountDTO));

        return "redirect:/auth/login";
    }
}