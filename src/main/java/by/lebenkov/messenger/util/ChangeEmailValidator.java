package by.lebenkov.messenger.util;

import by.lebenkov.messenger.dto.ChangeEmailDTO;
import by.lebenkov.messenger.service.AccountServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangeEmailValidator implements Validator {

    private final AccountServiceImp accountServiceImp;

    @Autowired
    public ChangeEmailValidator(AccountServiceImp accountServiceImp) {
        this.accountServiceImp = accountServiceImp;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangeEmailDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangeEmailDTO ChangeEmailDTO = (ChangeEmailDTO) target;

        if (accountServiceImp.findByEmail(ChangeEmailDTO.getNewEmail()).isPresent()) {
            errors.rejectValue("newEmail", "", "This e-mail is already taken");
        }
    }
}
