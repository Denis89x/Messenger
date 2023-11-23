package by.lebenkov.messenger.util;

import by.lebenkov.messenger.dto.AccountDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.service.AccountDetailsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountValidator implements Validator {

    AccountRepository accountRepository;

    AccountDetailsService accountDetailsService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Account.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountDTO account = (AccountDTO) target;
        String password = account.getPassword();
        String email = account.getEmail();

        if (password.length() < 4 || password.length() > 15) {
            errors.rejectValue("password", "", "Пароль должен быть длиной от 4 до 15 символов");
        }

        if (!password.matches(".*[a-zA-Z].*")) {
            errors.rejectValue("password", "", "Пароль должен содержать латинские буквы");
        }

        if (!password.matches(".*\\d.*")) {
            errors.rejectValue("password", "", "Пароль должен содержать хотя бы одну цифру");
        }

        if (!password.matches(".*[!@#$%^&*()].*")) {
            errors.rejectValue("password", "", "Пароль должен содержать хотя бы один знак");
        }

        if (!account.getUsername().matches("(?=.*[a-zA-Z])[a-zA-Z0-9_]+")) {
            errors.rejectValue("username", "", "Логин может содержать только латинские буквы, цифры и символ '_'");
        }

        Optional<Account> newAccount = accountRepository.findByEmail(email);

        if (newAccount.isPresent())
            errors.rejectValue("email", "", "Эта почта уже используется");

        try {
            accountDetailsService.loadUserByUsername(account.getUsername());
        } catch (UsernameNotFoundException ignored) {
            return;
        }

        errors.rejectValue("username", "", "Это логин уже используется");
    }
}
