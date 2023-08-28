package by.lebenkov.messenger.util;

import by.lebenkov.messenger.dto.ChangeUsernameDTO;
import by.lebenkov.messenger.service.AccountServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangeUsernameValidator implements Validator {

    private final AccountServiceImp accountServiceImp;

    @Autowired
    public ChangeUsernameValidator(AccountServiceImp accountServiceImp) {
        this.accountServiceImp = accountServiceImp;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangeUsernameDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangeUsernameDTO changeUsernameDTO = (ChangeUsernameDTO) target;

        if (!changeUsernameDTO.getNewUsername().matches("(?=.*[a-zA-Z])[a-zA-Z0-9_]+")) {
            errors.rejectValue("newUsername", "", "Логин должен содержать только латинские буквы, цифры и символ '_'");
        }

        if (accountServiceImp.findByUsername(changeUsernameDTO.getNewUsername()).isPresent()) {
            errors.rejectValue("newUsername", "", "Этот логин уже используется");
        }
    }
}
